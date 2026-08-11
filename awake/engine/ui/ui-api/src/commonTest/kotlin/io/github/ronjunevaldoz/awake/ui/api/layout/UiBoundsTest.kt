package io.github.ronjunevaldoz.awake.ui.api.layout

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiBoundsTest {
    @Test
    fun intersectReturnsOnlySharedArea() {
        assertEquals(
            UiBounds(x = 20f, y = 20f, width = 80f, height = 40f),
            UiBounds(x = 0f, y = 0f, width = 100f, height = 60f)
                .intersect(UiBounds(x = 20f, y = 20f, width = 100f, height = 100f)),
        )
    }

    @Test
    fun containsRecognizesBoundsOnItsEdge() {
        val outer = UiBounds(x = 0f, y = 0f, width = 100f, height = 60f)

        assertTrue(outer.contains(UiBounds(x = 0f, y = 0f, width = 100f, height = 60f)))
        assertFalse(outer.contains(UiBounds(x = 90f, y = 20f, width = 20f, height = 20f)))
    }
}
