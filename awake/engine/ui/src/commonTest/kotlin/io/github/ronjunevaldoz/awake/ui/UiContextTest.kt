// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlin.test.Test
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
}
