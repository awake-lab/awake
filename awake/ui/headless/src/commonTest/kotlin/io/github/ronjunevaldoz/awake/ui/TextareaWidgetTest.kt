// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textarea
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
        val ui = UiContext()
        val input = Input()
        var value = ""
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 200f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textarea(
                    "notes",
                    value,
                    modifier = Modifier.width(160f.px).height(60f.px),
                    minLines = 3,
                )
        }

        // 6 newlines -> 7 lines, well beyond the 3-line-tall box.
        repeat(6) { input.pushEditAction(TextEditAction.Enter) }
        input.pushTypedText("last line")
        ui.simulateFrame(
            pointerDown = false,
            x = 100f,
            y = 20f,
            screenHeight = 200f,
            input = input,
        ) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textarea(
                    "notes",
                    value,
                    modifier = Modifier.width(160f.px).height(60f.px),
                    minLines = 3,
                )
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

        // Render once more and prove the caret's final line actually got laid out as glyphs --
        // not left clipped out by a box that never scrolled to follow it.
        input.setPointer(down = false, x = 100f, y = 20f)
        ui.beginFrame(200f, 200f, input.updateSnapshot().toUiInputState())
        ui.pushFont(BitmapFont())
        value = ui.createAbsolute(x = 20f, y = 20f)
            .textarea(
                "notes",
                value,
                modifier = Modifier.width(160f.px).height(60f.px),
                minLines = 3,
            )
        val glyphCount = ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        assertEquals(
            "last line".length,
            glyphCount,
            "the caret's current (scrolled-into-view) line must still be laid out as glyphs",
        )
    }

    @Test
    fun typedTextRecomputesWrappedSemanticLineCountInTheSameFrame() {
        val ui = UiContext()
        val input = Input()
        var value = ""
        ui.simulateClick(x = 42f, y = 28f, screenHeight = 180f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textarea(
                    "notes",
                    value,
                    modifier = Modifier.width(96f.px).height(80f.px),
                    minLines = 3,
                )
        }

        input.pushTypedText("ANTIDISESTABLISHMENTARIANISM_ANTIDISESTABLISHMENTARIANISM")
        ui.simulateFrame(
            pointerDown = false,
            x = 42f,
            y = 28f,
            screenHeight = 180f,
            input = input,
        ) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textarea(
                    "notes",
                    value,
                    modifier = Modifier.width(96f.px).height(80f.px),
                    minLines = 3,
                )
        }

        val textareaNode = requireNotNull(
            ui.semanticNodes().firstOrNull { it.id == "notes" && it.role == UiSemanticRole.Text },
        ) { "textarea should record its own semantic node" }
        assertTrue(
            textareaNode.lineCount > 1,
            "textarea semantic lineCount must reflect the wrapped value typed during this frame",
        )
    }
}
