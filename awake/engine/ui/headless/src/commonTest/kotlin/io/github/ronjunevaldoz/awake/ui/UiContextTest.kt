// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.sliderValueFromPointerX
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.select
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
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
            checked = ui.createAbsolute(x = 20f, y = 20f).toggle(
                "t",
                checked,
                modifier = Modifier.width(
                    120f.px,
                ).height(40f.px),
            )
        }
        assertFalse(checked, "should not toggle on hover alone")

        // Press+release while hovered -- click fires on the release frame.
        ui.simulateClick(x = 60f, y = 40f, screenHeight = 100f) {
            checked = ui.createAbsolute(x = 20f, y = 20f).toggle(
                "t",
                checked,
                modifier = Modifier.width(
                    120f.px,
                ).height(40f.px),
            )
        }
        assertTrue(checked, "should toggle on press+release inside bounds")
    }

    @Test
    fun buttonDoesNotFireWhenPointerOutsideBounds() {
        val ui = UiContext()
        var clicked = false
        ui.simulateClick(x = 5f, y = 5f, screenHeight = 100f) {
            clicked = ui.createAbsolute(x = 20f, y = 20f).button(
                "b",
                modifier = Modifier.width(120f.px).height(40f.px),
            )
        }
        assertFalse(clicked, "click outside the widget's bounds must not register")
    }

    @Test
    fun sliderValueFromPointerXMapsTrackEdgesAndMidpoint() {
        // Track spans x=[20, 120] (w=100), value range [0, 10].
        assertEquals(
            0f,
            sliderValueFromPointerX(20f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
        )
        assertEquals(
            10f,
            sliderValueFromPointerX(120f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
        )
        assertEquals(
            5f,
            sliderValueFromPointerX(70f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
        )
    }

    @Test
    fun sliderValueFromPointerXClampsOutsideTrackBounds() {
        assertEquals(
            0f,
            sliderValueFromPointerX(-50f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
            "pointer left of the track must clamp to min, not extrapolate negative",
        )
        assertEquals(
            10f,
            sliderValueFromPointerX(500f, trackX = 20f, trackW = 100f, min = 0f, max = 10f),
            "pointer right of the track must clamp to max, not extrapolate past it",
        )
    }

    @Test
    fun sliderDragUpdatesValueWhilePointerHeldInsideTrack() {
        val ui = UiContext()

        // Press inside the slider's track -- latches activeId, no drag yet on this frame's
        // pointer position (matches button's own press-edge semantics).
        ui.beginFrame(200f, 100f, testSnapshot(x = 20f, y = 30f, down = true))
        var value = ui.createAbsolute(x = 20f, y = 20f).slider(
            "vol",
            min = 0f,
            max = 10f,
            value = 0f,
            modifier = Modifier.width(
                100f.px,
            ).height(20f.px),
        )
        ui.endFrame()

        // Drag to the track's midpoint while still held.
        ui.beginFrame(200f, 100f, testSnapshot(x = 70f, y = 30f, down = true))
        value = ui.createAbsolute(x = 20f, y = 20f).slider(
            "vol",
            min = 0f,
            max = 10f,
            value = value,
            modifier = Modifier.width(
                100f.px,
            ).height(20f.px),
        )
        ui.endFrame()
        assertEquals(5f, value, "dragging to the track midpoint should map to the midpoint value")

        // Release -- value should hold at whatever it last was.
        ui.beginFrame(200f, 100f, testSnapshot(x = 70f, y = 30f, down = false))
        value = ui.createAbsolute(x = 20f, y = 20f).slider(
            "vol",
            min = 0f,
            max = 10f,
            value = value,
            modifier = Modifier.width(
                100f.px,
            ).height(20f.px),
        )
        ui.endFrame()
        assertEquals(5f, value, "releasing should not further change the value")
    }

    @Test
    fun dropdownCanUseModifierSizingAsPrimaryApi() {
        val ui = UiContext()
        ui.beginFrame(220f, 160f, testSnapshot(x = 0f, y = 0f, down = false))
        val column = ui.createColumn(x = 20f, y = 20f, width = 160f)
        val expandedState = column.widgetState("dd")
        expandedState.set("expanded", true)

        column.select(
            id = "dd",
            options = listOf("A", "B"),
            selectedIndex = 0,
            modifier = Modifier.fillMaxWidth()
                .height(32f.px),
        )

        val optionBackgrounds = ui.endFrame().filter { primitive ->
            when (primitive) {
                is UiDrawPrimitive.Quad -> primitive.x == 20f && primitive.w == 160f && (primitive.y == 52f || primitive.y == 84f)
                is UiDrawPrimitive.RoundedQuad -> primitive.x == 20f && primitive.w == 160f && (primitive.y == 52f || primitive.y == 84f)
                else -> false
            }
        }
        assertEquals(
            2,
            optionBackgrounds.size,
            "modifier-sized dropdown should still expand to full-width option rows",
        )
    }

    @Test
    fun dropdownOptionsAlwaysPaintAfterSiblingWidgetsRegardlessOfCallOrder() {
        // Regression test for the dropdown-overlap bug: even when a sibling widget is drawn
        // in the SAME frame AFTER the dropdown's expanded options, the options must still end
        // up later in endFrame()'s output (painted on top), since they go through
        // emitOverlay() rather than emit().
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot(x = 0f, y = 0f, down = false))
        val column = ui.createColumn(x = 20f, y = 20f, width = 160f)

        // Expand the dropdown first (simulating it was already expanded from a prior frame).
        val expandedState = column.widgetState("dd")
        expandedState.set("expanded", true)
        column.select(
            "dd",
            listOf("A", "B"),
            selectedIndex = 0,
            modifier = Modifier.width(
                160f.px,
            ).height(32f.px),
        )

        // A sibling widget drawn AFTER the dropdown in this same frame, at a position that
        // would spatially overlap the dropdown's first expanded option row.
        column.button(
            "sibling",
            label = "Sibling",
            modifier = Modifier.width(
                160f.px,
            ).height(32f.px),
        )

        val allPrimitives = ui.endFrame()
        val siblingIndex = allPrimitives.indexOfLast { primitive ->
            when (primitive) {
                is UiDrawPrimitive.Quad -> primitive.x == 20f && primitive.y == 60f && primitive.w == 160f && primitive.h == 32f
                is UiDrawPrimitive.RoundedQuad -> primitive.x == 20f && primitive.y == 60f && primitive.w == 160f && primitive.h == 32f
                else -> false
            }
        }
        val optionIndices = buildList {
            allPrimitives.forEachIndexed { index, primitive ->
                val matches = when (primitive) {
                    is UiDrawPrimitive.Quad ->
                        primitive.x == 20f &&
                            primitive.w == 160f &&
                            primitive.h == 32f &&
                            (primitive.y == 52f || primitive.y == 84f)

                    is UiDrawPrimitive.RoundedQuad ->
                        primitive.x == 20f &&
                            primitive.w == 160f &&
                            primitive.h == 32f &&
                            (primitive.y == 52f || primitive.y == 84f)

                    is UiDrawPrimitive.FilledPath -> false
                    is UiDrawPrimitive.StrokedPath -> false
                    else -> false
                }
                if (matches) {
                    add(index)
                }
            }
        }
        assertTrue(siblingIndex >= 0, "sanity check: sibling widget background must be present")
        assertEquals(
            2,
            optionIndices.size,
            "expected one option background primitive per expanded option row",
        )
        assertTrue(
            optionIndices.all { it > siblingIndex },
            "expected the dropdown's option backgrounds to paint after the sibling widget",
        )
    }

    @Test
    fun dropdownCollapsesWhenClickLandsOutsideHeaderAndOptions() {
        // Matches Compose's DropdownMenu/Popup: clicking anywhere outside the open dropdown
        // dismisses it, without needing to click the header again.
        val ui = UiContext()
        val column = ui.createColumn(x = 20f, y = 20f, width = 160f)

        ui.simulateFrame(pointerDown = false, x = 0f, y = 0f) {
            column.widgetState("dd").set("expanded", true)
            column.select(
                "dd",
                listOf("A", "B"),
                selectedIndex = 0,
                modifier = Modifier.width(
                    160f.px,
                ).height(32f.px),
            )
        }
        assertTrue(
            column.widgetState("dd").get("expanded", false),
            "sanity check: still expanded before the outside click",
        )

        // Press far outside the header (y in [20,52]) and both option rows (y in [52,116]).
        ui.simulateFrame(pointerDown = true, x = 190f, y = 190f) {
            column.select(
                "dd",
                listOf("A", "B"),
                selectedIndex = 0,
                modifier = Modifier.width(
                    160f.px,
                ).height(32f.px),
            )
        }

        assertFalse(
            column.widgetState("dd").get("expanded", true),
            "an outside click must collapse the dropdown",
        )
    }

    @Test
    fun dropdownStaysOpenWhenClickLandsOnOneOfItsOwnOptions() {
        val ui = UiContext()
        val column = ui.createColumn(x = 20f, y = 20f, width = 160f)

        ui.simulateFrame(pointerDown = false, x = 0f, y = 0f) {
            column.widgetState("dd").set("expanded", true)
            column.select(
                "dd",
                listOf("A", "B"),
                selectedIndex = 0,
                modifier = Modifier.width(
                    160f.px,
                ).height(32f.px),
            )
        }

        // Press inside the first option row (directly below the 32px-tall header at y=20).
        ui.simulateFrame(pointerDown = true, x = 30f, y = 60f) {
            column.select(
                "dd",
                listOf("A", "B"),
                selectedIndex = 0,
                modifier = Modifier.width(
                    160f.px,
                ).height(32f.px),
            )
        }

        assertTrue(
            column.widgetState("dd").get("expanded", false),
            "a click on an option row must not be treated as an outside click",
        )
    }
}
