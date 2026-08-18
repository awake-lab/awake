// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.TextEditAction
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textField
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TextFieldWidgetTest {

    @Test
    fun clickingIntoAnEmptyFieldGrantsFocusAndAcceptsTypedText() {
        var value = ""
        uiTestSession(width = 200f, height = 100f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
            }
            click(100f, 20f) { draw() }
            assertTrue(ui.isFocusedInternal("field"), "clicking a text field must grant it focus")
            input.pushTypedText("abc")
            frame(x = 100f, y = 20f, down = false) { draw() }
        }
        assertEquals("abc", value, "typed text must insert while the field is focused")
    }

    @Test
    fun backspaceDeletesTheCharacterBeforeTheCursor() {
        var value = "abc"
        uiTestSession(width = 200f, height = 100f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
            }
            click(100f, 20f) { draw() }
            input.pushEditAction(TextEditAction.Backspace)
            frame(x = 100f, y = 20f, down = false) { draw() }
        }
        assertEquals(
            "ab",
            value,
            "backspace must remove the character immediately before the cursor",
        )
    }

    @Test
    fun clickingOutsideTheFieldClearsFocus() {
        var value = "hello"
        uiTestSession(width = 200f, height = 200f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
            }
            click(100f, 20f) { draw() }
            assertTrue(ui.isFocusedInternal("field"))
            click(100f, 150f) { draw() }
            assertFalse(ui.isFocusedInternal("field"), "a fresh click outside every focusable widget must clear focus")
            input.pushTypedText("ignored")
            frame(x = 100f, y = 150f, down = false) { draw() }
        }
        assertEquals("hello", value, "typed text must be ignored once focus has moved elsewhere")
    }

    @Test
    fun longValueScrollsHorizontallyInsteadOfTruncatingBehindTheClip() {
        // Regression test for "text input not scrollable": a value wider than the field's
        // fixed width used to just clip -- worse, overflow=Clip's own prefix-fit truncation
        // meant only the *first* few characters were ever laid out at all, so typing past the
        // visible width produced glyphs that were silently dropped, not merely off-screen.
        var value = ""
        val longValue = "abcdefghijklmnopqrstuvwxyz0123456789"
        val glyphCount = uiTestSession(width = 200f, height = 100f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .textField("field", value, modifier = Modifier.width(80f.px).height(36f.px))
            }
            click(100f, 20f) { draw() }
            input.pushTypedText(longValue)
            frame(x = 100f, y = 20f, down = false) { draw() }
            // Render one more frame (nothing typed this time) and prove every character of the
            // stored value actually got laid out as a glyph, rather than truncated to its field.
            frame(x = 100f, y = 20f, down = false) { draw() }
                .primitives.filterIsInstance<UiDrawPrimitive.Glyph>().size
        }
        assertEquals(
            longValue,
            value,
            "typed characters past the visible field width must not be lost",
        )

        assertEquals(
            longValue.length,
            glyphCount,
            "every character of a scrolled-past-view value must still be laid out, not clipped away by truncation",
        )
    }

    @Test
    fun arrowLeftThenInsertPlacesTextBeforeTheLastCharacter() {
        var value = "ac"
        uiTestSession(width = 200f, height = 100f, font = BitmapFont()) {
            fun UiScope.draw() {
                value = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .textField("field", value, modifier = Modifier.width(160f.px).height(36f.px))
            }
            click(176f, 20f) { draw() }
            input.pushEditAction(TextEditAction.ArrowLeft)
            input.pushTypedText("b")
            frame(x = 176f, y = 20f, down = false) { draw() }
        }
        assertEquals(
            "abc",
            value,
            "ArrowLeft must move the cursor back one position before the next insert",
        )
    }
}
