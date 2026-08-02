// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.button
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Mirrors `CheckboxTest`/`SurfaceClickableTest`'s press-then-release-while-still-hovered
 * simulation -- proves [io.github.ronjunevaldoz.awake.ui.unstyled.button]'s new `enabled`
 * param actually suppresses the click, not just its default-true no-op case. */
class ButtonEnabledTest {

    @Test
    fun clickFiresWhenEnabled() {
        val ui = UiContext()
        var clicked = false
        ui.simulateClick(x = 100f, y = 40f, screenHeight = 100f) {
            if (ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
                    .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px))
            ) clicked = true
        }
        assertTrue(clicked, "an otherwise-valid press+release must still click when enabled (the default)")
    }

    @Test
    fun clickDoesNotFireWhenDisabled() {
        val ui = UiContext()
        var clicked = false
        ui.simulateClick(x = 100f, y = 40f, screenHeight = 100f) {
            if (ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp))
                    .button("btn", "Go", modifier = Modifier.width(160f.px).height(40f.px), enabled = false)
            ) clicked = true
        }
        assertFalse(clicked, "enabled = false must suppress the click even on an otherwise valid press+release")
    }
}
