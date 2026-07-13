// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlin.test.Test
import kotlin.test.assertEquals

class ClipAndBorderTest {

    @Test
    fun intersectFullyOverlapping() {
        val a = UiSlot(0f, 0f, 100f, 100f)
        val b = UiSlot(20f, 20f, 50f, 50f)
        val result = a.intersect(b)
        assertEquals(UiSlot(20f, 20f, 50f, 50f), result, "b fully inside a must resolve to b")
    }

    @Test
    fun intersectPartiallyOverlapping() {
        val a = UiSlot(0f, 0f, 100f, 100f)
        val b = UiSlot(50f, 50f, 100f, 100f)
        val result = a.intersect(b)
        assertEquals(UiSlot(50f, 50f, 50f, 50f), result)
    }

    @Test
    fun intersectDisjointResolvesToZeroSize() {
        val a = UiSlot(0f, 0f, 10f, 10f)
        val b = UiSlot(100f, 100f, 10f, 10f)
        val result = a.intersect(b)
        assertEquals(0f, result.width, "disjoint rects must resolve to zero width, not negative")
        assertEquals(0f, result.height, "disjoint rects must resolve to zero height, not negative")
    }

    @Test
    fun clipStackResolvesNestedIntersection() {
        Input.setPointer(down = false, x = 0f, y = 0f)
        val ui = UiContext()
        ui.beginFrame(200f, 200f)
        val scope = ui.absolute(0f, 0f)

        scope.clip(UiSlot(0f, 0f, 100f, 100f)) {
            scope.clip(UiSlot(20f, 20f, 200f, 200f)) {
                // no content -- just proving the resolved rects below
            }
        }

        val primitives = ui.endFrame()
        val pushes = primitives.filterIsInstance<UiDrawPrimitive.ClipPush>()
        assertEquals(2, pushes.size)
        assertEquals(UiSlot(0f, 0f, 100f, 100f), pushes[0].rect, "outer clip has no parent to intersect against")
        assertEquals(UiSlot(20f, 20f, 80f, 80f), pushes[1].rect, "inner clip must be intersected against the outer, not just its own requested rect")
    }

    @Test
    fun clipStackPopRestoresParentRect() {
        Input.setPointer(down = false, x = 0f, y = 0f)
        val ui = UiContext()
        ui.beginFrame(200f, 200f)
        val scope = ui.absolute(0f, 0f)

        scope.clip(UiSlot(0f, 0f, 100f, 100f)) {
            scope.clip(UiSlot(20f, 20f, 50f, 50f)) { }
        }

        val primitives = ui.endFrame()
        val pops = primitives.filterIsInstance<UiDrawPrimitive.ClipPop>()
        assertEquals(2, pops.size)
        assertEquals(UiSlot(0f, 0f, 100f, 100f), pops[0].restoreRect, "popping the inner clip restores the outer's resolved rect")
        assertEquals(UiSlot(0f, 0f, 200f, 200f), pops[1].restoreRect, "popping the outermost clip restores the full frame extent")
    }

    @Test
    fun borderEmitsFourQuadsMatchingSlotGeometry() {
        Input.setPointer(down = false, x = 0f, y = 0f)
        val ui = UiContext()
        ui.beginFrame(200f, 200f)
        val scope = ui.absolute(0f, 0f)
        val slot = UiSlot(10f, 10f, 100f, 50f)
        val color = floatArrayOf(1f, 0f, 0f, 1f)

        scope.border(slot, width = 2f.dp, color = color)

        val quads = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>()
        assertEquals(4, quads.size, "border must emit exactly one Quad per edge")
        assertEquals(UiDrawPrimitive.Quad(10f, 10f, 100f, 2f, color), quads[0], "top")
        assertEquals(UiDrawPrimitive.Quad(10f, 58f, 100f, 2f, color), quads[1], "bottom")
        assertEquals(UiDrawPrimitive.Quad(10f, 10f, 2f, 50f, color), quads[2], "left")
        assertEquals(UiDrawPrimitive.Quad(108f, 10f, 2f, 50f, color), quads[3], "right")
    }

    @Test
    fun zeroWidthBorderEmitsNothing() {
        Input.setPointer(down = false, x = 0f, y = 0f)
        val ui = UiContext()
        ui.beginFrame(200f, 200f)
        val scope = ui.absolute(0f, 0f)
        scope.border(UiSlot(0f, 0f, 100f, 100f), width = UiShape.none)
        assertEquals(0, ui.endFrame().size)
    }
}
