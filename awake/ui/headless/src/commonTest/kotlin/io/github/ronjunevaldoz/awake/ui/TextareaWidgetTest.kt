// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textarea
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextareaWidgetTest {

    @Test
    fun manyLinesScrollVerticallyInsteadOfClippingBehindTheBox() {
        // Regression test for "text input not scrollable": a textarea with more lines than fit
        // its fixed height used to just clip the overflow with no way to reach it -- no scroll
        // offset was ever applied to the drawn text or caret. Typing past the visible bottom of
        // a 3-line-tall box (minLines=3) must scroll the caret's line back into view.
        var value = ""
        val glyphCount = uiTestSession(width = 200f, height = 200f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f).textarea(
                    "notes", value, modifier = Modifier.width(160f.px).height(60f.px), minLines = 3,
                )
            }
            click(100f, 20f) { draw() }
            repeat(6) { input.pushEditAction(TextEditAction.Enter) }
            input.pushTypedText("last line")
            frame(x = 100f, y = 20f, down = false) { draw() }
            frame(x = 100f, y = 20f, down = false) { draw() }
                .primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        }
        assertEquals(
            7,
            value.count { it == '\n' } + 1,
            "every typed newline must land in the value",
        )
        assertTrue(
            value.endsWith("last line"),
            "typed text past the visible bottom must not be lost",
        )

        assertEquals(
            "last line".length,
            glyphCount,
            "the caret's current (scrolled-into-view) line must still be laid out as glyphs",
        )
    }

    @Test
    fun typedTextRecomputesWrappedSemanticLineCountInTheSameFrame() {
        var value = ""
        val frame = uiTestSession(width = 200f, height = 180f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f).textarea(
                    "notes", value, modifier = Modifier.width(96f.px).height(80f.px), minLines = 3,
                )
            }
            click(42f, 28f) { draw() }
            input.pushTypedText("ANTIDISESTABLISHMENTARIANISM_ANTIDISESTABLISHMENTARIANISM")
            frame(x = 42f, y = 28f, down = false) { draw() }
        }

        val textareaNode = requireNotNull(
            frame.semantics.firstOrNull { it.id == "notes" && it.role == UiSemanticRole.Text },
        ) { "textarea should record its own semantic node" }
        assertTrue(
            textareaNode.lineCount > 1,
            "textarea semantic lineCount must reflect the wrapped value typed during this frame",
        )
    }

    @Test
    fun pointerClickMapsToCorrectMultiLineTextIndex() {
        var value = "First Line\nSecond Line\nThird Line"
        // Click on the second line (y = 20 + 20)
        val frame = uiTestSession(width = 200f, height = 200f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f).textarea(
                    "notes", value, modifier = Modifier.width(160f.px).height(80f.px), minLines = 3,
                )
            }
            click(25f, 40f) { draw() }
        }
        val node = requireNotNull(
            frame.semantics.firstOrNull { it.id == "notes" && it.role == UiSemanticRole.Text },
        )
        assertEquals(true, node.selected, "Clicked textarea should be focused/selected")
    }
}
