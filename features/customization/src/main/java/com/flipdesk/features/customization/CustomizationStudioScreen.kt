package com.flipdesk.features.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.design.components.FluxGlassCard
import com.flipdesk.core.design.theme.FluxAnimationIntensity
import com.flipdesk.core.design.theme.FluxClockStyle
import com.flipdesk.core.design.theme.FluxIconStyle
import com.flipdesk.core.design.theme.FluxTheme
import com.flipdesk.core.design.theme.FluxThemeConfig
import com.flipdesk.core.design.theme.FluxThemePreset
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.design.wallpaper.FluxWallpaperBackground
import com.flipdesk.core.design.wallpaper.FluxWallpapers

@Composable
fun CustomizationStudioScreen(
    config: FluxThemeConfig,
    gestureSensitivity: Float,
    capsuleCollapseMs: Long,
    onUpdateConfig: (FluxThemeConfig) -> Unit,
    onUpdateGestureSensitivity: (Float) -> Unit,
    onUpdateCapsuleCollapseMs: (Long) -> Unit,
    onRestoreDefaults: () -> Unit,
    onExportLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentOptions = listOf(0xFF38BDF8L, 0xFF60A5FAL, 0xFF34D399L, 0xFFA78BFAL, 0xFFF472B6L, 0xFFFBBF24L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("FLUX Customization Studio", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Live 382x720 Cover Screen Preview
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FluxTheme(config = config) {
                val previewColors = FluxThemeTokens.colors
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .aspectRatio(382f / 720f)
                        .clip(RoundedCornerShape(22.dp))
                        .border(2.dp, previewColors.borderHighlight, RoundedCornerShape(22.dp))
                ) {
                    FluxWallpaperBackground(wallpaperId = config.wallpaperId)
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                                .clip(CircleShape)
                                .background(previewColors.capsuleBackground)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("FlipDesk", color = previewColors.textPrimary, fontSize = 8.sp)
                        }
                        Text("10:09", color = previewColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        FluxGlassCard(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                                Text("Weather • 26°", color = previewColors.textPrimary, fontSize = 10.sp)
                            }
                        }
                        FluxGlassCard(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                            Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                                Text("Now Playing", color = previewColors.accent, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Theme Presets
        Text("Theme Preset", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxThemePreset.entries.forEach { preset ->
                FilterChip(
                    selected = config.preset == preset,
                    onClick = { onUpdateConfig(FluxThemeConfig.forPreset(preset).copy(clockStyle = config.clockStyle, wallpaperId = config.wallpaperId)) },
                    label = { Text(preset.displayName, fontSize = 12.sp) }
                )
            }
        }

        // Accent Colors
        Text("Accent Colour", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            accentOptions.forEach { argb ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .border(if (config.accentColorArgb == argb) 2.5.dp else 0.dp, Color.White, CircleShape)
                        .clickable {
                            onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, accentColorArgb = argb, primaryColorArgb = argb))
                        }
                )
            }
        }

        // Wallpapers
        Text("Cover Wallpaper", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxWallpapers.presets.forEach { wp ->
                FilterChip(
                    selected = config.wallpaperId == wp.id,
                    onClick = { onUpdateConfig(config.copy(wallpaperId = wp.id)) },
                    label = { Text(wp.name, fontSize = 12.sp) }
                )
            }
        }

        // Clock Style
        Text("Clock Style", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxClockStyle.entries.forEach { style ->
                FilterChip(
                    selected = config.clockStyle == style,
                    onClick = { onUpdateConfig(config.copy(clockStyle = style)) },
                    label = { Text(style.displayName, fontSize = 12.sp) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("24-Hour Clock", fontSize = 13.sp)
            Switch(checked = config.use24HourClock, onCheckedChange = { onUpdateConfig(config.copy(use24HourClock = it)) })
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Show Seconds", fontSize = 13.sp)
            Switch(checked = config.showClockSeconds, onCheckedChange = { onUpdateConfig(config.copy(showClockSeconds = it)) })
        }

        // Sliders
        StudioSlider("Corner Radius (${config.cornerRadiusDp.toInt()} dp)", config.cornerRadiusDp, 8f..28f) {
            onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, cornerRadiusDp = it))
        }
        StudioSlider("Widget Opacity (${(config.widgetTransparency * 100).toInt()}%)", config.widgetTransparency, 0.3f..1.0f) {
            onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, widgetTransparency = it))
        }
        StudioSlider("Font Scale (${String.format("%.2f", config.fontScale)}x)", config.fontScale, 0.85f..1.25f) {
            onUpdateConfig(config.copy(fontScale = it))
        }
        StudioSlider("Gesture Sensitivity (${String.format("%.1f", gestureSensitivity)}x)", gestureSensitivity, 0.5f..1.5f) {
            onUpdateGestureSensitivity(it)
        }
        StudioSlider("Capsule Auto-Collapse (${capsuleCollapseMs / 1000}s)", capsuleCollapseMs.toFloat(), 2000f..10000f) {
            onUpdateCapsuleCollapseMs(it.toLong())
        }

        // Icon Style & Animation Intensity
        Text("Icon Style", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxIconStyle.entries.forEach { style ->
                FilterChip(
                    selected = config.iconStyle == style,
                    onClick = { onUpdateConfig(config.copy(iconStyle = style)) },
                    label = { Text(style.displayName, fontSize = 12.sp) }
                )
            }
        }

        Text("Animation Intensity", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FluxAnimationIntensity.entries.forEach { intensity ->
                FilterChip(
                    selected = config.animationIntensity == intensity,
                    onClick = { onUpdateConfig(config.copy(animationIntensity = intensity)) },
                    label = { Text(intensity.displayName, fontSize = 12.sp) }
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRestoreDefaults, modifier = Modifier.weight(1f)) {
                Text("Restore Defaults")
            }
            Button(onClick = onExportLayout, modifier = Modifier.weight(1f)) {
                Text("Export Layout")
            }
        }
    }
}

@Composable
private fun StudioSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
