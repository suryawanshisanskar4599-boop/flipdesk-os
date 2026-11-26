package com.flipdesk.core.permissions

import android.Manifest
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.Display
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SupportLevel {
    SUPPORTED,
    PARTIAL_FALLBACK,
    REQUIRES_PERMISSION,
    REQUIRES_ADB_OR_SYSTEM,
    UNSUPPORTED
}

data class CapabilityItem(
    val id: String,
    val title: String,
    val level: SupportLevel,
    val detail: String,
    val adbCommand: String? = null
)

data class DisplaySummary(
    val displayId: Int,
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val refreshRateHz: Float,
    val isDefault: Boolean
)

data class DeviceCapabilityReport(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val colorOsVersion: String?,
    val isOppoFindN3Flip: Boolean,
    val displays: List<DisplaySummary>,
    val hasHingeSensor: Boolean,
    val capabilities: List<CapabilityItem>
)

@Singleton
class DeviceCapabilityProbe @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun probe(
        notificationListenerComponent: ComponentName? = null,
        accessibilityComponent: ComponentName? = null
    ): DeviceCapabilityReport {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = dm.displays.map { display ->
            val size = Point()
            @Suppress("DEPRECATION")
            display.getRealSize(size)
            DisplaySummary(
                displayId = display.displayId,
                name = display.name ?: "Display ${display.displayId}",
                widthPx = size.x,
                heightPx = size.y,
                refreshRateHz = display.refreshRate,
                isDefault = display.displayId == Display.DEFAULT_DISPLAY
            )
        }

        val secondaryDisplays = displays.filter { !it.isDefault }
        val colorOs = readSystemProperty("ro.build.version.oplusrom")
            ?: readSystemProperty("ro.build.version.opporom")
        val hasHinge = context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)

        val notifEnabled = isNotificationListenerEnabled(notificationListenerComponent)
        val accessEnabled = isAccessibilityEnabled(accessibilityComponent)
        val overlayEnabled = Settings.canDrawOverlays(context)
        val writeSettings = Settings.System.canWrite(context)
        val dndAccess = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .isNotificationPolicyAccessGranted
        val usageStats = hasUsageStatsPermission()
        val hasWriteSecure = ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
        val hasCamera = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val hasAudio = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isOppoFlip = Build.MANUFACTURER.equals("OPPO", ignoreCase = true) &&
            (Build.MODEL.contains("CPH2519", ignoreCase = true) ||
                Build.MODEL.contains("PHT110", ignoreCase = true) ||
                Build.MODEL.contains("Find N3 Flip", ignoreCase = true))

        val capabilities = listOf(
            CapabilityItem(
                id = "cover_display",
                title = "External Cover Display Detection",
                level = if (secondaryDisplays.isNotEmpty()) SupportLevel.SUPPORTED else SupportLevel.PARTIAL_FALLBACK,
                detail = if (secondaryDisplays.isNotEmpty()) {
                    val d = secondaryDisplays.first()
                    "Detected secondary display #${d.displayId} (${d.widthPx}x${d.heightPx} @ ${d.refreshRateHz.toInt()}Hz)."
                } else {
                    "No secondary display currently active; FlipDesk renders in 382x720 cover-viewport mode on the active display."
                }
            ),
            CapabilityItem(
                id = "fold_state",
                title = "Fold / Hinge State Tracking",
                level = if (hasHinge) SupportLevel.SUPPORTED else SupportLevel.PARTIAL_FALLBACK,
                detail = if (hasHinge) {
                    "Jetpack WindowManager WindowInfoTracker + hinge angle sensor available."
                } else {
                    "Using WindowInfoTracker + DisplayManager listener fallback."
                }
            ),
            CapabilityItem(
                id = "auto_launch_cover",
                title = "Automatic Cover-Screen Activation on Fold",
                level = if (overlayEnabled && accessEnabled) SupportLevel.PARTIAL_FALLBACK else SupportLevel.REQUIRES_PERMISSION,
                detail = "ColorOS restricts background activity launches on fold. Enable Overlay + Accessibility or use Quick Settings Tile / Mini Apps."
            ),
            CapabilityItem(
                id = "third_party_launch_display",
                title = "Launch Arbitrary Apps on Cover Display",
                level = SupportLevel.REQUIRES_ADB_OR_SYSTEM,
                detail = "ColorOS blocks non-system ActivityOptions.setLaunchDisplayId for arbitrary apps. Native Mini Apps & main-display fallback are used.",
                adbCommand = "adb shell settings put global force_resizable_activities 1"
            ),
            CapabilityItem(
                id = "notifications",
                title = "Dynamic Capsule & Notification Mirroring",
                level = if (notifEnabled) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_PERMISSION,
                detail = if (notifEnabled) "NotificationListenerService active." else "Grant Notification Access to mirror & reply in Dynamic Capsule."
            ),
            CapabilityItem(
                id = "media_session",
                title = "MediaSession Music Controls",
                level = if (notifEnabled) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_PERMISSION,
                detail = "Uses MediaSessionManager active sessions (requires Notification Access)."
            ),
            CapabilityItem(
                id = "dnd_control",
                title = "Do Not Disturb & System Toggles",
                level = if (dndAccess && writeSettings) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_PERMISSION,
                detail = "Flashlight & Audio work directly; DND & Brightness need policy/write permissions; Wi-Fi/Data open system panels."
            ),
            CapabilityItem(
                id = "secure_settings_adb",
                title = "ADB-Assisted Secure Settings",
                level = if (hasWriteSecure) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_ADB_OR_SYSTEM,
                detail = if (hasWriteSecure) "WRITE_SECURE_SETTINGS granted." else "Optional ADB grant enables enhanced cover-screen state integration.",
                adbCommand = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
            ),
            CapabilityItem(
                id = "camera_audio",
                title = "Quick Camera, QR & Voice Recorder",
                level = if (hasCamera && hasAudio) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_PERMISSION,
                detail = "CameraX preview & MediaRecorder voice notes."
            ),
            CapabilityItem(
                id = "usage_stats",
                title = "App Usage & Recent Apps",
                level = if (usageStats) SupportLevel.SUPPORTED else SupportLevel.REQUIRES_PERMISSION,
                detail = "Optional UsageStatsManager access for recently used apps."
            )
        )

        return DeviceCapabilityReport(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            colorOsVersion = colorOs,
            isOppoFindN3Flip = isOppoFlip,
            displays = displays,
            hasHingeSensor = hasHinge,
            capabilities = capabilities
        )
    }

    private fun isNotificationListenerEnabled(component: ComponentName?): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return if (component != null) {
            flat.contains(component.flattenToString()) || flat.contains(component.flattenToShortString())
        } else {
            flat.contains(context.packageName)
        }
    }

    private fun isAccessibilityEnabled(component: ComponentName?): Boolean {
        val enabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
        if (!enabled) return false
        val flat = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return if (component != null) {
            flat.contains(component.flattenToString()) || flat.contains(component.flattenToShortString())
        } else {
            flat.contains(context.packageName)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun readSystemProperty(key: String): String? = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
        process.inputStream.bufferedReader().readText().trim().takeIf { it.isNotEmpty() }
    }.getOrNull()
}
