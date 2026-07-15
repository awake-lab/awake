// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextWidgetsTest {

    @Test
    fun ellipsisClampsGlyphsInsideTheSlotWidth() {
        val font = BitmapFont()
        val ui = UiContext()
        val scope = ui.absolute(x = 10f, y = 20f, font = font)

        scope.text(
            label = "TOOLONG",
            slot = UiSlot(10f, 20f, 32f, 8f),
            font = font,
            overflow = UiTextOverflow.Ellipsis
        )

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertEquals(4, glyphs.size, "32px at an 8px glyph size must fit exactly four glyph quads after ellipsis")
        assertTrue(glyphs.all { it.x + it.w <= 42f }, "ellipsized glyphs must stay inside the slot width")
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
}
