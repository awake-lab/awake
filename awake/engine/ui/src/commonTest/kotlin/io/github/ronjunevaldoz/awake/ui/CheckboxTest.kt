// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
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
            checked = ui.absolute(20f, 20f).checkbox("cb", checked, 160f, 40f, label = "ENABLED")
        }
        assertTrue(checked, "clicking anywhere in the row must flip the checkbox, same as a real checkbox's clickable row")
    }

    @Test
    fun checkboxEmitsASeparateBoxAndLabelNotOneBigFill() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(200f, 100f)
        ui.absolute(20f, 20f, font = BitmapFont()).checkbox("cb", checked = false, width = 160f, height = 40f, label = "ENABLED")
        val primitives = ui.endFrame()

        val quads = primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(5, quads.size, "unchecked box draws the box quad plus its 4 border edge quads, not a full-row fill")
        assertTrue(quads[0].w < 160f, "the box quad must be far narrower than the row -- proves it's a small box, not the whole row filled")

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        assertTrue(glyphs.isNotEmpty(), "label must still render its own glyphs")
    }

    @Test
    fun checkedBoxAddsAnInsetAccentQuad() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(200f, 100f)
        ui.absolute(20f, 20f).checkbox("cb", checked = true, width = 160f, height = 40f)
        val quads = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(6, quads.size, "checked state adds one inset accent quad on top of the box quad plus its 4 border edge quads")
    }

    @Test
    fun modifierClipMakesTheBoxARoundedQuad() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(200f, 100f)
        ui.absolute(20f, 20f).checkbox("cb", checked = false, width = 160f, height = 40f, modifier = UiModifier().clip(UiShape.sm))
        val primitive = ui.endFrame().first()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitive, "modifier.clip() must round the checkbox's own box, not just buttons/panels")
    }
}
