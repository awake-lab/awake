// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textField
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
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertTrue(ui.isFocused("field"), "clicking a text field must grant it focus")

        input.pushTypedText("abc")
        ui.simulateFrame(pointerDown = false, x = 100f, y = 20f, screenHeight = 100f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertEquals("abc", value, "typed text must insert while the field is focused")
    }

    @Test
    fun backspaceDeletesTheCharacterBeforeTheCursor() {
        val ui = UiContext()
        val input = Input()
        var value = "abc"
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 100f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }

        input.pushEditAction(TextEditAction.Backspace)
        ui.simulateFrame(pointerDown = false, x = 100f, y = 20f, screenHeight = 100f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertEquals("ab", value, "backspace must remove the character immediately before the cursor")
    }

    @Test
    fun clickingOutsideTheFieldClearsFocus() {
        val ui = UiContext()
        val input = Input()
        var value = "hello"
        ui.simulateClick(x = 100f, y = 20f, screenHeight = 200f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertTrue(ui.isFocused("field"))

        ui.simulateClick(x = 100f, y = 150f, screenHeight = 200f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertFalse(ui.isFocused("field"), "a fresh click outside every focusable widget must clear focus")

        input.pushTypedText("ignored")
        ui.simulateFrame(pointerDown = false, x = 100f, y = 150f, screenHeight = 200f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertEquals("hello", value, "typed text must be ignored once focus has moved elsewhere")
    }

    @Test
    fun arrowLeftThenInsertPlacesTextBeforeTheLastCharacter() {
        val ui = UiContext()
        val input = Input()
        var value = "ac"
        ui.simulateClick(x = 176f, y = 20f, screenHeight = 100f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }

        input.pushEditAction(TextEditAction.ArrowLeft)
        input.pushTypedText("b")
        ui.simulateFrame(pointerDown = false, x = 176f, y = 20f, screenHeight = 100f, input = input) {
            value = ui.createAbsolute(20f, 20f, font = BitmapFont()).textField("field", value, modifier = UiModifier().width(160f.px).height(36f.px))
        }
        assertEquals("abc", value, "ArrowLeft must move the cursor back one position before the next insert")
    }
}
