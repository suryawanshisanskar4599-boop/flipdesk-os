package com.flipdesk.core.design.wallpaper

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.flipdesk.core.design.theme.FluxThemeTokens

data class FluxWallpaperPreset(
    val id: String,
    val name: String,
    val topColor: Color,
    val midColor: Color,
    val bottomColor: Color,
    val orbColor: Color
)

object FluxWallpapers {
    val presets = listOf(
        FluxWallpaperPreset(
            id = "obsidian_mesh",
            name = "Obsidian Mesh",
            topColor = Color(0xFF050609),
            midColor = Color(0xFF0C1220),
            bottomColor = Color(0xFF050608),
            orbColor = Color(0xFF38BDF8)
        ),
        FluxWallpaperPreset(
            id = "graphite_aurora",
            name = "Graphite Aurora",
            topColor = Color(0xFF080A0F),
            midColor = Color(0xFF141926),
            bottomColor = Color(0xFF090B10),
            orbColor = Color(0xFF34D399)
        ),
        FluxWallpaperPreset(
            id = "titanium_violet",
            name = "Titanium Violet",
            topColor = Color(0xFF090710),
            midColor = Color(0xFF1A142B),
            bottomColor = Color(0xFF06060A),
            orbColor = Color(0xFFA78BFA)
        ),
        FluxWallpaperPreset(
            id = "pure_amoled",
            name = "Pure AMOLED",
            topColor = Color(0xFF000000),
            midColor = Color(0xFF000000),
            bottomColor = Color(0xFF000000),
            orbColor = Color(0xFF1E293B)
        ),
        FluxWallpaperPreset(
            id = "silver_mist",
            name = "Silver Mist",
            topColor = Color(0xFFE2E8F0),
            midColor = Color(0xFFF8FAFC),
            bottomColor = Color(0xFFCBD5E1),
            orbColor = Color(0xFF3B82F6)
        )
    )

    fun findById(id: String): FluxWallpaperPreset =
        presets.firstOrNull { it.id == id } ?: presets.first()

    /**
     * Extracts a dominant/vibrant accent color from a custom wallpaper bitmap using AndroidX Palette.
     */
    fun extractAccentColor(bitmap: Bitmap, fallbackArgb: Long): Long {
        val palette = Palette.from(bitmap).generate()
        return (palette.vibrantSwatch?.rgb
            ?: palette.lightVibrantSwatch?.rgb
            ?: palette.dominantSwatch?.rgb)?.toLong()?.and(0xFFFFFFFFL)
            ?: fallbackArgb
    }
}

@Composable
fun FluxWallpaperBackground(
    wallpaperId: String = FluxThemeTokens.config.wallpaperId,
    modifier: Modifier = Modifier
) {
    val config = FluxThemeTokens.config
    val colors = FluxThemeTokens.colors
    val preset = FluxWallpapers.findById(wallpaperId)

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    preset.topColor.copy(alpha = config.backgroundOpacity),
                    preset.midColor.copy(alpha = config.backgroundOpacity),
                    preset.bottomColor.copy(alpha = config.backgroundOpacity)
                )
            )
        )
        if (wallpaperId != "pure_amoled") {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.22f),
                    radius = size.width * 0.75f
                ),
                center = Offset(size.width * 0.8f, size.height * 0.22f),
                radius = size.width * 0.75f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        preset.orbColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.78f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.18f, size.height * 0.78f),
                radius = size.width * 0.65f
            )
        }
    }
}
