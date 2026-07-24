// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.font

import kotlin.math.sqrt

/**
 * Distance-field flavored UI font built from Awake's existing hand-authored glyph source.
 *
 * This is deliberately not an offline multi-channel toolchain yet. Instead, it packs the
 * same source drawings into an RGB distance atlas so Vulkan, WebGPU, previews, and snapshot
 * tests can all exercise the same distance-field text path now, while leaving room for a
 * future true MSDF asset pipeline without changing the shared UI contracts again.
 */
class MsdfFont(
    override val cellSize: Int = 12,
    atlasScale: Int = 6,
    distanceFieldSourceRange: Float = 2f
) : UiFont {
    override val samplingMode = UiFontSamplingMode.DistanceField
    private val atlasScale = atlasScale.coerceAtLeast(2)
    private val chars: List<Char> = GlyphAtlasSource.chars
    private val columns = chars.size
    override val textScaleStep: Float = 1f / this.atlasScale.toFloat()
    private val atlasCellSize = cellSize * this.atlasScale
    private val atlasInsetPx = maxOf(2, this.atlasScale)
    private val atlasInnerSize = (atlasCellSize - atlasInsetPx * 2).coerceAtLeast(1)
    override val atlasWidth = columns * atlasCellSize
    override val atlasHeight = atlasCellSize
    override val distanceFieldRangePx: Float = (distanceFieldSourceRange * atlasInnerSize / GlyphAtlasSource.sourceCellSize)
        .coerceAtLeast(1f)

    override val atlasPixelsRgba: ByteArray = ByteArray(atlasWidth * atlasHeight * 4).also { pixels ->
        chars.forEachIndexed { charIndex, char ->
            val rows = GlyphAtlasSource.rowsFor(char) ?: return@forEachIndexed
            val boundaries = buildBoundaries(rows)
            val cellX = charIndex * atlasCellSize
            for (row in 0 until atlasCellSize) {
                for (col in 0 until atlasCellSize) {
                    val sampleX = ((col - atlasInsetPx) + 0.5f) / atlasInnerSize * GlyphAtlasSource.sourceCellSize
                    val sampleY = ((row - atlasInsetPx) + 0.5f) / atlasInnerSize * GlyphAtlasSource.sourceCellSize
                    val sourceCol = sampleX.toInt()
                    val sourceRow = sampleY.toInt()
                    val inside = GlyphAtlasSource.isFilled(rows, sourceCol, sourceRow)
                    val distance = boundaries.minOfOrNull { segment ->
                        pointToSegmentDistance(sampleX, sampleY, segment.x0, segment.y0, segment.x1, segment.y1)
                    } ?: distanceFieldSourceRange
                    val signedDistance = if (inside) distance else -distance
                    val normalized = (0.5f + signedDistance / distanceFieldSourceRange * 0.5f).coerceIn(0f, 1f)
                    val encoded = (normalized * 255f).toInt().coerceIn(0, 255).toByte()
                    val alpha = ((if (inside) 1f else 0f) * 255f).toInt().toByte()
                    val offset = (row * atlasWidth + cellX + col) * 4
                    pixels[offset] = encoded
                    pixels[offset + 1] = encoded
                    pixels[offset + 2] = encoded
                    pixels[offset + 3] = alpha
                }
            }
        }
    }

    override fun uvFor(char: Char): GlyphRect? {
        val atlasChar = GlyphAtlasSource.atlasCharFor(char)
        val index = chars.indexOf(atlasChar)
        if (index < 0) return null
        val cellStart = index * atlasCellSize
        val u0 = (cellStart + atlasInsetPx).toFloat() / atlasWidth
        val u1 = (cellStart + atlasCellSize - atlasInsetPx).toFloat() / atlasWidth
        val v0 = atlasInsetPx.toFloat() / atlasHeight
        val v1 = (atlasCellSize - atlasInsetPx).toFloat() / atlasHeight
        return GlyphRect(u0 = u0, v0 = v0, u1 = u1, v1 = v1)
    }

    override fun advanceFor(char: Char, glyphPx: Float): Float = GlyphAtlasSource.advanceFor(char, glyphPx)

    private fun buildBoundaries(rows: IntArray): List<Segment> {
        val segments = ArrayList<Segment>()
        repeat(GlyphAtlasSource.sourceCellSize) { y ->
            repeat(GlyphAtlasSource.sourceCellSize) { x ->
                if (!GlyphAtlasSource.isFilled(rows, x, y)) return@repeat
                if (!GlyphAtlasSource.isFilled(rows, x - 1, y)) segments += Segment(x.toFloat(), y.toFloat(), x.toFloat(), y + 1f)
                if (!GlyphAtlasSource.isFilled(rows, x + 1, y)) segments += Segment(x + 1f, y.toFloat(), x + 1f, y + 1f)
                if (!GlyphAtlasSource.isFilled(rows, x, y - 1)) segments += Segment(x.toFloat(), y.toFloat(), x + 1f, y.toFloat())
                if (!GlyphAtlasSource.isFilled(rows, x, y + 1)) segments += Segment(x.toFloat(), y + 1f, x + 1f, y + 1f)
            }
        }
        return segments
    }

    private fun pointToSegmentDistance(px: Float, py: Float, x0: Float, y0: Float, x1: Float, y1: Float): Float {
        val dx = x1 - x0
        val dy = y1 - y0
        if (dx == 0f && dy == 0f) {
            val distX = px - x0
            val distY = py - y0
            return sqrt(distX * distX + distY * distY)
        }
        val t = (((px - x0) * dx + (py - y0) * dy) / (dx * dx + dy * dy)).coerceIn(0f, 1f)
        val projX = x0 + t * dx
        val projY = y0 + t * dy
        val distX = px - projX
        val distY = py - projY
        return sqrt(distX * distX + distY * distY)
    }

    private data class Segment(val x0: Float, val y0: Float, val x1: Float, val y1: Float)
}
