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
        var checked = false
        ui.simulateFrame(pointerDown = false, x = 60f, y = 40f, screenHeight = 100f) {
            checked = ui.absolute(20f, 20f).toggle("t", checked, 120f, 40f)
        }
        assertFalse(checked, "should not toggle on hover alone")

        // Press+release while hovered -- click fires on the release frame.
        ui.simulateClick(x = 60f, y = 40f, screenHeight = 100f) {
            checked = ui.absolute(20f, 20f).toggle("t", checked, 120f, 40f)
        }
        assertTrue(checked, "should toggle on press+release inside bounds")
    }

    @Test
    fun buttonDoesNotFireWhenPointerOutsideBounds() {
        val ui = UiContext()
        var clicked = false
        ui.simulateClick(x = 5f, y = 5f, screenHeight = 100f) {
            clicked = ui.absolute(20f, 20f).button("b", 120f, 40f)
        }
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
        var value = ui.absolute(20f, 20f).slider("vol", min = 0f, max = 10f, value = 0f, width = 100f, height = 20f)
        ui.endFrame()

        // Drag to the track's midpoint while still held.
        Input.setPointer(down = true, x = 70f, y = 30f)
        ui.beginFrame(200f, 100f)
        value = ui.absolute(20f, 20f).slider("vol", min = 0f, max = 10f, value = value, width = 100f, height = 20f)
        ui.endFrame()
        assertEquals(5f, value, "dragging to the track midpoint should map to the midpoint value")

        // Release -- value should hold at whatever it last was.
        Input.setPointer(down = false, x = 70f, y = 30f)
        ui.beginFrame(200f, 100f)
        value = ui.absolute(20f, 20f).slider("vol", min = 0f, max = 10f, value = value, width = 100f, height = 20f)
        ui.endFrame()
        assertEquals(5f, value, "releasing should not further change the value")
    }

    @Test
    fun dropdownOptionsAlwaysPaintAfterSiblingWidgetsRegardlessOfCallOrder() {
        // Regression test for the dropdown-overlap bug: even when a sibling widget is drawn
        // in the SAME frame AFTER the dropdown's expanded options, the options must still end
        // up later in endFrame()'s output (painted on top), since they go through
        // emitOverlay() rather than emit().
        val ui = UiContext()
        Input.setPointer(down = false, x = 0f, y = 0f)
        ui.beginFrame(200f, 200f)
        val column = ui.column(20f, 20f, 160f)

        // Expand the dropdown first (simulating it was already expanded from a prior frame).
        val expandedState = column.widgetState("dd")
        expandedState.set("expanded", true)
        column.dropdown("dd", listOf("A", "B"), selectedIndex = 0, width = 160f, height = 32f)

        // A sibling widget drawn AFTER the dropdown in this same frame, at a position that
        // would spatially overlap the dropdown's first expanded option row.
        column.toggle("sibling", false, 160f, 32f)

        val allPrimitives = ui.endFrame()
        val overlayQuadCount = 2 // header quad isn't overlay; only the 2 option rows are
        val lastPrimitives = allPrimitives.takeLast(overlayQuadCount)
        assertTrue(lastPrimitives.all { it is UiDrawPrimitive.Quad }, "expected the dropdown's option quads to be the last primitives emitted")
    }

    @Test
    fun dropdownCollapsesWhenClickLandsOutsideHeaderAndOptions() {
        // Matches Compose's DropdownMenu/Popup: clicking anywhere outside the open dropdown
        // dismisses it, without needing to click the header again.
        val ui = UiContext()
        val column = ui.column(20f, 20f, 160f)

        ui.simulateFrame(pointerDown = false, x = 0f, y = 0f) {
            column.widgetState("dd").set("expanded", true)
            column.dropdown("dd", listOf("A", "B"), selectedIndex = 0, width = 160f, height = 32f)
        }
        assertTrue(column.widgetState("dd").get("expanded", false), "sanity check: still expanded before the outside click")

        // Press far outside the header (y in [20,52]) and both option rows (y in [52,116]).
        ui.simulateFrame(pointerDown = true, x = 190f, y = 190f) {
            column.dropdown("dd", listOf("A", "B"), selectedIndex = 0, width = 160f, height = 32f)
        }

        assertFalse(column.widgetState("dd").get("expanded", true), "an outside click must collapse the dropdown")
    }

    @Test
    fun dropdownStaysOpenWhenClickLandsOnOneOfItsOwnOptions() {
        val ui = UiContext()
        val column = ui.column(20f, 20f, 160f)

        ui.simulateFrame(pointerDown = false, x = 0f, y = 0f) {
            column.widgetState("dd").set("expanded", true)
            column.dropdown("dd", listOf("A", "B"), selectedIndex = 0, width = 160f, height = 32f)
        }

        // Press inside the first option row (directly below the 32px-tall header at y=20).
        ui.simulateFrame(pointerDown = true, x = 30f, y = 60f) {
            column.dropdown("dd", listOf("A", "B"), selectedIndex = 0, width = 160f, height = 32f)
        }

        assertTrue(column.widgetState("dd").get("expanded", false), "a click on an option row must not be treated as an outside click")
    }
}
