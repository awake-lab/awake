// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.ResizableDirection
import io.github.ronjunevaldoz.awake.ui.unstyled.resizablePanelGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResizablePanelGroupTest {

    // Group is 600px wide, one handle -- see RESIZABLE_HANDLE_THICKNESS (2dp/2px at the default
    // test density): 598px left for the two 50/50 panels, 299px each.
    @Test
    fun twoPanelsSplitAroundOneHandleLeavesRoomForTheHandle() {
        val ui = UiContext()
        var panel1: UiBounds? = null
        var panel2: UiBounds? = null
        ui.simulateFrame(pointerDown = false, x = -100f, y = -100f) {
            ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                id = "group",
                modifier = Modifier.width(600f.px).height(200f.px),
            ) {
                panel1 = panel("p1", defaultSize = 0.5f) { }
                handle("h1")
                panel2 = panel("p2", defaultSize = 0.5f) { }
            }
        }
        assertEquals(299f, panel1!!.width, 0.5f, "left panel should absorb half the budget left after the handle's own width")
        assertEquals(299f, panel2!!.width, 0.5f, "right panel should absorb the other half")
        assertEquals(0f, panel1!!.x, "left panel starts at the group's own origin")
        assertEquals(301f, panel2!!.x, 0.5f, "right panel starts after the left panel and the 2px handle")
    }

    @Test
    fun dragMovesFractionsAndClampsAtMinSize() {
        val ui = UiContext()
        var panel1: UiBounds? = null
        var panel2: UiBounds? = null
        val input = Input()

        fun frame(pointerDown: Boolean, x: Float) {
            ui.simulateFrame(pointerDown = pointerDown, x = x, y = 100f, input = input) {
                ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                    id = "drag-group",
                    modifier = Modifier.width(600f.px).height(200f.px),
                ) {
                    panel1 = panel("p1", defaultSize = 0.5f) { }
                    handle("h1")
                    panel2 = panel("p2", defaultSize = 0.5f) { }
                }
            }
        }

        // Handle sits at x in [299, 301] (see the layout test above) -- press its center.
        frame(pointerDown = true, x = 300f)
        val widthAfterPress = panel1!!.width
        assertEquals(299f, widthAfterPress, 0.5f, "pressing the handle must not move it before any pointer motion")

        frame(pointerDown = true, x = 400f)
        // One more frame at the same x: the panel drawn *before* its own trailing handle reads
        // the fraction the handle just wrote, one frame behind (see ResizablePanelGroupScope's
        // class doc ponytail note) -- this settle frame lets that write become visible.
        frame(pointerDown = true, x = 400f)
        val widthAfterMove = panel1!!.width
        assertTrue(
            widthAfterMove > widthAfterPress + 50f,
            "dragging the handle right by 100px must grow the panel before it (was $widthAfterPress, now $widthAfterMove)",
        )

        frame(pointerDown = true, x = -5000f)
        frame(pointerDown = true, x = -5000f)
        val minWidth = 0.1f * (600f - 2f)
        assertEquals(minWidth, panel1!!.width, 1f, "left panel must clamp at its minSize, not shrink past it")
        assertTrue(
            panel2!!.width > widthAfterMove,
            "right panel should have absorbed the space the left panel gave up while dragging further left",
        )

        frame(pointerDown = false, x = -5000f)
    }

    @Test
    fun nestedVerticalGroupInsideHorizontalPanelLaysOutCleanly() {
        val ui = UiContext()
        ui.beginFrame(600f, 400f, testSnapshot())
        ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
            id = "outer",
            direction = ResizableDirection.Horizontal,
            modifier = Modifier.width(600f.px).height(400f.px),
        ) {
            panel("left", defaultSize = 0.5f) { slot ->
                emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, Color.Black))
            }
            handle("h-outer")
            panel("right", defaultSize = 0.5f) { slot ->
                resizablePanelGroup(id = "inner", direction = ResizableDirection.Vertical) {
                    panel("two", defaultSize = 0.25f) { s ->
                        emit(UiDrawPrimitive.Quad(s.x, s.y, s.width, s.height, Color.Black))
                    }
                    handle("h-inner")
                    panel("three", defaultSize = 0.75f) { s ->
                        emit(UiDrawPrimitive.Quad(s.x, s.y, s.width, s.height, Color.Black))
                    }
                }
            }
        }
        val output = ui.finishFrame()
        val report = inspectUiFrame(output.primitives, ui.frameBounds())
        assertTrue(report.isClean, report.summary())
    }

    @Test
    fun hoveringHorizontalHandleRequestsHorizontalResizeCursor() {
        val ui = UiContext()
        // Handle sits at x in [299, 301] for a 600px-wide group (see the layout test above).
        ui.simulateFrame(pointerDown = false, x = 300f, y = 100f) {
            ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                id = "cursor-group",
                modifier = Modifier.width(600f.px).height(200f.px),
            ) {
                panel("p1", defaultSize = 0.5f) { }
                handle("h1")
                panel("p2", defaultSize = 0.5f) { }
            }
        }
        assertEquals(
            UiCursor.ResizeHorizontal,
            ui.finishFrame().effects.cursor,
            "hovering a horizontal handle must request the horizontal resize cursor",
        )
    }

    @Test
    fun hoveringVerticalHandleRequestsVerticalResizeCursor() {
        val ui = UiContext()
        // Vertical group, 200px tall, one handle: handle band sits at y in [99, 101].
        ui.simulateFrame(pointerDown = false, x = 100f, y = 100f) {
            ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                id = "cursor-group-v",
                direction = ResizableDirection.Vertical,
                modifier = Modifier.width(200f.px).height(200f.px),
            ) {
                panel("p1", defaultSize = 0.5f) { }
                handle("h1")
                panel("p2", defaultSize = 0.5f) { }
            }
        }
        assertEquals(
            UiCursor.ResizeVertical,
            ui.finishFrame().effects.cursor,
            "hovering a vertical handle must request the vertical resize cursor",
        )
    }

    @Test
    fun pointerAwayFromHandleLeavesCursorDefault() {
        val ui = UiContext()
        ui.simulateFrame(pointerDown = false, x = -100f, y = -100f) {
            ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                id = "cursor-group-away",
                modifier = Modifier.width(600f.px).height(200f.px),
            ) {
                panel("p1", defaultSize = 0.5f) { }
                handle("h1")
                panel("p2", defaultSize = 0.5f) { }
            }
        }
        assertEquals(
            UiCursor.Default,
            ui.finishFrame().effects.cursor,
            "a frame with no pointer near the handle must not request a resize cursor",
        )
    }

    @Test
    fun draggingHandleKeepsResizeCursorEvenIfPointerOutrunsTheThinHitStrip() {
        val ui = UiContext()
        val input = Input()

        fun frame(pointerDown: Boolean, x: Float) {
            ui.simulateFrame(pointerDown = pointerDown, x = x, y = 100f, input = input) {
                ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                    id = "cursor-drag-group",
                    modifier = Modifier.width(600f.px).height(200f.px),
                ) {
                    panel("p1", defaultSize = 0.5f) { }
                    handle("h1")
                    panel("p2", defaultSize = 0.5f) { }
                }
            }
        }

        frame(pointerDown = true, x = 300f)
        // Drag far past the handle's own 2px-wide hit strip -- still mid-gesture.
        frame(pointerDown = true, x = 400f)
        assertEquals(
            UiCursor.ResizeHorizontal,
            ui.finishFrame().effects.cursor,
            "a resize gesture still in progress must keep requesting the resize cursor even " +
                "once the pointer has moved off the handle's own thin hit strip",
        )
    }
}
