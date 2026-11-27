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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val accentOptions = listOf(0xFF38BDF8L, 0xFFCBD5E1L, 0xFFF4A29EL, 0xFFFBBF24L, 0xFFFDE047L, 0xFF86EFACL)
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // Centered ColorOS-inspired Cover Screen Live Preview Card (382:720)
        FluxTheme(config = config) {
            val previewColors = FluxThemeTokens.colors
            Box(
                modifier = Modifier
                    .width(172.dp)
                    .aspectRatio(382f / 720f)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(26.dp))
            ) {
                FluxWallpaperBackground(wallpaperId = config.wallpaperId)
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (config.clockStyle == FluxClockStyle.STACKED_EDITORIAL) "10\n30" else "01:13",
                                color = previewColors.accent,
                                fontSize = if (config.clockStyle == FluxClockStyle.STACKED_EDITORIAL) 34.sp else 26.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = when (config.clockStyle) {
                                    FluxClockStyle.NOTHING_DOT -> FontFamily.Monospace
                                    FluxClockStyle.STACKED_EDITORIAL -> FontFamily.Serif
                                    else -> FontFamily.SansSerif
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(previewColors.accent.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = previewColors.accent, modifier = Modifier.size(15.dp))
                            }
                        }
                        Text("Fri, 27 Nov  22°", color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Color(0x66000000))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("Dynamic Capsule • Ready", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Rounded Dark Editor Bottom Sheet (matching Image 3)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF17181C))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Dual Action Pills: Theme / Wallpaper
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF26282E))
                        .clickable { showAdvanced = !showAdvanced },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(if (showAdvanced) "Basic" else "Edit details", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF26282E))
                        .clickable {
                            val list = FluxWallpapers.presets
                            val idx = list.indexOfFirst { it.id == config.wallpaperId }
                            val next = list[(idx + 1) % list.size]
                            onUpdateConfig(config.copy(wallpaperId = next.id))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Change", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Digital Clock Visual Selector Tiles ("12" previews)
            Text("Digital clock", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FluxClockStyle.entries.forEach { style ->
                    val selected = config.clockStyle == style
                    Box(
                        modifier = Modifier
                            .size(width = 66.dp, height = 56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2A2C33))
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) Color(0xFF2563EB) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onUpdateConfig(config.copy(clockStyle = style)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "12",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = when (style) {
                                FluxClockStyle.MINIMAL_THIN -> FontWeight.Light
                                FluxClockStyle.MODERN_BOLD -> FontWeight.ExtraBold
                                else -> FontWeight.SemiBold
                            },
                            fontFamily = when (style) {
                                FluxClockStyle.NOTHING_DOT -> FontFamily.Monospace
                                FluxClockStyle.STACKED_EDITORIAL -> FontFamily.Serif
                                else -> FontFamily.SansSerif
                            }
                        )
                    }
                }
            }

            // Widget Colour Swatches
            Text("Widget colour", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                accentOptions.forEach { argb ->
                    val selected = config.accentColorArgb == argb
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(argb))
                            .border(if (selected) 2.5.dp else 0.dp, Color(0xFF2563EB), CircleShape)
                            .clickable {
                                onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, accentColorArgb = argb, primaryColorArgb = argb))
                            }
                    )
                }
            }

            if (showAdvanced) {
                Text("Flux Theme Preset", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FluxThemePreset.entries.forEach { preset ->
                        FilterChip(
                            selected = config.preset == preset,
                            onClick = { onUpdateConfig(FluxThemeConfig.forPreset(preset).copy(clockStyle = config.clockStyle, wallpaperId = config.wallpaperId)) },
                            label = { Text(preset.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("24-Hour Clock", color = Color.White, fontSize = 13.sp)
                    Switch(checked = config.use24HourClock, onCheckedChange = { onUpdateConfig(config.copy(use24HourClock = it)) })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Show Seconds", color = Color.White, fontSize = 13.sp)
                    Switch(checked = config.showClockSeconds, onCheckedChange = { onUpdateConfig(config.copy(showClockSeconds = it)) })
                }
                StudioSlider("Corner Radius (${config.cornerRadiusDp.toInt()} dp)", config.cornerRadiusDp, 10f..28f) {
                    onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, cornerRadiusDp = it))
                }
                StudioSlider("Widget Opacity (${(config.widgetTransparency * 100).toInt()}%)", config.widgetTransparency, 0.3f..1.0f) {
                    onUpdateConfig(config.copy(preset = FluxThemePreset.CUSTOM_FLUX, widgetTransparency = it))
                }
                StudioSlider("Gesture Sensitivity (${String.format("%.1f", gestureSensitivity)}x)", gestureSensitivity, 0.5f..1.5f) {
                    onUpdateGestureSensitivity(it)
                }
                StudioSlider("Capsule Auto-Collapse (${capsuleCollapseMs / 1000}s)", capsuleCollapseMs.toFloat(), 2000f..10000f) {
                    onUpdateCapsuleCollapseMs(it.toLong())
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FluxIconStyle.entries.forEach { style ->
                        FilterChip(
                            selected = config.iconStyle == style,
                            onClick = { onUpdateConfig(config.copy(iconStyle = style)) },
                            label = { Text(style.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FluxAnimationIntensity.entries.forEach { intensity ->
                        FilterChip(
                            selected = config.animationIntensity == intensity,
                            onClick = { onUpdateConfig(config.copy(animationIntensity = intensity)) },
                            label = { Text(intensity.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRestoreDefaults,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26282E)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset") }
                    Button(
                        onClick = onExportLayout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26282E)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Export JSON") }
                }
            }

            // Full-width bright blue Apply pill button (matching Image 3)
            Button(
                onClick = { onUpdateConfig(config) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D74F5)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Apply", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StudioSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
