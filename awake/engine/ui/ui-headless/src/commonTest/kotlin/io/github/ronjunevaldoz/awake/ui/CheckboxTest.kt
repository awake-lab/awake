// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckboxTest {

    @Test
    fun checkboxFlipsOnPressReleaseInsideBounds() {
        val ui = UiContext()
        var checked = false
        // Whole row is the hit target, not just the small box -- click somewhere in the
        // middle of the row (x=100), not at the box's own tiny x=0..16 range.
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 100f) {
            checked = ui.createAbsolute(x = 20f, y = 20f).checkbox("cb", checked, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px))
        }
        assertTrue(checked, "clicking anywhere in the row must flip the checkbox, same as a real checkbox's clickable row")
    }

    @Test
    fun checkboxEmitsASeparateBoxAndLabelNotOneBigFill() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.pushFont(BitmapFont())
        ui.createAbsolute(x = 20f, y = 20f).checkbox("cb", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px))
        val primitives = ui.endFrame()

        val quads = primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(5, quads.size, "unchecked box draws the box quad plus its 4 border edge quads, not a full-row fill")
        assertTrue(quads[0].w < 160f, "the box quad must be far narrower than the row -- proves it's a small box, not the whole row filled")

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "label must still render its own glyphs")
    }

    @Test
    fun checkboxCanUseModifierSizingAsPrimaryApi() {
        val ui = UiContext()
        ui.beginFrame(220f, 100f, testSnapshot())
        ui.pushFont(BitmapFont())
        ui.createAbsolute(x = 20f, y = 20f).checkbox(
            id = "cb",
            checked = false,
            label = "ENABLED",
            modifier = Modifier.width(160f.px).height(40f.px),
        )

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "modifier-sized checkbox should still render its label")
        assertTrue(glyphs.first().x >= 44f, "label should still render to the right of the checkbox box when sizing comes from modifier")
    }

    @Test
    fun checkedBoxAddsAnInsetAccentQuad() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createAbsolute(x = 20f, y = 20f).checkbox("cb", checked = true, modifier = Modifier.width(160f.px).height(40f.px))
        val quads = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(6, quads.size, "checked state adds one inset accent quad on top of the box quad plus its 4 border edge quads")
    }

    @Test
    fun styleShapeMakesTheBoxARoundedQuad() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createAbsolute(x = 20f, y = 20f).checkbox(
            "cb",
            checked = false,
            modifier = Modifier.width(160f.px).height(40f.px),
            style = Style { shape(UiShape.sm) },
        )
        val primitive = ui.endFrame().first()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitive, "style.shape() must round the checkbox's own box, not just buttons/panels")
    }

    @Test
    fun trueFontCheckboxLabelStaysVerticallyCenteredInTheRow() {
        val font = UiFonts.trueSans()
        val ui = UiContext()
        ui.beginFrame(220f, 100f, testSnapshot())
        ui.pushFont(font)
        ui.createAbsolute(x = 20f, y = 20f).checkbox("cb", checked = false, label = "ENABLED", modifier = Modifier.width(160f.px).height(40f.px))

        val glyphBounds = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>().glyphBounds()
        val rowCenterY = 40f
        val glyphCenterY = glyphBounds.y + glyphBounds.height / 2f

        assertTrue(
            kotlin.math.abs(glyphCenterY - rowCenterY) <= 1f,
            "checkbox label should stay vertically centered in its row with the true font: rowCenterY=$rowCenterY glyphCenterY=$glyphCenterY bounds=$glyphBounds",
        )
    }
}

private fun List<UiDrawPrimitive.Glyph>.glyphBounds(): UiBounds {
    require(isNotEmpty()) { "expected at least one glyph primitive" }
    return drop(1).fold(UiBounds(first().x, first().y, first().w, first().h)) { acc, glyph ->
        val minX = minOf(acc.x, glyph.x)
        val minY = minOf(acc.y, glyph.y)
        val maxX = maxOf(acc.x + acc.width, glyph.x + glyph.w)
        val maxY = maxOf(acc.y + acc.height, glyph.y + glyph.h)
        UiBounds(minX, minY, maxX - minX, maxY - minY)
    }
}
