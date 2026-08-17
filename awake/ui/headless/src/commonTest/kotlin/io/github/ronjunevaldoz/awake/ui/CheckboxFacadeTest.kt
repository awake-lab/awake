// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.checkbox
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.checkbox] driven
 * only through the public `UiScope` API -- no `headless.internal.*` import, unlike `CheckboxTest`.
 */
class CheckboxFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            checkbox(
                id = "checkbox.smoke",
                checked = false,
                boxSize = 16f.dp,
                modifier = Modifier.width(160f.dp).height(24f.dp),
            )
        }

        val bounds = frame.bounds("checkbox.smoke")
        assertEquals(160f, bounds.width)
        assertEquals(24f, bounds.height)
    }

    @Test
    fun clickTogglesCheckedState() {
        var checked = false
        uiTestSession(width = 200f, height = 100f) {
            val initial = frame {
                checked = checkbox(
                    id = "checkbox.smoke",
                    checked = checked,
                    boxSize = 16f.dp,
                    modifier = Modifier.width(160f.dp).height(24f.dp),
                )
            }
            val bounds = initial.bounds("checkbox.smoke")

            click(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f) {
                checked = checkbox(
                    id = "checkbox.smoke",
                    checked = checked,
                    boxSize = 16f.dp,
                    modifier = Modifier.width(160f.dp).height(24f.dp),
                )
            }
        }

        assertTrue(checked, "clicking an unchecked checkbox must check it")
    }
}
