// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CanvasDiagnosticTest {

    @Test
    fun canvasPreviewEmitsExpectedGradientsClippingAndNestedCoordinates() {
        val metadata = previewMetadataFor(UiShowcaseCanvasPreview)
        val frame = UiShowcaseCanvasPreview.render(metadata)
        val primitives = frame.primitives

        // 1. Validate Gradients
        // The header gradient is a horizontal gradient quad
        assertTrue(
            primitives.any { it is UiDrawPrimitive.GradientQuad },
            "Canvas must emit at least one gradient quad",
        )

        // 2. Validate Clipping
        // clipShape(UiShapeSpec.Circle, ...) should emit ClipPush/Pop
        assertTrue(
            primitives.any { it is UiDrawPrimitive.ClipPush },
            "Canvas must emit ClipPush for clipShape",
        )
        assertTrue(
            primitives.any { it is UiDrawPrimitive.ClipPop },
            "Canvas must emit ClipPop after clipped content",
        )

        // 3. Validate Nested Local Coordinates
        // The "Chart" rounded quad is nested at (188, 82) within the surface's content slot.
        // The surface is at (24, 24) plus some padding from renderUiShowcaseCardPreviewFrame.
        // We want to ensure that the primitives inside nested {} have their coordinates correctly offset.

        // Find the RoundedQuad that is the chart background
        // It is nested at (188, 82) relative to its parent.
        // Its width is 92.
        val nestedQuads = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>()
            .filter { it.w == 92f && it.h == 92f }

        assertEquals(1, nestedQuads.size, "Must find exactly one nested chart background quad")
        val chartQuad = nestedQuads.first()

        // Ensure the chart quad is offset from the canvas root (showcase-canvas-page surface slot)
        val surfaceNode = frame.semantics.find { it.id == "showcase-canvas-root" }
        assertTrue(surfaceNode != null, "Canvas surface must have a semantic node")

        val expectedChartX = surfaceNode.bounds.x + 188f
        val expectedChartY = surfaceNode.bounds.y + 82f

        // Tolerance is half a pixel, not 0.1: emitted quads pixel-snap (see ShapePainter) while
        // semantic bounds keep their sub-pixel layout position, so the two legitimately differ by
        // up to half a pixel whenever the parent lands off the pixel grid. The old 0.1 only held
        // because this page happened to lay out on whole pixels.
        assertEquals(expectedChartX, chartQuad.x, 0.5f, "Nested quad X must be relative to parent slot")
        assertEquals(expectedChartY, chartQuad.y, 0.5f, "Nested quad Y must be relative to parent slot")
    }
}
