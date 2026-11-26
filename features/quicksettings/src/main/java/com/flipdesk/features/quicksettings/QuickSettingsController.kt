package com.flipdesk.features.quicksettings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class QuickSettingsState(
    val flashlightOn: Boolean = false,
    val dndEnabled: Boolean = false,
    val ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
    val autoRotateEnabled: Boolean = true,
    val brightness: Float = 0.6f,
    val batterySaverOn: Boolean = false
)

@Singleton
class QuickSettingsController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(readInitialState())
    val state: StateFlow<QuickSettingsState> = _state.asStateFlow()

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    fun refresh() {
        _state.value = readInitialState()
    }

    fun toggleFlashlight() {
        val backCameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: cameraManager.cameraIdList.firstOrNull() ?: return

        val next = !_state.value.flashlightOn
        runCatching {
            cameraManager.setTorchMode(backCameraId, next)
            _state.value = _state.value.copy(flashlightOn = next)
        }
    }

    fun toggleDnd() {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            launchSettings(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return
        }
        val next = !_state.value.dndEnabled
        notificationManager.setInterruptionFilter(
            if (next) NotificationManager.INTERRUPTION_FILTER_PRIORITY
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
        _state.value = _state.value.copy(dndEnabled = next)
    }

    fun cycleRingerMode() {
        val next = when (_state.value.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        runCatching {
            audioManager.ringerMode = next
            _state.value = _state.value.copy(ringerMode = next)
        }.onFailure {
            launchSettings(Intent(Settings.Panel.ACTION_VOLUME))
        }
    }

    fun toggleAutoRotate() {
        if (!Settings.System.canWrite(context)) {
            launchSettings(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            })
            return
        }
        val next = !_state.value.autoRotateEnabled
        Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (next) 1 else 0)
        _state.value = _state.value.copy(autoRotateEnabled = next)
    }

    fun setBrightness(fraction: Float) {
        val clamped = fraction.coerceIn(0.05f, 1f)
        if (!Settings.System.canWrite(context)) {
            launchSettings(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            })
            return
        }
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, (clamped * 255).toInt())
        _state.value = _state.value.copy(brightness = clamped)
    }

    fun openInternetPanel() = launchSettings(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
    fun openBluetoothSettings() = launchSettings(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    fun openAirplaneSettings() = launchSettings(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
    fun openBatterySaverSettings() = launchSettings(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
    fun openHotspotSettings() = launchSettings(Intent("android.settings.TETHER_SETTINGS"))
    fun openSystemSettings() = launchSettings(Intent(Settings.ACTION_SETTINGS))

    private fun launchSettings(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun readInitialState(): QuickSettingsState {
        val dnd = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
        val rot = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1
        }.getOrDefault(true)
        val bright = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 150) / 255f
        }.getOrDefault(0.6f)
        return QuickSettingsState(
            flashlightOn = false,
            dndEnabled = dnd,
            ringerMode = audioManager.ringerMode,
            autoRotateEnabled = rot,
            brightness = bright,
            batterySaverOn = powerManager.isPowerSaveMode
        )
    }
}
