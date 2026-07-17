// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextWidgetsTest {

    @Test
    fun ellipsisClampsGlyphsInsideTheSlotWidth() {
        val font = BitmapFont()
        val ui = UiContext()
        val scope = ui.absolute(x = 10f, y = 20f, font = font)
        val layout = layoutBitmapText(
            label = "TOOLONG",
            glyphPx = 12f,
            maxWidthPx = 32f,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) }
        )

        scope.text(
            label = "TOOLONG",
            slot = UiSlot(10f, 20f, 32f, 12f),
            font = font,
            overflow = UiTextOverflow.Ellipsis
        )

        val frame = ui.endFrame()
        val clipPushes = frame.filterIsInstance<UiDrawPrimitive.ClipPush>()
        assertTrue(layout.lines.single().endsWith("..."), "ellipsis overflow should append a visible ellipsis when text is truncated")
        assertTrue(layout.lineWidths.single() <= 32f, "ellipsized text layout must measure within the slot width")
        assertTrue(
            clipPushes.isNotEmpty(),
            "ellipsized text should clip to the slot bounds even when the final glyph quad extends past the right edge"
        )
    }

    @Test
    fun wordWrapSplitsTextAcrossMultipleLines() {
        val layout = layoutBitmapText(
            label = "Awake widget system",
            glyphPx = 8f,
            maxWidthPx = 48f,
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Clip,
            maxLines = 3
        )

        assertEquals(listOf("Awake", "widget", "system"), layout.lines)
        assertTrue(!layout.truncated)
    }

    @Test
    fun wordWrapEllipsizesTheFinalVisibleLineWhenMaxLinesAreExceeded() {
        val layout = layoutBitmapText(
            label = "Awake widget system proves overflow",
            glyphPx = 8f,
            maxWidthPx = 48f,
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 2
        )

        assertEquals(listOf("Awake", "wid..."), layout.lines)
        assertTrue(layout.truncated)
    }

    @Test
    fun lineWidthsReflectVariableGlyphAdvance() {
        val font = BitmapFont()
        val narrow = layoutBitmapText(
            label = "ill",
            glyphPx = 12f,
            maxWidthPx = 120f,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Visible,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) }
        )
        val wide = layoutBitmapText(
            label = "WWW",
            glyphPx = 12f,
            maxWidthPx = 120f,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Visible,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) }
        )

        assertTrue(narrow.lineWidths.single() < wide.lineWidths.single())
    }

    @Test
    fun trueFontCentersVisibleGlyphBoundsInsideTheRequestedSlot() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(200f, 80f)
        ui.absolute(0f, 0f, font = font).text(
            label = "BUTTON",
            slot = UiSlot(20f, 20f, 160f, 40f),
            font = font,
            centered = true
        )

        val glyphBounds = ui.endFrame().glyphBounds()
        val slotCenterY = 40f
        val glyphCenterY = glyphBounds.centerY()

        assertTrue(
            kotlin.math.abs(glyphCenterY - slotCenterY) <= 1f,
            "true-font text should center its visible glyph bounds inside the slot: slotCenterY=$slotCenterY glyphCenterY=$glyphCenterY bounds=$glyphBounds"
        )
    }

    @Test
    fun trueFontTopAlignmentTrimsAtlasPaddingFromVisibleGlyphs() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(180f, 80f)
        ui.absolute(0f, 0f, font = font).text(
            label = "Title",
            slot = UiSlot(16f, 24f, 120f, 20f),
            font = font
        )

        val glyphBounds = ui.endFrame().glyphBounds()

        assertTrue(
            kotlin.math.abs(glyphBounds.y - 24f) <= 1f,
            "top-aligned text should align the visible glyph top with the slot top instead of preserving baked atlas padding: bounds=$glyphBounds"
        )
    }
}

private fun List<UiDrawPrimitive>.glyphBounds(): UiSlot {
    val glyphs = filterIsInstance<UiDrawPrimitive.Glyph>()
    require(glyphs.isNotEmpty()) { "expected at least one glyph primitive" }
    return glyphs.drop(1).fold(UiSlot(glyphs.first().x, glyphs.first().y, glyphs.first().w, glyphs.first().h)) { acc, glyph ->
        acc.union(UiSlot(glyph.x, glyph.y, glyph.w, glyph.h))
    }
}

private fun UiSlot.centerY(): Float = y + height / 2f

private fun UiSlot.union(other: UiSlot): UiSlot {
    val minX = minOf(x, other.x)
    val minY = minOf(y, other.y)
    val maxX = maxOf(x + width, other.x + other.width)
    val maxY = maxOf(y + height, other.y + other.height)
    return UiSlot(minX, minY, maxX - minX, maxY - minY)
}
