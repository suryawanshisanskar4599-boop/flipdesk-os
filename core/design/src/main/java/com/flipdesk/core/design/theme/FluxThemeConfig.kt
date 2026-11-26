package com.flipdesk.core.design.theme

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * The 5 built-in FLUX visual presets for FlipDesk OS.
 */
@Serializable
enum class FluxThemePreset(val displayName: String) {
    FLUX_DARK("Flux Dark"),
    FLUX_LIGHT("Flux Light"),
    FLUX_GLASS("Flux Glass"),
    FLUX_AMOLED("Flux AMOLED"),
    CUSTOM_FLUX("Custom Flux")
}

@Serializable
enum class FluxIconStyle(val displayName: String) {
    ROUNDED_SQUIRCLE("Squircle"),
    CIRCLE("Circle"),
    MINIMAL_OUTLINE("Minimal Outline")
}

@Serializable
enum class FluxAnimationIntensity(val displayName: String, val speedMultiplier: Float) {
    REDUCED("Reduced", 0.35f),
    NORMAL("Normal", 1.0f),
    ENHANCED("Enhanced", 1.25f)
}

@Serializable
enum class FluxClockStyle(val displayName: String) {
    MODERN_BOLD("Modern Bold"),
    MINIMAL_THIN("Minimal Thin"),
    STACKED_EDITORIAL("Stacked Editorial"),
    NOTHING_DOT("Dot Matrix"),
    COMPACT_PILL("Compact Pill")
}

/**
 * Central theme configuration persisted per profile and consumed across all FlipDesk modules.
 */
@Serializable
data class FluxThemeConfig(
    val preset: FluxThemePreset = FluxThemePreset.FLUX_DARK,
    val primaryColorArgb: Long = 0xFF3B82F6L,
    val accentColorArgb: Long = 0xFF38BDF8L,
    val backgroundOpacity: Float = 0.92f,
    val cornerRadiusDp: Float = 20f,
    val blurIntensityDp: Float = 16f,
    val widgetTransparency: Float = 0.78f,
    val fontScale: Float = 1.0f,
    val iconStyle: FluxIconStyle = FluxIconStyle.ROUNDED_SQUIRCLE,
    val animationIntensity: FluxAnimationIntensity = FluxAnimationIntensity.NORMAL,
    val clockStyle: FluxClockStyle = FluxClockStyle.MODERN_BOLD,
    val use24HourClock: Boolean = false,
    val showClockSeconds: Boolean = false,
    val wallpaperId: String = "obsidian_mesh",
    val extractAccentFromWallpaper: Boolean = false
) {
    val primaryColor: Color get() = Color(primaryColorArgb)
    val accentColor: Color get() = Color(accentColorArgb)

    companion object {
        fun forPreset(preset: FluxThemePreset): FluxThemeConfig = when (preset) {
            FluxThemePreset.FLUX_DARK -> FluxThemeConfig(
                preset = preset,
                primaryColorArgb = 0xFF60A5FAL,
                accentColorArgb = 0xFF38BDF8L,
                backgroundOpacity = 0.94f,
                cornerRadiusDp = 20f,
                blurIntensityDp = 16f,
                widgetTransparency = 0.80f
            )
            FluxThemePreset.FLUX_LIGHT -> FluxThemeConfig(
                preset = preset,
                primaryColorArgb = 0xFF2563EBL,
                accentColorArgb = 0xFF0284C7L,
                backgroundOpacity = 0.92f,
                cornerRadiusDp = 20f,
                blurIntensityDp = 14f,
                widgetTransparency = 0.86f
            )
            FluxThemePreset.FLUX_GLASS -> FluxThemeConfig(
                preset = preset,
                primaryColorArgb = 0xFFA78BFAL,
                accentColorArgb = 0xFF38BDF8L,
                backgroundOpacity = 0.65f,
                cornerRadiusDp = 24f,
                blurIntensityDp = 22f,
                widgetTransparency = 0.56f
            )
            FluxThemePreset.FLUX_AMOLED -> FluxThemeConfig(
                preset = preset,
                primaryColorArgb = 0xFFF4F4F5L,
                accentColorArgb = 0xFF22D3EEL,
                backgroundOpacity = 1.0f,
                cornerRadiusDp = 18f,
                blurIntensityDp = 0f,
                widgetTransparency = 0.92f
            )
            FluxThemePreset.CUSTOM_FLUX -> FluxThemeConfig(preset = preset)
        }
    }
}
