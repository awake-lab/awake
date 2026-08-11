// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(ExperimentalEncodingApi::class)

package io.github.ronjunevaldoz.awake.ui.font

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal interface PackedUiFontData {
    val name: String
    val baseCellSize: Int

    /** Ascent + descent as a multiple of the em size (Roboto: ~1.19). A font's ink band is
     * taller than its nominal size, so a slot sized at exactly glyphPx cannot contain its own
     * text. Explicit because baseCellSize used to double as the line height and stopped being
     * able to once em values were normalised against the font size. */
    val lineHeightEm: Float
    val textScaleStep: Float
    val atlasWidth: Int
    val atlasHeight: Int
    val samplingMode: UiFontSamplingMode
    val fallbackChar: Char

    /** Distance-field spread, in ATLAS TEXELS -- the `-pxrange` the atlas was generated with.
     * The glyph shader divides this by the atlas size to recover a UV-space range, so it must
     * stay in texels, not screen pixels. Ignored for [UiFontSamplingMode.CoverageAlpha]. */
    val distanceFieldRangePx: Float get() = 0f

    /** Channels per texel in [encodedAtlasBase64]: 1 for a coverage-alpha atlas, 4 for an
     * MTSDF one (RGB carries the multi-channel distance, A a true signed distance). */
    val atlasChannels: Int get() = 1

    /** Raw atlas bytes, [atlasChannels] per texel, row-major from the top-left. */
    val encodedAtlasBase64: String
    val glyphOrder: String
    val uvBoundsPx: IntArray

    /** Per-glyph RENDER quad (offsetX, offsetY, width, height in em, pen/line-top relative),
     * covering exactly the texels [uvBoundsPx] samples -- outline plus crop bleed plus the
     * texel snap. Sizing the quad to anything smaller than the sampled rect squeezes the atlas
     * region into it and every glyph renders below its own metrics. */
    val quadMetricsEm: FloatArray

    /** Per-glyph INK bounds (same layout), outline-exact with no bleed. This is what metrics
     * (cap height, baseline, visible band, advance clamping) must derive from; deriving them
     * from [quadMetricsEm] bakes the bleed into the type's measurements. Defaults to
     * [quadMetricsEm] for data generated before the split. */
    val inkMetricsEm: FloatArray get() = quadMetricsEm
    val advancesEm: FloatArray
}

class PackedUiFont internal constructor(
    private val data: PackedUiFontData,
    override val cellSize: Int = data.baseCellSize,
) : UiFont {
    override val samplingMode: UiFontSamplingMode = data.samplingMode
    override val textScaleStep: Float = data.textScaleStep
    override val atlasWidth: Int = data.atlasWidth
    override val atlasHeight: Int = data.atlasHeight
    override val atlasPixelsRgba: ByteArray by lazy(::decodeAtlasPixels)
    override val distanceFieldRangePx: Float = data.distanceFieldRangePx
    override val visibleTopEm: Float by lazy { computeVisibleBand().first }
    override val visibleBottomEm: Float by lazy { computeVisibleBand().second }

    /** Distance from a line's layout origin down to its baseline, measured off the atlas rather
     * than inherited from [UiFont]'s fabricated 0.8 default. */
    override val ascentEm: Float by lazy { baselineEm }

    /** Ink below the baseline. Uses the atlas's deepest glyph, so it covers every descender the
     * font can actually draw. */
    override val descentEm: Float by lazy { visibleBottomEm - baselineEm }

    override val capHeightEm: Float by lazy { measureCapHeightEm() }

    /**
     * Baseline position, read off a flat-bottomed capital rather than assumed.
     *
     * Round glyphs ('O', 'S') overshoot the baseline slightly by design, and every descender
     * sits well below it, so the atlas's own ink extremes cannot locate it -- [visibleBottomEm]
     * is 1.265625 here, set by '@'. [BASELINE_REFERENCE_GLYPHS] are all flat-bottomed with no
     * overshoot, so their ink bottom IS the baseline; they agree exactly in the packed Roboto
     * atlas, and the first one present wins.
     */
    private val baselineEm: Float by lazy {
        BASELINE_REFERENCE_GLYPHS.firstNotNullOfOrNull { char ->
            inkByChar[char]?.takeIf { it.heightEm > 0f }?.let { it.offsetYEm + it.heightEm }
        } ?: visibleBottomEm
    }

    private val glyphsByChar: Map<Char, GlyphRect> by lazy(::decodeGlyphRects)
    private val inkByChar: Map<Char, InkBounds> by lazy(::decodeInkBounds)
    private val advancesByChar: Map<Char, Float> by lazy(::decodeAdvances)

    override val lineHeightEm: Float get() = data.lineHeightEm

    override fun uvFor(char: Char): GlyphRect? = glyphsByChar[char] ?: glyphsByChar[data.fallbackChar]

    /**
     * The embedded Roboto data's declared advance is supposed to trail the glyph's own quad
     * right edge (`offsetXEm + widthEm`) by only "a few percent" (see [measureTextWidth]'s
     * doc), just enough that the *next* glyph's quad redraws over the sliver of overhang.
     * In practice several common letters ('t', 'r', 'f', 'k', ...) declare an advance
     * 20-40% narrower than their own ink -- e.g. 'f' overhangs by 42% of its advance width --
     * which at real UI text sizes reads as adjacent glyphs visibly touching/merging (the
     * per-character misalignment this was reported as). Clamping the pen step to never be
     * smaller than the glyph's own right edge keeps every glyph's ink inside its own advance,
     * eliminating the collision while leaving glyphs with correctly-tight metrics untouched.
     *
     * `offsetXEm` is generated pen-relative (measured from the same pen origin as
     * `advanceEm`, see generate_ui_font_atlas.py), so `rightEdgeEm` is directly comparable to
     * `advanceEm` here -- it must NOT carry any atlas-cell-relative padding term, or this
     * `maxOf` degenerates into clamping essentially every glyph by a padding-sized (not
     * overhang-sized) amount, which varies per glyph because ink width varies per glyph. That
     * variable inflation was the actual root cause of the reported uneven letter spacing.
     */
    override fun advanceFor(char: Char, glyphPx: Float): Float {
        val advanceEm = advancesByChar[char] ?: advancesByChar[data.fallbackChar] ?: 1f
        // Clamp against the INK right edge, not the render quad's: the quad carries the crop
        // bleed, and clamping to it would inflate spacing by a bleed-sized, per-glyph amount --
        // the exact variable-inflation failure described above.
        val ink = inkByChar[char] ?: inkByChar[data.fallbackChar]
        val rightEdgeEm = if (ink != null) ink.offsetXEm + ink.widthEm else 0f
        return glyphPx * maxOf(advanceEm, rightEdgeEm)
    }

    private fun decodeAtlasPixels(): ByteArray {
        val normalizedBase64 = data.encodedAtlasBase64.filterNot(Char::isWhitespace)
        val decoded = Base64.Default.decode(normalizedBase64)
        // An MTSDF atlas is already RGBA -- RGB carries the multi-channel distance the shader
        // takes a median of, A a true signed distance. Handing it back verbatim is the point;
        // rewriting RGB to white (as the coverage path must) would erase the distance field.
        if (data.atlasChannels == RGBA_CHANNELS) return decoded
        val rgba = ByteArray(decoded.size * RGBA_CHANNELS)
        var sourceIndex = 0
        var targetIndex = 0
        while (sourceIndex < decoded.size) {
            rgba[targetIndex] = -1
            rgba[targetIndex + 1] = -1
            rgba[targetIndex + 2] = -1
            rgba[targetIndex + 3] = decoded[sourceIndex]
            sourceIndex += 1
            targetIndex += RGBA_CHANNELS
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
                heightEm = data.quadMetricsEm[metricIndex + 3],
            )
            uvIndex += 4
            metricIndex += 4
        }
        return result
    }

    private fun decodeInkBounds(): Map<Char, InkBounds> {
        val result = LinkedHashMap<Char, InkBounds>(data.glyphOrder.length)
        var metricIndex = 0
        data.glyphOrder.forEach { char ->
            result[char] = InkBounds(
                offsetXEm = data.inkMetricsEm[metricIndex],
                offsetYEm = data.inkMetricsEm[metricIndex + 1],
                widthEm = data.inkMetricsEm[metricIndex + 2],
                heightEm = data.inkMetricsEm[metricIndex + 3],
            )
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

    /** Outline-exact ink rect, pen/line-top relative in em -- the measurement-facing twin of
     * the render-facing [GlyphRect]. */
    private data class InkBounds(
        val offsetXEm: Float,
        val offsetYEm: Float,
        val widthEm: Float,
        val heightEm: Float,
    )

    /** Cap height is the reference capital's own ink height -- flat top, flat bottom, so the
     * ink rect spans exactly baseline-to-cap with no overshoot to subtract. Read from ink, not
     * the render quad: the quad carries crop bleed and would overstate the metric. */
    private fun measureCapHeightEm(): Float =
        BASELINE_REFERENCE_GLYPHS.firstNotNullOfOrNull { char ->
            inkByChar[char]?.heightEm?.takeIf { it > 0f }
        } ?: super.capHeightEm

    private fun computeVisibleBand(): Pair<Float, Float> {
        var metricIndex = 0
        var top = Float.POSITIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        repeat(data.glyphOrder.length) {
            val offsetYEm = data.inkMetricsEm[metricIndex + 1]
            val heightEm = data.inkMetricsEm[metricIndex + 3]
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

    private companion object {
        /** Flat-bottomed capitals, in preference order. Ordered by how reliably a font draws
         * them without overshoot or optical correction. */
        val BASELINE_REFERENCE_GLYPHS = listOf('H', 'I', 'E', 'T', 'X')
        const val RGBA_CHANNELS = 4
    }
}
