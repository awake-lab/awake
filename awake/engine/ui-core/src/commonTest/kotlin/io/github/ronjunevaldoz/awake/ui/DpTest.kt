// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

class DpTest {

    private val originalScale = 1f
    private val originalFontScale = 1f

    @AfterTest
    fun resetScale() {
        UiDensity.scale = originalScale
        UiDensity.fontScale = originalFontScale
    }

    @Test
    fun toPxScalesByDensity() {
        UiDensity.scale = 1f
        assertEquals(1f, 1f.dp.toPx())

        UiDensity.scale = 2f
        assertEquals(2f, 1f.dp.toPx(), "1.dp at scale 2f must resolve to 2px")

        UiDensity.scale = 3f
        assertEquals(30f, 10f.dp.toPx(), "10.dp at scale 3f must resolve to 30px")

        UiDensity.scale = originalScale
    }

    @Test
    fun pxRoundTripsRegardlessOfDensity() {
        UiDensity.scale = 2f
        assertEquals(280f, 280f.px.toPx(), "a raw-pixel value wrapped via .px must round-trip to the exact same pixel count regardless of density scale")
        UiDensity.scale = originalScale
    }

    @Test
    fun spScalesByDensityAndFontScale() {
        UiDensity.scale = 1f
        UiDensity.fontScale = 1f
        assertEquals(14f, 14f.sp.toPx())

        UiDensity.scale = 2f
        UiDensity.fontScale = 1f
        assertEquals(28f, 14f.sp.toPx(), "sp should track density the same way dp does")

        UiDensity.scale = 2f
        UiDensity.fontScale = 1.25f
        assertEquals(35f, 14f.sp.toPx(), "sp should also honor user font scale")
    }

    @Test
    fun glyphResolutionHonorsStyledTextSizesBelowBitmapCellSize() {
        UiDensity.scale = 1f
        UiDensity.fontScale = 1f
        val font = BitmapFont()
        val ui = UiContext()
        ui.beginFrame(120f, 80f, testSnapshot())

        val glyphPx = ui.absolute(0f, 0f, font = font, theme = CoreUiTheme).resolveGlyphPx(font, textSize = 10.sp)

        assertEquals(10f, glyphPx, "styled text sizes should not be clamped back up to the bitmap cell size")
    }

    @Test
    fun glyphResolutionSnapsStyledTextSizesToWholePixels() {
        UiDensity.scale = 1f
        UiDensity.fontScale = 1.1f
        val font = BitmapFont()
        val ui = UiContext()
        ui.beginFrame(120f, 80f, testSnapshot())

        val glyphPx = ui.absolute(0f, 0f, font = font, theme = CoreUiTheme).resolveGlyphPx(font, textSize = 13f.sp)

        assertEquals(14f, glyphPx, "styled text sizes should snap to whole device pixels for crisper bitmap text")
    }

    @Test
    fun uiShapeOffsetsScaleWithDensity() {
        UiShape.base = 8f.dp
        UiDensity.scale = 1f
        assertEquals(4f, UiShape.sm.toPx())
        assertEquals(6f, UiShape.md.toPx())
        assertEquals(8f, UiShape.lg.toPx())
        assertEquals(12f, UiShape.xl.toPx())

        UiDensity.scale = 2f
        // Offsets happen in Dp-space (base - 4f, etc) BEFORE conversion, so doubling density
        // must double every derived value proportionally -- not just add a flat px amount.
        assertEquals(8f, UiShape.sm.toPx())
        assertEquals(12f, UiShape.md.toPx())
        assertEquals(16f, UiShape.lg.toPx())
        assertEquals(24f, UiShape.xl.toPx())

        UiDensity.scale = originalScale
        UiShape.base = 8f.dp
    }
}
