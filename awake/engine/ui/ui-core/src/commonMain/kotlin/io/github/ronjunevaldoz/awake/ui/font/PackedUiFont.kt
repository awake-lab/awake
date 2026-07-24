// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(ExperimentalEncodingApi::class)

package io.github.ronjunevaldoz.awake.ui.font

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal interface PackedUiFontData {
    val name: String
    val baseCellSize: Int
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val samplingMode: UiFontSamplingMode
    val fallbackChar: Char
    val encodedAlphaBase64: String
    val glyphOrder: String
    val uvBoundsPx: IntArray
    val quadMetricsEm: FloatArray
    val advancesEm: FloatArray
}

class PackedUiFont internal constructor(
    private val data: PackedUiFontData,
    override val cellSize: Int = data.baseCellSize
) : UiFont {
    override val samplingMode: UiFontSamplingMode = data.samplingMode
    override val textScaleStep: Float = data.textScaleStep
    override val atlasWidth: Int = data.atlasWidth
    override val atlasHeight: Int = data.atlasHeight
    override val atlasPixelsRgba: ByteArray by lazy(::decodeAtlasPixels)
    override val visibleTopEm: Float by lazy { computeVisibleBand().first }
    override val visibleBottomEm: Float by lazy { computeVisibleBand().second }

    private val glyphsByChar: Map<Char, GlyphRect> by lazy(::decodeGlyphRects)
    private val advancesByChar: Map<Char, Float> by lazy(::decodeAdvances)

    override fun uvFor(char: Char): GlyphRect? = glyphsByChar[char] ?: glyphsByChar[data.fallbackChar]

    override fun advanceFor(char: Char, glyphPx: Float): Float =
        glyphPx * (advancesByChar[char] ?: advancesByChar[data.fallbackChar] ?: 1f)

    private fun decodeAtlasPixels(): ByteArray {
        val normalizedBase64 = data.encodedAlphaBase64.filterNot(Char::isWhitespace)
        val alpha = Base64.Default.decode(normalizedBase64)
        val rgba = ByteArray(alpha.size * 4)
        var sourceIndex = 0
        var targetIndex = 0
        while (sourceIndex < alpha.size) {
            rgba[targetIndex] = -1
            rgba[targetIndex + 1] = -1
            rgba[targetIndex + 2] = -1
            rgba[targetIndex + 3] = alpha[sourceIndex]
            sourceIndex += 1
            targetIndex += 4
        }
        return rgba
    }

    private fun decodeGlyphRects(): Map<Char, GlyphRect> {
        val result = LinkedHashMap<Char, GlyphRect>(data.glyphOrder.length)
        var uvIndex = 0
        var metricIndex = 0
        data.glyphOrder.forEach { char ->
            result[char] = GlyphRect(
                u0 = data.uvBoundsPx[uvIndex].toFloat() / atlasWidth,
                v0 = data.uvBoundsPx[uvIndex + 1].toFloat() / atlasHeight,
                u1 = data.uvBoundsPx[uvIndex + 2].toFloat() / atlasWidth,
                v1 = data.uvBoundsPx[uvIndex + 3].toFloat() / atlasHeight,
                offsetXEm = data.quadMetricsEm[metricIndex],
                offsetYEm = data.quadMetricsEm[metricIndex + 1],
                widthEm = data.quadMetricsEm[metricIndex + 2],
                heightEm = data.quadMetricsEm[metricIndex + 3]
            )
            uvIndex += 4
            metricIndex += 4
        }
        return result
    }

    private fun decodeAdvances(): Map<Char, Float> {
        val result = LinkedHashMap<Char, Float>(data.glyphOrder.length)
        data.glyphOrder.forEachIndexed { index, char ->
            result[char] = data.advancesEm[index]
        }
        return result
    }

    private fun computeVisibleBand(): Pair<Float, Float> {
        var metricIndex = 0
        var top = Float.POSITIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        repeat(data.glyphOrder.length) {
            val offsetYEm = data.quadMetricsEm[metricIndex + 1]
            val heightEm = data.quadMetricsEm[metricIndex + 3]
            if (heightEm > 0f) {
                top = minOf(top, offsetYEm)
                bottom = maxOf(bottom, offsetYEm + heightEm)
            }
            metricIndex += 4
        }
        if (!top.isFinite() || !bottom.isFinite() || bottom <= top) {
            return 0f to 1f
        }
        return top to bottom
    }
}
