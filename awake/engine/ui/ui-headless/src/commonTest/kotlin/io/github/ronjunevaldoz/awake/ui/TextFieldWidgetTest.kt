// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.input.text.textField
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextFieldWidgetTest {

    @Test
    fun clickingIntoAnEmptyFieldGrantsFocusAndAcceptsTypedText() {
        val ui = UiContext()
        val input = Input()
        var value = ""
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertTrue(ui.isFocused("field"), "clicking a text field must grant it focus")

        input.pushTypedText("abc")
        ui.simulateFrame(pointerDown = false, x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertEquals("abc", value, "typed text must insert while the field is focused")
    }

    @Test
    fun backspaceDeletesTheCharacterBeforeTheCursor() {
        val ui = UiContext()
        val input = Input()
        var value = "abc"
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }

        input.pushEditAction(TextEditAction.Backspace)
        ui.simulateFrame(pointerDown = false, x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertEquals("ab", value, "backspace must remove the character immediately before the cursor")
    }

    @Test
    fun clickingOutsideTheFieldClearsFocus() {
        val ui = UiContext()
        val input = Input()
        var value = "hello"
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 200f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertTrue(ui.isFocused("field"))

        ui.simulateClick(x = 100f, y = 150f, screenHeight = 200f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertFalse(ui.isFocused("field"), "a fresh click outside every focusable widget must clear focus")

        input.pushTypedText("ignored")
        ui.simulateFrame(pointerDown = false, x = 100f, y = 150f, screenHeight = 200f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertEquals("hello", value, "typed text must be ignored once focus has moved elsewhere")
    }

    @Test
    fun longValueScrollsHorizontallyInsteadOfTruncatingBehindTheClip() {
        // Regression test for "text input not scrollable": a value wider than the field's
        // fixed width used to just clip -- worse, overflow=Clip's own prefix-fit truncation
        // meant only the *first* few characters were ever laid out at all, so typing past the
        // visible width produced glyphs that were silently dropped, not merely off-screen.
        val ui = UiContext()
        val input = Input()
        var value = ""
        val longValue = "abcdefghijklmnopqrstuvwxyz0123456789"
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textField("field", value, modifier = Modifier.width(80f.px).height(36f.px))
        }
        input.pushTypedText(longValue)
        ui.simulateFrame(pointerDown = false, x = 100f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f)
                .textField("field", value, modifier = Modifier.width(80f.px).height(36f.px))
        }
        assertEquals(longValue, value, "typed characters past the visible field width must not be lost")

        // Render one more frame (nothing typed this time) and prove every character of the
        // stored value actually got laid out as a glyph -- not truncated to whatever prefix
        // fit the field's own narrow width.
        input.setPointer(down = false, x = 100f, y = 20f)
        ui.beginFrame(200f, 100f, input.updateSnapshot().toUiInputState())
        ui.pushFont(BitmapFont())
        value = ui.createAbsolute(x = 20f, y = 20f)
            .textField("field", value, modifier = Modifier.width(80f.px).height(36f.px))
        val glyphCount = ui.finishFrame().primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        assertEquals(
            longValue.length,
            glyphCount,
            "every character of a scrolled-past-view value must still be laid out, not clipped away by truncation",
        )
    }

    @Test
    fun arrowLeftThenInsertPlacesTextBeforeTheLastCharacter() {
        val ui = UiContext()
        val input = Input()
        var value = "ac"
        ui.simulateClick(x = 176f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }

        input.pushEditAction(TextEditAction.ArrowLeft)
        input.pushTypedText("b")
        ui.simulateFrame(pointerDown = false, x = 176f, y = 20f, screenHeight = 100f, input = input) {
            ui.pushFont(BitmapFont())
            value = ui.createAbsolute(x = 20f, y = 20f).textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
        }
        assertEquals("abc", value, "ArrowLeft must move the cursor back one position before the next insert")
    }
}
