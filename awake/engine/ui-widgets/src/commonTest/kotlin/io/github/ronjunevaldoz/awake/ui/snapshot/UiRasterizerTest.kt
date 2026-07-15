// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
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
                color = floatArrayOf(1f, 1f, 1f, 1f)
            )
        ).rasterize(font.cellSize, font.cellSize, background = floatArrayOf(0f, 0f, 0f, 0f), font = font)

        val atlasPixels = font.atlasPixelsRgba
        val atlasCellStart = ((uv.u0 * font.atlasWidth).toInt()) * 4

        for (y in 0 until font.cellSize) {
            for (x in 0 until font.cellSize) {
                val rasterAlpha = pixels[(y * font.cellSize + x) * 4 + 3].toInt() and 0xFF
                val atlasAlpha = atlasPixels[(y * font.atlasWidth * 4) + atlasCellStart + x * 4 + 3].toInt() and 0xFF
                assertEquals(atlasAlpha, rasterAlpha, "glyph pixel mismatch at ($x, $y)")
            }
        }
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
                color = floatArrayOf(1f, 1f, 1f, 1f)
            )
        ).rasterize(font.cellSize, font.cellSize, background = floatArrayOf(0f, 0f, 0f, 0f), font = font)

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
                color = floatArrayOf(1f, 0f, 0f, 1f),
                radius = UiShapeSpec.RoundedRectangle(8f.dp).radius.toPx()
            )
        ).rasterize(32, 32, background = floatArrayOf(0f, 0f, 0f, 0f))

        fun alphaAt(x: Int, y: Int): Int = pixels[(y * 32 + x) * 4 + 3].toInt() and 0xFF

        assertEquals(0, alphaAt(4, 4), "rounded corner should not paint the extreme corner pixel")
        assertTrue(alphaAt(16, 16) > 0, "the interior should remain filled")
    }
}

private inline fun ByteArray.anyIndexed(predicate: (Int, Byte) -> Boolean): Boolean {
    for (index in indices) {
        if (predicate(index, this[index])) return true
    }
    return false
}
