// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.resizablePanelGroup
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exact-value drag contract: a handle drag moves panel widths by exactly the pointer delta,
 * conserves the pair's total, and never grows the group. The older drag test's `> +50px`
 * threshold passed while drags moved 2x the pointer and the group stretched (2026-08-15
 * audit, reported on desktop).
 */
class ResizablePanelDragConservationTest {

    private class Panels {
        var p1: UiBounds? = null
        var p2: UiBounds? = null
        var handle: UiBounds? = null
    }

    private fun frame(ui: UiContext, input: Input, panels: Panels, pointerDown: Boolean, x: Float) {
        ui.simulateFrame(pointerDown = pointerDown, x = x, y = 100f, input = input) {
            ui.createAbsolute(x = 0f, y = 0f).resizablePanelGroup(
                id = "exact-drag-group",
                modifier = Modifier.width(600f.px).height(200f.px),
            ) {
                panels.p1 = panel("p1", defaultSize = 0.5f) { }
                panels.handle = handle("h1")
                panels.p2 = panel("p2", defaultSize = 0.5f) { }
            }
        }
    }

    @Test
    fun dragMovesPanelsByExactlyThePointerDeltaAndConservesTotalWidth() {
        val ui = UiContext()
        val input = Input()
        val panels = Panels()

        frame(ui, input, panels, pointerDown = true, x = 300f)
        assertEquals(298f, panels.p1!!.width, 0.5f, "press alone must not move the split")
        assertEquals(298f, panels.p2!!.width, 0.5f, "press alone must not move the split")

        frame(ui, input, panels, pointerDown = true, x = 400f)
        frame(ui, input, panels, pointerDown = true, x = 400f)

        assertEquals(398f, panels.p1!!.width, 1f, "p1 must grow by exactly the 100px pointer delta")
        assertEquals(198f, panels.p2!!.width, 1f, "p2 must shrink by exactly the 100px pointer delta")
        assertEquals(
            596f,
            panels.p1!!.width + panels.p2!!.width,
            0.5f,
            "the pair must conserve the group's panel budget",
        )
        assertEquals(
            400f,
            panels.handle!!.x + panels.handle!!.width / 2f,
            2f,
            "the handle center must track the pointer 1:1",
        )

        frame(ui, input, panels, pointerDown = false, x = 400f)
    }

    // The showcase's failing shape: the group fills a parent whose own width wraps content
    // (the preview card). The group's slot must stay pinned frame to frame -- the live bug
    // grew the card by roughly the drag delta while the far panel never shrank.
    @Test
    fun groupInsideWrapContentParentDoesNotGrowDuringDrag() {
        val ui = UiContext()
        val input = Input()
        val panels = Panels()
        var group: UiBounds? = null

        fun wrappedFrame(pointerDown: Boolean, x: Float) {
            ui.simulateFrame(pointerDown = pointerDown, x = x, y = 100f, input = input) {
                ui.createAbsolute(x = 0f, y = 0f).column(
                    id = "wrap-parent",
                    modifier = Modifier.width(Dimension.WrapContent).height(240f.px),
                ) {
                    group = resizablePanelGroup(
                        id = "wrap-drag-group",
                        modifier = Modifier.fillMaxWidth().height(200f.px),
                    ) {
                        panels.p1 = panel("p1", defaultSize = 0.4f) { }
                        panels.handle = handle("h1")
                        panels.p2 = panel("p2", defaultSize = 0.6f) { }
                    }
                }
            }
        }

        wrappedFrame(pointerDown = false, x = -100f)
        val settledWidth = group!!.width
        val handleCenter = panels.handle!!.x + panels.handle!!.width / 2f
        wrappedFrame(pointerDown = true, x = handleCenter)
        wrappedFrame(pointerDown = true, x = handleCenter + 100f)
        wrappedFrame(pointerDown = true, x = handleCenter + 100f)
        wrappedFrame(pointerDown = false, x = handleCenter + 100f)
        wrappedFrame(pointerDown = false, x = -100f)

        assertEquals(
            settledWidth,
            group!!.width,
            0.5f,
            "dragging a handle must redistribute inside the group, never grow the group itself",
        )
        assertEquals(
            settledWidth - panels.handle!!.width,
            panels.p1!!.width + panels.p2!!.width,
            1f,
            "panel pair must still sum to the group budget after the drag",
        )
    }

    // Full failing shape from the showcase (reproduced without designsystem): a WrapContent
    // surface whose width hugs a text sibling, holding a fillMaxWidth group, at a narrow frame.
    @Test
    fun wrapContentSurfaceHuggingTextDoesNotGrowDuringDrag() {
        val ui = UiContext()
        val input = Input()
        val panels = Panels()
        var card: UiBounds? = null

        fun surfaceFrame(pointerDown: Boolean, x: Float) {
            ui.simulateFrame(pointerDown = pointerDown, x = x, y = 200f, input = input) {
                ui.createAbsolute(x = 0f, y = 0f).column(
                    id = "shell",
                    modifier = Modifier.width(592f.px).height(402f.px),
                ) {
                    card = surface(
                        id = "card",
                        style = Style { contentPadding(24f.dp, 24f.dp, 24f.dp, 24f.dp) },
                        modifier = Modifier.width(Dimension.WrapContent).height(Dimension.WrapContent),
                    ) {
                        text(label = "Drag the handle to redistribute space between the two panels.")
                        resizablePanelGroup(
                            id = "card-group",
                            modifier = Modifier.fillMaxWidth().height(240f.px),
                        ) {
                            panels.p1 = panel("p1", defaultSize = 0.4f) { }
                            panels.handle = handle("h1")
                            panels.p2 = panel("p2", defaultSize = 0.6f) { }
                        }
                    }
                }
            }
        }

        surfaceFrame(pointerDown = false, x = -100f)
        surfaceFrame(pointerDown = false, x = -100f)
        val cardBefore = card!!.width
        val handleCenter = panels.handle!!.x + panels.handle!!.width / 2f
        surfaceFrame(pointerDown = true, x = handleCenter)
        surfaceFrame(pointerDown = true, x = handleCenter + 100f)
        surfaceFrame(pointerDown = true, x = handleCenter + 100f)
        surfaceFrame(pointerDown = false, x = handleCenter + 100f)
        surfaceFrame(pointerDown = false, x = -100f)
        assertEquals(
            cardBefore,
            card!!.width,
            0.5f,
            "a WrapContent surface must not grow because a handle inside it was dragged",
        )
    }

    @Test
    fun secondDragGestureStartsWithoutJumpingFromStalePointerState() {
        val ui = UiContext()
        val input = Input()
        val panels = Panels()

        frame(ui, input, panels, pointerDown = true, x = 300f)
        frame(ui, input, panels, pointerDown = true, x = 350f)
        frame(ui, input, panels, pointerDown = true, x = 350f)
        // Release and immediately press again elsewhere on the handle -- no idle frame between
        // gestures, the shape a fast user (or any automation) produces.
        frame(ui, input, panels, pointerDown = false, x = 350f)
        val widthBeforeSecondPress = panels.p1!!.width
        frame(ui, input, panels, pointerDown = true, x = 350f)
        frame(ui, input, panels, pointerDown = true, x = 350f)
        assertEquals(
            widthBeforeSecondPress,
            panels.p1!!.width,
            0.5f,
            "pressing the handle again without moving must not jump the split",
        )
    }
}
