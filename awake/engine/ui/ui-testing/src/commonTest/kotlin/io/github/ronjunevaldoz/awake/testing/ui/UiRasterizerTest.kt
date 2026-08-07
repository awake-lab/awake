// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class UiRasterizerTest {

    /** Regression for a vertex-0 triangle-fan seam: a wide (600px), shallow (36px) rounded
     * rect used to leave a gap along the top edge on the side far from the fan's origin
     * vertex, because float32 catastrophic cancellation in the rasterizer's edge function
     * mis-rejected pixels covered by near-degenerate sliver triangles. See UiPath.tessellateFill. */
    @Test
    fun wideShallowRoundedQuadFillsTopEdgeSymmetrically() {
        val fill = Color(0.1f, 0.1f, 0.1f, 1f)
        val primitives = listOf(UiDrawPrimitive.RoundedQuad(x = 0f, y = 0f, w = 600f, h = 36f, color = fill, radius = 6f))
        val pixels = primitives.rasterize(width = 600, height = 36, background = Color(1f, 1f, 1f, 1f))

        fun colorAt(x: Int, y: Int): Color {
            val offset = (y * 600 + x) * 4
            return Color(
                (pixels[offset].toInt() and 0xFF) / 255f,
                (pixels[offset + 1].toInt() and 0xFF) / 255f,
                (pixels[offset + 2].toInt() and 0xFF) / 255f,
                (pixels[offset + 3].toInt() and 0xFF) / 255f,
            )
        }

        // Row just past the top-left/top-right corner recession (radius=6): both sides of the
        // top edge, symmetric distance from their respective corner, must be filled the same.
        val interior = colorAt(300, 20)
        assertEquals(colorAt(20, 1), colorAt(579, 1), "top edge fill must be symmetric across both corners")
        assertEquals(interior, colorAt(300, 1), "top edge near the shape's midpoint (far from vertex 0) must be filled")
    }
}
