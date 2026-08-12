// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.rangeSlider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Mirrors `SliderEnabledTest`'s press+move+release drag simulation, but [rangeSlider] has two
 * independently draggable knobs sharing one track -- the behavior worth locking is that a drag
 * claims exactly one knob (the closer one at press time) and leaves the other value untouched,
 * and that `enabled = false` suppresses both.
 */
class RangeSliderTest {

    // Box is offset(20dp,20dp) sized width(160px) x height(20px): track spans x in [30, 170]
    // (knob-radius-inset by 10px each side). start=20/end=80 of [0,100] puts the start knob at
    // x=58 and the end knob at x=142.
    private fun UiContext.drag(
        enabled: Boolean,
        pressX: Float,
        dragToX: Float,
    ): Pair<Float, Float> {
        val input = Input()
        var start = 20f
        var end = 80f
        fun step(pointerDown: Boolean, x: Float) {
            simulateFrame(pointerDown = pointerDown, x = x, y = 30f, input = input) {
                val (s, e) = createAbsolute(x = 20f, y = 20f).rangeSlider(
                    "rs",
                    min = 0f,
                    max = 100f,
                    valueStart = start,
                    valueEnd = end,
                    modifier = Modifier.width(160f.px).height(20f.px),
                    enabled = enabled,
                )
                start = s
                end = e
            }
        }
        step(pointerDown = true, x = pressX)
        step(pointerDown = true, x = dragToX)
        step(pointerDown = false, x = dragToX)
        return start to end
    }

    @Test
    fun draggingTheStartKnobMovesOnlyStartValue() {
        val ui = UiContext()
        val (start, end) = ui.drag(enabled = true, pressX = 58f, dragToX = 100f)
        assertTrue(
            start > 20f,
            "dragging the start knob rightward should raise valueStart: start=$start",
        )
        assertTrue(start < end, "dragged start must not cross the end knob: start=$start end=$end")
        assertEquals(80f, end, "dragging only the start knob must not move valueEnd")
    }

    @Test
    fun draggingTheEndKnobMovesOnlyEndValue() {
        val ui = UiContext()
        val (start, end) = ui.drag(enabled = true, pressX = 142f, dragToX = 100f)
        assertTrue(end < 80f, "dragging the end knob leftward should lower valueEnd: end=$end")
        assertTrue(end > start, "dragged end must not cross the start knob: start=$start end=$end")
        assertEquals(20f, start, "dragging only the end knob must not move valueStart")
    }

    @Test
    fun dragDoesNotMoveEitherValueWhenDisabled() {
        val ui = UiContext()
        val (start, end) = ui.drag(enabled = false, pressX = 58f, dragToX = 100f)
        assertEquals(20f, start, "enabled = false must suppress the start knob's drag")
        assertEquals(80f, end, "enabled = false must suppress the end knob's drag")
    }
}
