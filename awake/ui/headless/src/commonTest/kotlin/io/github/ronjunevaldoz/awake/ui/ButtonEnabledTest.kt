// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.Key
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Mirrors `CheckboxTest`/`SurfaceClickableTest`'s press-then-release-while-still-hovered
 * simulation -- proves [io.github.ronjunevaldoz.awake.ui.headless.button]'s new `enabled`
 * param actually suppresses the click, not just its default-true no-op case. */
class ButtonEnabledTest {

    @Test
    fun clickFiresWhenEnabled() {
        var clicked = false
        uiTestSession(width = 200f, height = 100f) {
            click(100f, 40f) {
                if (primitive.context.createAbsolute(x = 20f, y = 20f)
                        .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px))
                ) clicked = true
            }
        }
        assertTrue(clicked, "an otherwise-valid press+release must still click when enabled (the default)")
    }

    @Test
    fun clickDoesNotFireWhenDisabled() {
        var clicked = false
        uiTestSession(width = 200f, height = 100f) {
            click(100f, 40f) {
                if (primitive.context.createAbsolute(x = 20f, y = 20f)
                        .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px), enabled = false)
                ) clicked = true
            }
        }
        assertFalse(clicked, "enabled = false must suppress the click even on an otherwise valid press+release")
    }

    @Test
    fun spaceActivatesTheButtonAClickAlreadyFocused() {
        var clicks = 0
        uiTestSession(width = 200f, height = 100f) {
            click(100f, 40f) {
                if (primitive.context.createAbsolute(x = 20f, y = 20f)
                        .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px))
                ) clicks++
            }
            assertEquals(1, clicks, "the pointer click should fire once and focus the button")

            val input = Input().apply { setKeyDown(Key.Space, true) }
            frame(input.updateSnapshot().toUiInputState()) {
                if (primitive.context.createAbsolute(x = 20f, y = 20f)
                        .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px))
                ) clicks++
            }
        }

        assertEquals(
            2,
            clicks,
            "Space must re-activate the already-focused button even with the pointer away from it",
        )
    }
}
