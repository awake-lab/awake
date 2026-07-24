// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.core.graphics.border
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.scope.intersect
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.clip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

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
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

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
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        val slot = UiSlot(10f, 10f, 100f, 50f)
        val color = Color(1f, 0f, 0f, 1f)

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
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        scope.border(UiSlot(0f, 0f, 100f, 100f), width = UiShape.none)
        assertEquals(0, ui.endFrame().size)
    }

    @Test
    fun styleShapeOverridesButtonSlotRadiusParam() {
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        scope.buttonSlot("b", modifier = Modifier.width(100f.px).height(40f.px), style = Style {
            shape(
                UiShape.md
            )
        }, radius = UiShape.none)
        val primitive = ui.endFrame().first()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitive, "style.shape() must produce a RoundedQuad even though radius param was UiShape.none")
    }

    @Test
    fun styleBorderOverridesVariantDefault() {
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        val customColor = Color(1f, 0f, 0f, 1f)
        scope.buttonSlot(
            "b",
            modifier = Modifier.width(100f.px).height(40f.px),
            style = Style { border(3f.dp, customColor) },
            variant = UiButtonVariant.Filled
        )
        val quads = ui.endFrame().filterIsInstance<UiDrawPrimitive.Quad>()
        val borderQuads = quads.filter { it.color == customColor }
        assertEquals(4, borderQuads.size, "style.border() must draw all 4 edge quads even for a Filled (non-Outline) button")
    }

    @Test
    fun cutCornerShapeSpecEmitsPathPrimitives() {
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))
        val customBorder = Color(1f, 0f, 0f, 1f)

        scope.buttonSlot(
            "b",
            modifier = Modifier.width(100f.px).height(40f.px),
            style = Style {
                shape(UiShapeSpec.CutCorner(8f.dp))
                border(2f.dp, customBorder)
            }
        )

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.FilledPath>(primitives.first(), "cut-corner widget fill should use the path lane instead of pretending to be a rounded box")
        assertIs<UiDrawPrimitive.StrokedPath>(primitives[1], "cut-corner border should use the path stroke lane")
    }

    @Test
    fun circleShapeSpecOnNonSquareSlotUsesPathLane() {
        val ui = UiContext()
        ui.beginFrame(200f, 200f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

        scope.buttonSlot(
            "circle",
            modifier = Modifier.width(120f.px).height(40f.px),
            style = Style { shape(UiShapeSpec.Circle) }
        )

        val primitives = ui.endFrame()
        assertTrue(primitives.any { it is UiDrawPrimitive.FilledPath }, "a true circle inside a non-square slot needs the path lane")
    }

    @Test
    fun shapeClipEmitsClipPathPushWithResolvedBounds() {
        val ui = UiContext()
        ui.beginFrame(100f, 100f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

        scope.clip(UiShapeSpec.CutCorner(8f.dp), UiSlot(10f, 10f, 40f, 30f)) { }

        val push = ui.endFrame().filterIsInstance<UiDrawPrimitive.ClipPathPush>().single()
        assertEquals(UiSlot(10f, 10f, 40f, 30f), push.boundsRect)
    }

    @Test
    fun pathClipRasterizerMasksPixelsOutsideCutCorner() {
        val red = Color(1f, 0f, 0f, 1f)
        val primitives = buildList {
            add(UiDrawPrimitive.ClipPathPush(UiShapeSpec.CutCorner(8f.dp).toPath(
                UiSlot(
                    10f,
                    10f,
                    40f,
                    30f
                )
            ), UiSlot(10f, 10f, 40f, 30f)
            ))
            add(UiDrawPrimitive.Quad(10f, 10f, 40f, 30f, red))
            add(UiDrawPrimitive.ClipPop(UiSlot(0f, 0f, 64f, 64f)))
        }

        val pixels = primitives.rasterize(64, 64)

        fun rgbaAt(x: Int, y: Int): IntArray {
            val offset = (y * 64 + x) * 4
            return intArrayOf(
                pixels[offset].toInt() and 0xFF,
                pixels[offset + 1].toInt() and 0xFF,
                pixels[offset + 2].toInt() and 0xFF,
                pixels[offset + 3].toInt() and 0xFF
            )
        }

        assertEquals(listOf(25, 25, 30, 255), rgbaAt(11, 11).toList(), "clipped corner should leave the background untouched")
        assertEquals(listOf(255, 0, 0, 255), rgbaAt(30, 25).toList(), "interior pixel should remain painted")
    }

    @Test
    fun panelClipContentEmitsShapeClip() {
        val ui = UiContext()
        ui.beginFrame(120f, 120f, testSnapshot())
        val scope = ui.createAbsolute(modifier = Modifier.offset(0f.dp, 0f.dp))

        scope.surface(
            id = "panel",
            style = Style { shape(UiShapeSpec.CutCorner(8f.dp)) },
            clipContent = true
        , modifier = Modifier.copy(width = Dimension.Fixed(80f.px), height = Dimension.Fixed(60f.px))) { slot ->
            emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, Color(1f, 0f, 0f, 1f)))
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.any { it is UiDrawPrimitive.ClipPathPush }, "clipContent should route shaped panels through the shape clip lane")
    }
}
