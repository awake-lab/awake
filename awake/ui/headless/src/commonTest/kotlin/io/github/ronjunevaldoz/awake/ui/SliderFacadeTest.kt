// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.headless.slider
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.slider] driven only
 * through the public `UiScope` API -- no `headless.internal.*` import, unlike `SliderEnabledTest`.
 */
class SliderFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            slider(
                id = "slider.smoke",
                min = 0f,
                max = 100f,
                value = 50f,
                modifier = Modifier.width(160f.dp).height(20f.dp),
            )
        }

        val bounds = frame.bounds("slider.smoke")
        assertEquals(160f, bounds.width)
    }

    @Test
    fun clickingNearTheRightEdgeRaisesTheValue() {
        var value = 50f
        uiTestSession(width = 200f, height = 100f) {
            val initial = frame {
                value = slider(
                    id = "slider.smoke",
                    min = 0f,
                    max = 100f,
                    value = value,
                    modifier = Modifier.width(160f.dp).height(20f.dp),
                )
            }
            val bounds = initial.bounds("slider.smoke")

            click(bounds.x + bounds.width - 2f, bounds.y + bounds.height / 2f) {
                value = slider(
                    id = "slider.smoke",
                    min = 0f,
                    max = 100f,
                    value = value,
                    modifier = Modifier.width(160f.dp).height(20f.dp),
                )
            }
        }

        assertTrue(value > 50f, "clicking near the track's right end must raise the value, was $value")
    }
}
