package io.github.ronjunevaldoz.awake.ui.api.layout

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupSize
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

    @Test
    fun dimensionContractsRemainRuntimeFree() {
        assertEquals(Dimension.Fixed(12.dp), 12.dp.toDimension())
        assertEquals(Dimension.FillMax, 0.dp.toDimension())
        assertEquals(LayoutWeight(weight = 2f, fill = false), LayoutWeight(2f, fill = false))
    }

    @Test
    fun popupContractsAreRuntimeFree() {
        val provider = UiPopupPositionProvider { _, _, size -> UiBounds(1f, 2f, size.width, size.height) }

        assertEquals(UiBounds(1f, 2f, 30f, 40f), provider.calculatePosition(UiBounds(0f, 0f, 1f, 1f), UiBounds(0f, 0f, 100f, 100f), UiPopupSize(30f, 40f)))
        assertEquals(UiPopupProperties(), UiPopupProperties())
    }
}
