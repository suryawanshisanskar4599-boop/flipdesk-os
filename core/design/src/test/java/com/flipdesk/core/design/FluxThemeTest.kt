package com.flipdesk.core.design

import androidx.compose.ui.graphics.Color
import com.flipdesk.core.design.theme.FluxThemeConfig
import com.flipdesk.core.design.theme.FluxThemePreset
import com.flipdesk.core.design.theme.resolveFluxPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FluxThemeTest {
    @Test
    fun `forPreset creates expected light and dark palettes`() {
        val light = resolveFluxPalette(FluxThemeConfig.forPreset(FluxThemePreset.FLUX_LIGHT))
        val amoled = resolveFluxPalette(FluxThemeConfig.forPreset(FluxThemePreset.FLUX_AMOLED))

        assertTrue(light.isLight)
        assertFalse(amoled.isLight)
        assertEquals(Color.Black, amoled.background)
    }
}
