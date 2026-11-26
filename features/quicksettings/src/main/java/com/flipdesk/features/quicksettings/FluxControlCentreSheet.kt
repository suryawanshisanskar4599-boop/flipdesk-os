package com.flipdesk.features.quicksettings

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.design.components.FluxGlassCard
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.platform.SystemStatusSnapshot

@Composable
fun FluxControlCentreSheet(
    qsState: QuickSettingsState,
    systemStatus: SystemStatusSnapshot,
    controller: QuickSettingsController,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors

    FluxGlassCard(modifier = modifier.fillMaxWidth(), elevated = true) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Control Centre", color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Close", color = colors.accent, fontSize = 11.sp, modifier = Modifier.clickable { onClose() })
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QsTile("Wi-Fi", if (systemStatus.wifiConnected) "On" else "Panel", Icons.Rounded.Wifi, systemStatus.wifiConnected, Modifier.weight(1f)) {
                    controller.openInternetPanel()
                }
                QsTile("Bluetooth", if (systemStatus.bluetoothEnabled) "On" else "Off", Icons.Rounded.Bluetooth, systemStatus.bluetoothEnabled, Modifier.weight(1f)) {
                    controller.openBluetoothSettings()
                }
                QsTile("Data", if (systemStatus.cellularConnected) "Active" else "Panel", Icons.Rounded.SignalCellularAlt, systemStatus.cellularConnected, Modifier.weight(1f)) {
                    controller.openInternetPanel()
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QsTile("Torch", if (qsState.flashlightOn) "On" else "Off", Icons.Rounded.FlashlightOn, qsState.flashlightOn, Modifier.weight(1f)) {
                    controller.toggleFlashlight()
                }
                QsTile("DND", if (qsState.dndEnabled) "On" else "Off", Icons.Rounded.DoNotDisturbOn, qsState.dndEnabled, Modifier.weight(1f)) {
                    controller.toggleDnd()
                }
                val soundLabel = when (qsState.ringerMode) {
                    AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                    AudioManager.RINGER_MODE_SILENT -> "Silent"
                    else -> "Ring"
                }
                QsTile("Sound", soundLabel, Icons.Rounded.NotificationsActive, qsState.ringerMode == AudioManager.RINGER_MODE_NORMAL, Modifier.weight(1f)) {
                    controller.cycleRingerMode()
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QsTile("Rotate", if (qsState.autoRotateEnabled) "Auto" else "Lock", Icons.Rounded.ScreenRotation, qsState.autoRotateEnabled, Modifier.weight(1f)) {
                    controller.toggleAutoRotate()
                }
                QsTile("Saver", if (qsState.batterySaverOn) "On" else "Off", Icons.Rounded.BatterySaver, qsState.batterySaverOn, Modifier.weight(1f)) {
                    controller.openBatterySaverSettings()
                }
                QsTile("Airplane", "Panel", Icons.Rounded.AirplanemodeActive, false, Modifier.weight(1f)) {
                    controller.openAirplaneSettings()
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QsTile("Hotspot", "Panel", Icons.Rounded.WifiTethering, false, Modifier.weight(1f)) {
                    controller.openHotspotSettings()
                }
                QsTile("Settings", "System", Icons.Rounded.Settings, false, Modifier.weight(1f)) {
                    controller.openSystemSettings()
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.BrightnessMedium, contentDescription = "Brightness", tint = colors.accent, modifier = Modifier.size(18.dp))
                Slider(
                    value = qsState.brightness,
                    onValueChange = { controller.setBrightness(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QsTile(
    label: String,
    status: String,
    icon: ImageVector,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FluxThemeTokens.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) colors.primary.copy(alpha = 0.24f) else colors.surfaceElevated)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (active) colors.accent else colors.textSecondary, modifier = Modifier.size(18.dp))
        Text(label, color = colors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(status, color = colors.textMuted, fontSize = 9.sp, maxLines = 1)
    }
}
