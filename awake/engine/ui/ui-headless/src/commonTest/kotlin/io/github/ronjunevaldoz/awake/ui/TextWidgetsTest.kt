// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.layoutBitmapText
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextWidgetsTest {

    @Test
    fun ellipsisClampsGlyphsInsideTheSlotWidth() {
        val font = BitmapFont()
        val ui = UiContext()
        ui.pushFont(font)
        val scope = ui.createAbsolute(x = 10f, y = 20f)
        // BitmapFont is true monospace (every glyph advances a full glyphPx, see
        // GlyphAtlasSource.advanceFor) -- the slot must be wide enough to fit the 3-dot
        // ellipsis itself (3 * glyphPx) plus at least one real character, or there's no
        // valid truncation to assert on.
        val slotWidthPx = 60f
        val layout = layoutBitmapText(
            label = "TOOLONG",
            glyphPx = 12f,
            maxWidthPx = slotWidthPx,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) },
        )

        scope.text(
            label = "TOOLONG",
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(10f, 20f, slotWidthPx, 12f),
            font = font,
            overflow = UiTextOverflow.Ellipsis,
        )

        val frame = ui.endFrame()
        val clipPushes = frame.filterIsInstance<UiDrawPrimitive.ClipPush>()
        assertTrue(layout.lines.single().endsWith("..."), "ellipsis overflow should append a visible ellipsis when text is truncated")
        assertTrue(layout.lineWidths.single() <= slotWidthPx, "ellipsized text layout must measure within the slot width")
        assertTrue(
            clipPushes.isNotEmpty(),
            "ellipsized text should clip to the slot bounds even when the final glyph quad extends past the right edge",
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
            maxLines = 3,
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
            maxLines = 2,
        )

        assertEquals(listOf("Awake", "wid..."), layout.lines)
        assertTrue(layout.truncated)
    }

    @Test
    fun lineWidthsAreMonospaceAcrossGlyphs() {
        // BitmapFont is true monospace -- every glyph's quad spans a full 1em cell (see
        // BitmapFont.uvFor), so advance must match that width exactly regardless of a
        // character's ink shape (GlyphAtlasSource.advanceFor), or consecutive glyph quads
        // overlap. A narrow character ("i"/"l") and a wide one ("W") must therefore measure
        // to the same line width, not a narrower one.
        val font = BitmapFont()
        val narrow = layoutBitmapText(
            label = "ill",
            glyphPx = 12f,
            maxWidthPx = 120f,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Visible,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) },
        )
        val wide = layoutBitmapText(
            label = "WWW",
            glyphPx = 12f,
            maxWidthPx = 120f,
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Visible,
            maxLines = 1,
            advanceOf = { char -> font.advanceFor(char, 12f) },
        )

        assertEquals(narrow.lineWidths.single(), wide.lineWidths.single())
    }

    @Test
    fun trueFontCentersVisibleGlyphBoundsInsideTheRequestedSlot() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())
        ui.pushFont(font)
        ui.createAbsolute(x = 0f, y = 0f).text(
            label = "BUTTON",
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(20f, 20f, 160f, 40f),
            font = font,
            centered = true,
        )

        val glyphBounds = ui.endFrame().glyphBounds()
        val slotCenterY = 40f
        val glyphCenterY = glyphBounds.centerY()

        assertTrue(
            kotlin.math.abs(glyphCenterY - slotCenterY) <= 1f,
            "true-font text should center its visible glyph bounds inside the slot: slotCenterY=$slotCenterY glyphCenterY=$glyphCenterY bounds=$glyphBounds",
        )
    }

    @Test
    fun trueFontTopAlignmentTrimsAtlasPaddingFromVisibleGlyphs() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(180f, 80f, testSnapshot())
        ui.pushFont(font)
        ui.createAbsolute(x = 0f, y = 0f).text(
            label = "Title",
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(16f, 24f, 120f, 20f),
            font = font,
        )

        val glyphBounds = ui.endFrame().glyphBounds()

        assertTrue(
            kotlin.math.abs(glyphBounds.y - 24f) <= 1f,
            "top-aligned text should align the visible glyph top with the slot top instead of preserving baked atlas padding: bounds=$glyphBounds",
        )
    }

    @Test
    fun buttonCanLeftAlignTextInsideItsContentPadding() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(220f, 100f, testSnapshot())
        ui.pushFont(font)
        ui.createAbsolute(x = 0f, y = 0f).button(
            id = "nav",
            modifier = Modifier.width(180f.px).height(36f.px),
            label = "Overview",
            style = Style {
                contentPadding(start = 14f.dp, top = 0f.dp, end = 14f.dp, bottom = 0f.dp)
            },
            centered = false,
        )

        val glyphBounds = ui.endFrame().glyphBounds()

        assertTrue(
            kotlin.math.abs(glyphBounds.x - 14f) <= 1f,
            "left-aligned button text should start from the padded content edge instead of the button center: bounds=$glyphBounds",
        )
    }

    @Test
    fun hoveredTextKeepsItsOriginalColumnSlot() {
        val ui = UiContext()
        ui.beginFrame(240f, 120f, testSnapshot(x = 20f, y = 12f))

        var firstSlot: UiBounds? = null
        var secondSlot: UiBounds? = null

        ui.column(
            modifier = Modifier.width(200f.dp),
            verticalArrangement = Arrangement.spacedBy(8f.dp),
        ) {
            firstSlot = text("Hover target", modifier = Modifier.width(120f.px))
            secondSlot = text("Sibling", modifier = Modifier.width(120f.px))
        }

        assertEquals(0f, firstSlot?.y, "hovered text should not re-claim a later row in the same column")
        assertEquals(
            (firstSlot?.y ?: 0f) + (firstSlot?.height ?: 0f) + 8f,
            secondSlot?.y,
            "a hovered text widget must not push later siblings further down by claiming a second slot",
        )
    }
}

private fun List<UiDrawPrimitive>.glyphBounds(): UiBounds {
    val glyphs = filterIsInstance<UiDrawPrimitive.Glyph>()
    require(glyphs.isNotEmpty()) { "expected at least one glyph primitive" }
    return glyphs.drop(1).fold(
        UiBounds(
            glyphs.first().x,
            glyphs.first().y,
            glyphs.first().w,
            glyphs.first().h,
        ),
    ) { acc, glyph ->
        acc.union(UiBounds(glyph.x, glyph.y, glyph.w, glyph.h))
    }
}

private fun UiBounds.centerY(): Float = y + height / 2f

private fun UiBounds.union(other: UiBounds): UiBounds {
    val minX = minOf(x, other.x)
    val minY = minOf(y, other.y)
    val maxX = maxOf(x + width, other.x + other.width)
    val maxY = maxOf(y + height, other.y + other.height)
    return UiBounds(minX, minY, maxX - minX, maxY - minY)
}
