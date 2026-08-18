// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.toggle
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.toggle] driven only
 * through the public `UiScope` API. No existing facade-level test file owns plain `toggle` (as
 * opposed to `toggleGroup`, covered by the internal-level `ToggleGroupWidgetTest`).
 */
class ToggleFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            toggle(
                id = "toggle.smoke",
                checked = false,
                label = "Bold",
                modifier = Modifier.width(120f.dp).height(32f.dp),
            )
        }

        val bounds = frame.bounds("toggle.smoke")
        assertEquals(120f, bounds.width)
        assertEquals(32f, bounds.height)
    }

    @Test
    fun clickTogglesCheckedState() {
        var checked = false
        uiTestSession(width = 200f, height = 100f) {
            val initial = frame {
                checked = toggle(
                    id = "toggle.smoke",
                    checked = checked,
                    label = "Bold",
                    modifier = Modifier.width(120f.dp).height(32f.dp),
                )
            }
            val bounds = initial.bounds("toggle.smoke")

            click(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f) {
                checked = toggle(
                    id = "toggle.smoke",
                    checked = checked,
                    label = "Bold",
                    modifier = Modifier.width(120f.dp).height(32f.dp),
                )
            }
        }

        assertTrue(checked, "clicking an unchecked toggle must check it")
    }
}
