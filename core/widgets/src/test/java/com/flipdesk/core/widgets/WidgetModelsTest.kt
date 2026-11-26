package com.flipdesk.core.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WidgetModelsTest {
    @Test
    fun `all built-in widget types resolve by id`() {
        BuiltInWidgetType.entries.forEach { type ->
            assertNotNull(BuiltInWidgetType.fromId(type.id))
        }
    }

    @Test
    fun `widget size resolves from span`() {
        assertEquals(WidgetSize.WIDE_2X1, WidgetSize.fromSpan(2, 1))
        assertEquals(WidgetSize.LARGE_2X2, WidgetSize.fromSpan(2, 2))
        assertEquals(WidgetSize.SMALL_1X1, WidgetSize.fromSpan(1, 1))
    }
}
