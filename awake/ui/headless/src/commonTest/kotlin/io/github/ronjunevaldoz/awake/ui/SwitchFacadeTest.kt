// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.switch
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.switch] driven only
 * through the public `UiScope` API -- no `headless.internal.*` import, unlike `SwitchEnabledTest`.
 */
class SwitchFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            switch(
                id = "switch.smoke",
                checked = false,
                modifier = Modifier.width(120f.dp).height(24f.dp),
            )
        }

        val bounds = frame.bounds("switch.smoke")
        assertEquals(120f, bounds.width)
        assertEquals(24f, bounds.height)
    }

    @Test
    fun clickTogglesCheckedState() {
        var checked = false
        uiTestSession(width = 200f, height = 100f) {
            val initial = frame {
                checked = switch(
                    id = "switch.smoke",
                    checked = checked,
                    modifier = Modifier.width(120f.dp).height(24f.dp),
                )
            }
            val bounds = initial.bounds("switch.smoke")

            click(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f) {
                checked = switch(
                    id = "switch.smoke",
                    checked = checked,
                    modifier = Modifier.width(120f.dp).height(24f.dp),
                )
            }
        }

        assertTrue(checked, "clicking an unchecked switch must check it")
    }
}
