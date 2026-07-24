// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiDebugOverlayTest {

    @Test
    fun boundsOnlyNodeEmitsExactlyOneStrokedPath() {
        val node = UiSemanticNode(role = UiSemanticRole.Panel, bounds = UiSlot(0f, 0f, 100f, 40f))
        val primitives = node.debugOverlayPrimitives()
        assertEquals(1, primitives.size, "no contentBounds/clippedBounds should mean just the bounds outline")
        val stroked = primitives.single() as UiDrawPrimitive.StrokedPath
        assertEquals(UiDebugOverlayColors.Bounds, stroked.color)
    }

    @Test
    fun contentAndClippedBoundsAddTheirOwnDistinctlyColoredOutlines() {
        val node = UiSemanticNode(
            role = UiSemanticRole.Button,
            bounds = UiSlot(0f, 0f, 100f, 40f),
            contentBounds = UiSlot(8f, 8f, 84f, 24f),
            clippedBounds = UiSlot(0f, 0f, 60f, 40f)
        )
        val primitives = node.debugOverlayPrimitives().filterIsInstance<UiDrawPrimitive.StrokedPath>()
        assertEquals(3, primitives.size)
        val colors = primitives.map { it.color }.toSet()
        assertEquals(setOf(UiDebugOverlayColors.Bounds, UiDebugOverlayColors.ContentBounds, UiDebugOverlayColors.ClippedBounds), colors)
    }

    @Test
    fun contextOverlaySurvivesPastEndFrameUntilNextBeginFrame() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())
        ui.createAbsolute(modifier = UiModifier().offset(10f.dp, 10f.dp)).recordSemantic(role = UiSemanticRole.Button, bounds = UiSlot(
            10f,
            10f,
            80f,
            32f
        ), id = "b")
        ui.endFrame()

        val overlay = ui.debugOverlayPrimitives()
        assertTrue(overlay.isNotEmpty(), "semantic nodes recorded during the frame should still be readable after endFrame()")

        ui.beginFrame(200f, 100f, testSnapshot())
        assertTrue(ui.debugOverlayPrimitives().isEmpty(), "beginFrame() clears semantic nodes from the prior frame")
    }
}
