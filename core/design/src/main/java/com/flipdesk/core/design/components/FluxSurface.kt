package com.flipdesk.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.flipdesk.core.design.theme.FluxThemeTokens

/**
 * Frosted glassmorphic FLUX panel with subtle top highlight and rounded corners.
 */
@Composable
fun FluxGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = FluxThemeTokens.config.cornerRadiusDp.dp,
    elevated: Boolean = false,
    accentGlow: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = FluxThemeTokens.colors
    val shape = RoundedCornerShape(cornerRadius)
    val baseSurface = if (elevated) colors.surfaceElevated else colors.surfaceGlass

    val borderBrush = if (accentGlow != null) {
        Brush.linearGradient(
            listOf(
                accentGlow.copy(alpha = 0.55f),
                colors.borderHighlight,
                accentGlow.copy(alpha = 0.2f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.borderHighlight,
                colors.borderHighlight.copy(alpha = 0.06f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseSurface)
            .border(BorderStroke(0.8.dp, borderBrush), shape),
        content = content
    )
}

/**
 * Compact FLUX pill button or badge container.
 */
@Composable
fun FluxPill(
    modifier: Modifier = Modifier,
    backgroundColor: Color = FluxThemeTokens.colors.surfaceElevated,
    borderColor: Color = FluxThemeTokens.colors.borderHighlight,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = BorderStroke(0.75.dp, borderColor)
    ) {
        Box(content = content)
    }
}
