// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.switch
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Mirrors `CheckboxTest`'s press-then-release simulation -- proves
 * [switch]'s new `enabled` param
 * actually suppresses the toggle, not just its default-true no-op case. */
class SwitchEnabledTest {

    @Test
    fun toggleFlipsWhenEnabled() {
        var checked = false
        // The switch's own slot is offset(20dp,20dp) sized width(40px) x height(22px) --
        // click inside that box (not CheckboxTest's whole-row target), so (30, 30).
        uiTestSession(width = 200f, height = 100f) {
            click(30f, 30f) {
                checked = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .switch("sw", checked, modifier = Modifier.width(40f.px).height(22f.px))
            }
        }
        assertTrue(
            checked,
            "an otherwise-valid press+release must still flip the switch when enabled (the default)",
        )
    }

    @Test
    fun toggleDoesNotFlipWhenDisabled() {
        var checked = false
        uiTestSession(width = 200f, height = 100f) {
            click(30f, 30f) {
                checked = primitive.context.createAbsolute(x = 20f, y = 20f)
                    .switch(
                        "sw",
                        checked,
                        modifier = Modifier.width(40f.px).height(22f.px),
                        enabled = false,
                    )
            }
        }
        assertFalse(
            checked,
            "enabled = false must suppress the flip even on an otherwise valid press+release",
        )
    }
}
