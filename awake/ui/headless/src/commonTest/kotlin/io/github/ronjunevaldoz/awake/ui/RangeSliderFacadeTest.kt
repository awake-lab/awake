// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.rangeSlider
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.rangeSlider] driven
 * only through the public `UiScope` API -- no `headless.internal.*` import, unlike
 * `RangeSliderTest` (which drives `headless.internal.controls.rangeSlider` directly).
 */
class RangeSliderFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            rangeSlider(
                id = "rangeSlider.smoke",
                min = 0f,
                max = 100f,
                valueStart = 20f,
                valueEnd = 80f,
                modifier = Modifier.width(160f.dp).height(20f.dp),
            )
        }

        val bounds = frame.bounds("rangeSlider.smoke")
        assertEquals(160f, bounds.width)
    }

    @Test
    fun draggingNearTheEndKnobRaisesOnlyValueEnd() {
        var start = 20f
        var end = 80f
        uiTestSession(width = 200f, height = 100f) {
            val initial = frame {
                val (s, e) = rangeSlider(
                    id = "rangeSlider.smoke",
                    min = 0f,
                    max = 100f,
                    valueStart = start,
                    valueEnd = end,
                    modifier = Modifier.width(160f.dp).height(20f.dp),
                )
                start = s
                end = e
            }
            val bounds = initial.bounds("rangeSlider.smoke")
            // Matches RangeSlider.kt's own knob-radius inset (half of a 16px knob diameter).
            val trackInset = 8f
            val trackX = bounds.x + trackInset
            val trackWidth = bounds.width - trackInset * 2f
            val endKnobX = trackX + trackWidth * 0.8f
            val endKnobY = bounds.y + bounds.height / 2f

            drag(endKnobX, endKnobY, bounds.x + bounds.width - 2f, endKnobY) {
                val (s, e) = rangeSlider(
                    id = "rangeSlider.smoke",
                    min = 0f,
                    max = 100f,
                    valueStart = start,
                    valueEnd = end,
                    modifier = Modifier.width(160f.dp).height(20f.dp),
                )
                start = s
                end = e
            }
        }

        assertTrue(end > 80f, "dragging the end knob rightward should raise valueEnd, was $end")
        assertEquals(20f, start, "dragging only the end knob must not move valueStart")
    }
}
