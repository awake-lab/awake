// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiContextTest {

    @Test
    fun toggleFlipsOnPressReleaseInsideBounds() {
        val ui = UiContext()

        // Frame 1: pointer moves over the button, not yet pressed.
        Input.setPointer(down = false, x = 60f, y = 40f)
        ui.beginFrame(200f, 100f)
        var checked = ui.toggle("t", 20f, 20f, 120f, 40f, false)
        ui.endFrame()
        assertFalse(checked, "should not toggle on hover alone")

        // Frame 2: press down while hovered.
        Input.setPointer(down = true, x = 60f, y = 40f)
        ui.beginFrame(200f, 100f)
        checked = ui.toggle("t", 20f, 20f, 120f, 40f, checked)
        ui.endFrame()
        assertFalse(checked, "should not toggle on press alone (fires on release)")

        // Frame 3: release while still hovered -- click fires here.
        Input.setPointer(down = false, x = 60f, y = 40f)
        ui.beginFrame(200f, 100f)
        checked = ui.toggle("t", 20f, 20f, 120f, 40f, checked)
        ui.endFrame()
        assertTrue(checked, "should toggle on press+release inside bounds")
    }

    @Test
    fun buttonDoesNotFireWhenPointerOutsideBounds() {
        val ui = UiContext()
        Input.setPointer(down = true, x = 5f, y = 5f)
        ui.beginFrame(200f, 100f)
        ui.button("b", 20f, 20f, 120f, 40f)
        ui.endFrame()

        Input.setPointer(down = false, x = 5f, y = 5f)
        ui.beginFrame(200f, 100f)
        val clicked = ui.button("b", 20f, 20f, 120f, 40f)
        ui.endFrame()
        assertFalse(clicked, "click outside the widget's bounds must not register")
    }

    @Test
    fun sliderValueFromPointerXMapsTrackEdgesAndMidpoint() {
        // Track spans x=[20, 120] (w=100), value range [0, 10].
        assertEquals(0f, sliderValueFromPointerX(20f, trackX = 20f, trackW = 100f, min = 0f, max = 10f))
        assertEquals(10f, sliderValueFromPointerX(120f, trackX = 20f, trackW = 100f, min = 0f, max = 10f))
        assertEquals(5f, sliderValueFromPointerX(70f, trackX = 20f, trackW = 100f, min = 0f, max = 10f))
    }

    @Test
    fun sliderValueFromPointerXClampsOutsideTrackBounds() {
        assertEquals(
            0f,
            sliderValueFromPointerX(-50f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
            "pointer left of the track must clamp to min, not extrapolate negative"
        )
        assertEquals(
            10f,
            sliderValueFromPointerX(500f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
            "pointer right of the track must clamp to max, not extrapolate past it"
        )
    }

    @Test
    fun sliderDragUpdatesValueWhilePointerHeldInsideTrack() {
        val ui = UiContext()

        // Press inside the slider's track -- latches activeId, no drag yet on this frame's
        // pointer position (matches button's own press-edge semantics).
        Input.setPointer(down = true, x = 20f, y = 30f)
        ui.beginFrame(200f, 100f)
        var value = ui.slider("vol", 20f, 20f, 100f, 20f, min = 0f, max = 10f, value = 0f)
        ui.endFrame()

        // Drag to the track's midpoint while still held.
        Input.setPointer(down = true, x = 70f, y = 30f)
        ui.beginFrame(200f, 100f)
        value = ui.slider("vol", 20f, 20f, 100f, 20f, min = 0f, max = 10f, value = value)
        ui.endFrame()
        assertEquals(5f, value, "dragging to the track midpoint should map to the midpoint value")

        // Release -- value should hold at whatever it last was.
        Input.setPointer(down = false, x = 70f, y = 30f)
        ui.beginFrame(200f, 100f)
        value = ui.slider("vol", 20f, 20f, 100f, 20f, min = 0f, max = 10f, value = value)
        ui.endFrame()
        assertEquals(5f, value, "releasing should not further change the value")
    }
}
