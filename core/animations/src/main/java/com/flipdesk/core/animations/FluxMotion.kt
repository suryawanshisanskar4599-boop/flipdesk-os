package com.flipdesk.core.animations

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize
import com.flipdesk.core.design.theme.FluxAnimationIntensity

/**
 * Spring-based FLUX motion presets calibrated for the 60Hz 3.26" AMOLED cover display.
 */
object FluxMotion {
    fun <T> capsuleSpring(intensity: FluxAnimationIntensity): FiniteAnimationSpec<T> =
        when (intensity) {
            FluxAnimationIntensity.REDUCED -> tween(durationMillis = 120)
            FluxAnimationIntensity.NORMAL -> spring(
                dampingRatio = 0.78f,
                stiffness = 460f
            )
            FluxAnimationIntensity.ENHANCED -> spring(
                dampingRatio = 0.68f,
                stiffness = 380f
            )
        }

    fun <T> widgetSpring(intensity: FluxAnimationIntensity): FiniteAnimationSpec<T> =
        when (intensity) {
            FluxAnimationIntensity.REDUCED -> tween(durationMillis = 100)
            FluxAnimationIntensity.NORMAL -> spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
            FluxAnimationIntensity.ENHANCED -> spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow
            )
        }

    fun sizeSpring(intensity: FluxAnimationIntensity): FiniteAnimationSpec<IntSize> =
        capsuleSpring(intensity)
}

object FluxHaptics {
    fun tap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun confirm(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun longPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun gestureTick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}
