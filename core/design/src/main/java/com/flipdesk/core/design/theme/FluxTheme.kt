package com.flipdesk.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class FluxColorPalette(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceGlass: Color,
    val borderHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val capsuleBackground: Color,
    val capsuleBorder: Color,
    val isLight: Boolean
)

val LocalFluxColors = staticCompositionLocalOf {
    FluxColorPalette(
        background = Color(0xFF06070A),
        surface = Color(0xFF12141A),
        surfaceElevated = Color(0xFF1B1E27),
        surfaceGlass = Color(0xCC171A23),
        borderHighlight = Color(0x26FFFFFF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFCBD5E1),
        textMuted = Color(0xFF64748B),
        primary = Color(0xFF60A5FA),
        accent = Color(0xFF38BDF8),
        success = Color(0xFF34D399),
        warning = Color(0xFFFBBF24),
        error = Color(0xFFF87171),
        capsuleBackground = Color(0xFF0A0B0E),
        capsuleBorder = Color(0x33FFFFFF),
        isLight = false
    )
}

val LocalFluxConfig = staticCompositionLocalOf { FluxThemeConfig() }

fun resolveFluxPalette(config: FluxThemeConfig): FluxColorPalette {
    val isLight = config.preset == FluxThemePreset.FLUX_LIGHT
    val primary = config.primaryColor
    val accent = config.accentColor
    val widgetAlpha = config.widgetTransparency.coerceIn(0.25f, 1.0f)

    return when (config.preset) {
        FluxThemePreset.FLUX_LIGHT -> FluxColorPalette(
            background = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF).copy(alpha = widgetAlpha),
            surfaceElevated = Color(0xFFF8FAFC).copy(alpha = (widgetAlpha + 0.08f).coerceAtMost(1f)),
            surfaceGlass = Color(0xFFFFFFFF).copy(alpha = (widgetAlpha * 0.85f).coerceIn(0.35f, 0.95f)),
            borderHighlight = Color(0x180F172A),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF334155),
            textMuted = Color(0xFF64748B),
            primary = primary,
            accent = accent,
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            error = Color(0xFFEF4444),
            capsuleBackground = Color(0xFF090A0D),
            capsuleBorder = Color(0x29FFFFFF),
            isLight = true
        )
        FluxThemePreset.FLUX_AMOLED -> FluxColorPalette(
            background = Color(0xFF000000),
            surface = Color(0xFF0B0C0F).copy(alpha = widgetAlpha),
            surfaceElevated = Color(0xFF14161B),
            surfaceGlass = Color(0xFF0E1015).copy(alpha = widgetAlpha),
            borderHighlight = Color(0x1EFFFFFF),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFFD4D4D8),
            textMuted = Color(0xFF71717A),
            primary = primary,
            accent = accent,
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            error = Color(0xFFF87171),
            capsuleBackground = Color(0xFF000000),
            capsuleBorder = Color(0x38FFFFFF),
            isLight = false
        )
        FluxThemePreset.FLUX_GLASS -> FluxColorPalette(
            background = Color(0xFF080B12),
            surface = Color(0xFF161B26).copy(alpha = widgetAlpha),
            surfaceElevated = Color(0xFF1F2636).copy(alpha = (widgetAlpha + 0.1f).coerceAtMost(0.95f)),
            surfaceGlass = Color(0xFF1D2433).copy(alpha = widgetAlpha),
            borderHighlight = Color(0x36FFFFFF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFE2E8F0),
            textMuted = Color(0xFF94A3B8),
            primary = primary,
            accent = accent,
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            error = Color(0xFFF87171),
            capsuleBackground = Color(0xFF07090E),
            capsuleBorder = Color(0x40FFFFFF),
            isLight = false
        )
        FluxThemePreset.FLUX_DARK, FluxThemePreset.CUSTOM_FLUX -> FluxColorPalette(
            background = Color(0xFF08090D),
            surface = Color(0xFF13161E).copy(alpha = widgetAlpha),
            surfaceElevated = Color(0xFF1B1F2A).copy(alpha = (widgetAlpha + 0.08f).coerceAtMost(1f)),
            surfaceGlass = Color(0xFF161A24).copy(alpha = widgetAlpha),
            borderHighlight = Color(0x22FFFFFF),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFCBD5E1),
            textMuted = Color(0xFF64748B),
            primary = primary,
            accent = accent,
            success = Color(0xFF34D399),
            warning = Color(0xFFFBBF24),
            error = Color(0xFFF87171),
            capsuleBackground = Color(0xFF07080B),
            capsuleBorder = Color(0x2EFFFFFF),
            isLight = isLight
        )
    }
}

@Composable
fun FluxTheme(
    config: FluxThemeConfig = FluxThemeConfig(),
    content: @Composable () -> Unit
) {
    val palette = resolveFluxPalette(config)
    val scale = config.fontScale.coerceIn(0.8f, 1.3f)

    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.accent,
            background = palette.background,
            surface = palette.surface,
            onPrimary = Color.White,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    } else {
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.accent,
            background = palette.background,
            surface = palette.surface,
            onPrimary = Color.Black,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    }

    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (42 * scale).sp,
            letterSpacing = (-1.5).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            letterSpacing = (-0.4).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (10 * scale).sp,
            letterSpacing = 0.2.sp
        )
    )

    val radius = config.cornerRadiusDp.dp
    val shapes = Shapes(
        small = RoundedCornerShape((radius * 0.5f).coerceAtLeast(8.dp)),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape((radius * 1.25f).coerceAtMost(32.dp))
    )

    CompositionLocalProvider(
        LocalFluxColors provides palette,
        LocalFluxConfig provides config
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

object FluxThemeTokens {
    val colors: FluxColorPalette
        @Composable get() = LocalFluxColors.current

    val config: FluxThemeConfig
        @Composable get() = LocalFluxConfig.current
}
