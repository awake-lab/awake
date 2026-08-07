// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiRasterizerTest {

    @Test
    fun glyphRasterizationSamplesTheRealFontAtlas() {
        val font = BitmapFont()
        val uv = font.uvFor('A') ?: error("A must exist in the debug font")
        val pixels = listOf(
            UiDrawPrimitive.Glyph(
                x = 0f,
                y = 0f,
                w = font.cellSize.toFloat(),
                h = font.cellSize.toFloat(),
                u0 = uv.u0,
                v0 = uv.v0,
                u1 = uv.u1,
                v1 = uv.v1,
                color = Color.White,
            ),
        ).rasterize(font.cellSize, font.cellSize, background = Color.Transparent, font = font)

        val alphas = pixels.filterIndexed { index, _ -> index % 4 == 3 }.map { it.toInt() and 0xFF }
        assertTrue(alphas.any { it == 255 }, "glyph should still keep fully opaque interior pixels")
        assertTrue(alphas.any { it in 1 until 255 }, "filtered glyph rasterization should expose smoothed edge pixels")
    }

    @Test
    fun lowercaseGlyphsRemainVisibleInSnapshots() {
        val font = BitmapFont()
        val uv = font.uvFor('g') ?: error("lowercase aliases must resolve in the debug font")
        val pixels = listOf(
            UiDrawPrimitive.Glyph(
                x = 0f,
                y = 0f,
                w = font.cellSize.toFloat(),
                h = font.cellSize.toFloat(),
                u0 = uv.u0,
                v0 = uv.v0,
                u1 = uv.u1,
                v1 = uv.v1,
                color = Color.White,
            ),
        ).rasterize(font.cellSize, font.cellSize, background = Color.Transparent, font = font)

        assertTrue(pixels.anyIndexed { index, byte -> index % 4 == 3 && (byte.toInt() and 0xFF) > 0 })
    }

    @Test
    fun roundedQuadRasterizationLeavesCornersTransparent() {
        val pixels = listOf(
            UiDrawPrimitive.RoundedQuad(
                x = 4f,
                y = 4f,
                w = 24f,
                h = 24f,
                color = Color(1f, 0f, 0f, 1f),
                radius = UiShapeSpec.RoundedRectangle(8f.dp).radius.toPx(),
            ),
        ).rasterize(32, 32, background = Color.Transparent)

        fun alphaAt(x: Int, y: Int): Int = pixels[(y * 32 + x) * 4 + 3].toInt() and 0xFF

        assertEquals(0, alphaAt(4, 4), "rounded corner should not paint the extreme corner pixel")
        assertTrue(alphaAt(16, 16) > 0, "the interior should remain filled")
    }

    /** Real coverage for `graphicsLayer(scale(...))`'s scale-only transform (see
     * docs/tasks/2026-08-02-graphicslayer-rotation-scale.md) -- proves [UiDrawPrimitive.transform]
     * actually changes the rasterized footprint the same way the GPU vertex shaders'
     * `pivot + (pos - pivot) * scale` math does (see `ui_quad.vert`/`.wgsl`'s comment). */
    @Test
    fun quadWithScaleTransformGrowsAroundItsPivot() {
        fun alphaAt(pixels: ByteArray, width: Int, x: Int, y: Int): Int =
            pixels[(y * width + x) * 4 + 3].toInt() and 0xFF

        val unscaled = listOf(
            UiDrawPrimitive.Quad(x = 8f, y = 8f, w = 8f, h = 8f, color = Color(1f, 0f, 0f, 1f)),
        ).rasterize(32, 32, background = Color.Transparent)

        // 2x scale around the quad's own center (pivot = 12,12) -- doubles the footprint to
        // 16x16 centered on the same point, so a corner well outside the original 8x8 quad
        // (but inside the doubled one) should now be painted.
        val scaled = listOf(
            UiDrawPrimitive.Quad(
                x = 8f,
                y = 8f,
                w = 8f,
                h = 8f,
                color = Color(1f, 0f, 0f, 1f),
                transform = UiPrimitiveTransform(scaleX = 2f, scaleY = 2f, pivotX = 12f, pivotY = 12f),
            ),
        ).rasterize(32, 32, background = Color.Transparent)

        assertEquals(0, alphaAt(unscaled, 32, 5, 5), "unscaled quad should not reach (5,5)")
        assertTrue(alphaAt(scaled, 32, 5, 5) > 0, "2x-scaled quad around its center should now cover (5,5)")
        assertTrue(alphaAt(scaled, 32, 12, 12) > 0, "the pivot point itself should stay covered after scaling")
    }
}

private inline fun ByteArray.anyIndexed(predicate: (Int, Byte) -> Boolean): Boolean {
    for (index in indices) {
        if (predicate(index, this[index])) return true
    }
    return false
}
