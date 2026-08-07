// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.shimmerBand
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.intersect
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.util.LruCache

/** Draws [label] as a row of glyph quads. */
enum class UiTextWrap {
    None,
    Word
}

enum class UiTextOverflow {
    Visible,
    Clip,
    Ellipsis
}

internal fun UiScope.renderTextBlock(
    label: String,
    slot: UiBounds,
    font: UiFont,
    color: Color?,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = 1,
    textStyle: TextStyle,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text,
    shimmer: Boolean = false,
    textStyleToken: String? = null,
    backgroundToken: String? = null,
    foregroundToken: String? = null,
    borderToken: String? = null
) : UiBounds {
    val glyphPx = resolveGlyphPx(font, textStyle)
    val layout = cachedLayoutBitmapText(
        label = label,
        glyphPx = glyphPx,
        maxWidthPx = slot.width,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        font = font
    )
    val lineGap = glyphPx * 0.25f
    val blockMetrics = measureTextBlock(layout, font, glyphPx, lineGap)
    val shouldClip = wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || maxLines > 1
    val contentBounds = resolveTextContentBounds(
        slot = slot,
        lineWidths = layout.lineWidths,
        blockHeight = blockMetrics.heightPx,
        verticallyCentered = verticallyCentered,
        centered = centered
    )
    val clippedBounds = if (shouldClip) contentBounds.intersect(slot) else contentBounds

    val textColor = color ?: context.currentTextStyle.color ?: context.currentTheme.colors.foreground

    recordSemantic(
        role = semanticRole,
        id = semanticId,
        label = label,
        bounds = slot,
        contentBounds = contentBounds,
        clippedBounds = clippedBounds,
        truncated = layout.truncated,
        lineCount = layout.lines.size,
        foregroundColor = textColor,
        textStyleToken = textStyleToken,
        backgroundToken = backgroundToken,
        foregroundToken = foregroundToken,
        borderToken = borderToken
    )

    fun emitLinesInternal(drawColor: Color, shimmerGradient: UiLinearGradient? = null, shimmerX: Float? = null, shimmerWidth: Float? = null) {
        var penY = if (verticallyCentered) {
            slot.y + (slot.height - blockMetrics.heightPx) / 2f - blockMetrics.topPx
        } else {
            slot.y - blockMetrics.topPx
        }
        layout.lines.forEachIndexed { index, line ->
            val textWidth = layout.lineWidths[index]
            var penX = if (centered) slot.x + (slot.width - textWidth) / 2f else slot.x
            for (char in line) {
                val glyph = font.uvFor(char)
                val advance = font.advanceFor(char, glyphPx)
                if (glyph != null) {
                    val glyphX = penX + glyph.offsetXEm * glyphPx
                    val glyphY = penY + glyph.offsetYEm * glyphPx
                    val glyphW = glyph.widthEm * glyphPx
                    val glyphH = glyph.heightEm * glyphPx
                    
                    var finalColor = drawColor
                    
                    if (shimmerGradient != null && shimmerX != null && shimmerWidth != null) {
                        val glyphCenterX = glyphX + glyphW / 2f
                        val relX = (glyphCenterX - shimmerX) / shimmerWidth
                        if (relX < 0f || relX > 1f) {
                            penX += advance
                            continue
                        }
                        
                        // 3-point horizontal sweep: Transparent -> Accent (topRight) -> Transparent
                        val peak = 0.5f
                        val highlight = if (relX < peak) {
                            shimmerGradient.topLeft.lerp(shimmerGradient.topRight, relX / peak)
                        } else {
                            shimmerGradient.topRight.lerp(shimmerGradient.bottomRight, (relX - peak) / (1f - peak))
                        }
                        finalColor = highlight
                    }

                    emit(
                        UiDrawPrimitive.Glyph(
                            pixelPerfectPixel(glyphX),
                            pixelPerfectPixel(glyphY),
                            pixelPerfectPixel(glyphW).coerceAtLeast(1f),
                            pixelPerfectPixel(glyphH).coerceAtLeast(1f),
                            glyph.u0,
                            glyph.v0,
                            glyph.u1,
                            glyph.v1,
                            finalColor,
                            tokenId = textStyleToken
                        )
                    )
                }
                penX += advance
            }
            penY += glyphPx + lineGap
        }
    }

    fun drawAllPasses() {
        emitLinesInternal(textColor)

        // --- Optional Shimmer Sweep ---
        // `shimmer` here is a plain Boolean draw-pass flag; the modifier-level decoupling lives
        // in UiModifier.graphicsLayer / UiGraphicsLayer (see modifier/GraphicsLayer.kt and
        // shadcnShimmer in modifier/StyleModifiers.kt) -- callers resolve the effect there and
        // pass the resulting boolean down into this low-level glyph emitter.
        if (shimmer && semanticId != null) {
            // Phase/band math now lives in the widget-agnostic
            // io.github.ronjunevaldoz.awake.ui.graphics.shimmerBand (one-directional sweep loop,
            // 0 -> 1, snap back to 0, repeat -- not a ping-pong bounce). The per-glyph 3-point
            // lerp below stays text-specific.
            val band = shimmerBand(id = semanticId, slot = slot)

            val highlightColor = Color.White.withAlpha(0.6f)

            // High-contrast white-ish shimmer for visibility over any text color
            val gradient = UiLinearGradient(
                topLeft = Color.Transparent,
                topRight = highlightColor,
                bottomRight = Color.Transparent,
                bottomLeft = Color.Transparent
            )

            emitLinesInternal(textColor, gradient, band.x, band.width)
        }
    }

    if (shouldClip) {
        clip(slot) {
            drawAllPasses()
        }
    } else {
        drawAllPasses()
    }

    return slot
}

private fun resolveTextContentBounds(
    slot: UiBounds,
    lineWidths: List<Float>,
    blockHeight: Float,
    verticallyCentered: Boolean,
    centered: Boolean
): UiBounds {
    val top = if (verticallyCentered) {
        slot.y + (slot.height - blockHeight) / 2f
    } else {
        slot.y
    }
    val left = if (lineWidths.isEmpty()) {
        slot.x
    } else {
        lineWidths.minOf { lineWidth ->
            if (centered) slot.x + (slot.width - lineWidth) / 2f else slot.x
        }
    }
    val right = if (lineWidths.isEmpty()) {
        slot.x
    } else {
        lineWidths.maxOf { lineWidth ->
            val lineLeft = if (centered) slot.x + (slot.width - lineWidth) / 2f else slot.x
            lineLeft + lineWidth
        }
    }
    return io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        x = left,
        y = top,
        width = (right - left).coerceAtLeast(0f),
        height = blockHeight.coerceAtLeast(0f)
    )
}

/**
 * Memoizes [layoutBitmapText] for [renderTextBlock]'s call shape specifically (the one call site
 * with direct access to a [UiFont] rather than an opaque `advanceOf` closure). Pure function of
 * its parameters -- see docs/tasks/2026-08-03-text-layout-measure-cache.md -- so a plain
 * process-lifetime LRU is safe: no closures, no state reads, no per-`UiContext`/per-frame lifetime
 * needed. Bounded so unbounded/dynamic `label`s (text field input, live counters) can't grow this
 * without limit; those naturally miss every frame anyway since their key changes every frame.
 *
 * Relies on [UiFont] instances being stable/shared across callers asking for "the same" font
 * (true for `UiFonts.default()` since it was memoized per `cellSize` -- see commit `3f51404a`);
 * a `UiFont` implementation that hands out a fresh instance per call would silently defeat this
 * cache (safe, just a permanent miss, not a correctness bug).
 */
private data class TextLayoutCacheKey(
    val label: String,
    val glyphPx: Float,
    val maxWidthPx: Float,
    val wrap: UiTextWrap,
    val overflow: UiTextOverflow,
    val maxLines: Int,
    val font: UiFont
)

private val textLayoutCache = LruCache<TextLayoutCacheKey, UiBitmapTextLayout>(maxSize = 256)

// Hit/miss counters live here, not inside LruCache itself -- LruCache stays a plain, stats-free
// mechanism; only this specific cache's callers (a debug perf overlay) care about its
// effectiveness. See LruCache.containsKey's doc comment for why this is observable without
// LruCache tracking anything itself.
private var textLayoutCacheHits = 0
private var textLayoutCacheMisses = 0

/** For a debug perf overlay (`GameUiRuntime.drawPerfStatsOverlay`) to report cache
 * effectiveness -- not used by any rendering path itself. */
fun textLayoutCacheStats(): Pair<Int, Int> = textLayoutCacheHits to textLayoutCacheMisses

private fun cachedLayoutBitmapText(
    label: String,
    glyphPx: Float,
    maxWidthPx: Float,
    wrap: UiTextWrap,
    overflow: UiTextOverflow,
    maxLines: Int,
    font: UiFont
): UiBitmapTextLayout {
    val key = TextLayoutCacheKey(label, glyphPx, maxWidthPx, wrap, overflow, maxLines, font)
    if (textLayoutCache.containsKey(key)) textLayoutCacheHits++ else textLayoutCacheMisses++
    return textLayoutCache.getOrPut(key) {
        layoutBitmapText(
            label = label,
            glyphPx = glyphPx,
            maxWidthPx = maxWidthPx,
            wrap = wrap,
            overflow = overflow,
            maxLines = maxLines,
            advanceOf = { char -> font.advanceFor(char, glyphPx) }
        )
    }
}

data class UiBitmapTextLayout(
    val lines: List<String>,
    val lineWidths: List<Float>,
    val truncated: Boolean
) {
    fun blockHeight(glyphPx: Float, lineGap: Float): Float {
        if (lines.isEmpty()) {
            return 0f
        }
        return lines.size * glyphPx + (lines.size - 1) * lineGap
    }
}

private data class UiMeasuredTextBlock(
    val topPx: Float,
    val heightPx: Float
)

private fun measureTextBlock(
    layout: UiBitmapTextLayout,
    font: UiFont,
    glyphPx: Float,
    lineGap: Float
): UiMeasuredTextBlock {
    if (layout.lines.isEmpty()) {
        return UiMeasuredTextBlock(topPx = 0f, heightPx = 0f)
    }
    var blockTopPx = Float.POSITIVE_INFINITY
    var blockBottomPx = Float.NEGATIVE_INFINITY
    layout.lines.forEachIndexed { index, line ->
        val (lineTopEm, lineBottomEm) = measureVisibleLineBandEm(line, font)
        val lineOriginY = index * (glyphPx + lineGap)
        blockTopPx = minOf(blockTopPx, lineOriginY + lineTopEm * glyphPx)
        blockBottomPx = maxOf(blockBottomPx, lineOriginY + lineBottomEm * glyphPx)
    }
    if (!blockTopPx.isFinite() || !blockBottomPx.isFinite() || blockBottomPx <= blockTopPx) {
        return UiMeasuredTextBlock(topPx = 0f, heightPx = glyphPx)
    }
    return UiMeasuredTextBlock(
        topPx = blockTopPx,
        heightPx = blockBottomPx - blockTopPx
    )
}

private fun measureVisibleLineBandEm(line: String, font: UiFont): Pair<Float, Float> {
    // Always use the font's consistent vertical band (ascent to descent), NOT per-string
    // character bounding boxes. Per-string character bounding boxes caused text with
    // descenders ('y', 'g', 'p') or symbols ('*') to calculate different line heights and
    // baseline offsets than plain text ('Name'), misaligning adjacent labels in Rows and
    // clipping descenders at the bottom of line slots.
    return font.visibleTopEm to font.visibleBottomEm
}

fun layoutBitmapText(
    label: String,
    glyphPx: Float,
    maxWidthPx: Float,
    wrap: UiTextWrap,
    overflow: UiTextOverflow,
    maxLines: Int,
    trim: Boolean = true,
    advanceOf: (Char) -> Float = { glyphPx }
): UiBitmapTextLayout {
    val normalizedMaxLines = maxLines.coerceAtLeast(1)
    val safeMaxWidthPx = if (glyphPx <= 0f || maxWidthPx <= 0f) {
        Float.POSITIVE_INFINITY
    } else {
        maxWidthPx.coerceAtLeast(glyphPx)
    }
    val result = ArrayList<String>()
    var truncated = false

    val paragraphs = label.split('\n')
    paragraphs.forEachIndexed { paragraphIndex, paragraph ->
        if (result.size >= normalizedMaxLines) {
            truncated = true
            return@forEachIndexed
        }
        if (wrap == UiTextWrap.None) {
            val line = truncateLine(paragraph, safeMaxWidthPx, overflow, advanceOf)
            truncated = truncated || line != paragraph
            result += line
            return@forEachIndexed
        }

        var remaining = paragraph
        if (remaining.isEmpty()) {
            result += ""
        }
        while (remaining.isNotEmpty() && result.size < normalizedMaxLines) {
            if (measureLineWidth(remaining, advanceOf) <= safeMaxWidthPx) {
                result += remaining
                remaining = ""
                break
            }
            val fitIndex = fitPrefixByWidth(remaining, safeMaxWidthPx, advanceOf).coerceAtLeast(1)
            val splitIndex = remaining.substring(0, fitIndex).lastIndexOf(' ').takeIf { it > 0 } ?: fitIndex
            val line = if (trim) remaining.substring(0, splitIndex).trimEnd() else remaining.substring(0, splitIndex)
            val safeLine = if (line.isEmpty() && trim) remaining.substring(0, fitIndex).trimEnd() else if (line.isEmpty()) remaining.substring(0, fitIndex) else line
            result += safeLine
            remaining = if (trim) remaining.substring(splitIndex).trimStart() else remaining.substring(splitIndex)
        }
        if (remaining.isNotEmpty()) {
            truncated = true
        }
        if (paragraphIndex < paragraphs.lastIndex && result.size >= normalizedMaxLines) {
            truncated = true
        }
    }

    if (result.isEmpty()) {
        result += ""
    }
    if (truncated) {
        val lastIndex = result.lastIndex
        result[lastIndex] = when (overflow) {
            UiTextOverflow.Visible -> result[lastIndex]
            UiTextOverflow.Clip -> truncateLine(result[lastIndex], safeMaxWidthPx, UiTextOverflow.Clip, advanceOf)
            UiTextOverflow.Ellipsis -> ellipsizeTruncatedLine(result[lastIndex], safeMaxWidthPx, advanceOf)
        }
    }
    val finalLines = result.take(normalizedMaxLines)
    return UiBitmapTextLayout(
        lines = finalLines,
        lineWidths = finalLines.map { line -> measureLineWidth(line, advanceOf) },
        truncated = truncated
    )
}

private fun truncateLine(
    line: String,
    maxWidthPx: Float,
    overflow: UiTextOverflow,
    advanceOf: (Char) -> Float
): String {
    if (maxWidthPx.isInfinite() || measureLineWidth(line, advanceOf) <= maxWidthPx) {
        return line
    }
    return when (overflow) {
        UiTextOverflow.Visible -> line
        UiTextOverflow.Clip -> line.take(fitPrefixByWidth(line, maxWidthPx, advanceOf))
        UiTextOverflow.Ellipsis -> ellipsizeLine(line, maxWidthPx, advanceOf)
    }
}

private fun ellipsizeLine(line: String, maxWidthPx: Float, advanceOf: (Char) -> Float): String {
    if (maxWidthPx.isInfinite() || measureLineWidth(line, advanceOf) <= maxWidthPx) {
        return line
    }
    val ellipsis = "..."
    val ellipsisWidth = measureLineWidth(ellipsis, advanceOf)
    if (ellipsisWidth >= maxWidthPx) {
        return ellipsis.take(fitPrefixByWidth(ellipsis, maxWidthPx, advanceOf))
    }
    val prefixCount = fitPrefixByWidth(line, maxWidthPx - ellipsisWidth, advanceOf)
    return line.take(prefixCount) + ellipsis
}

private fun ellipsizeTruncatedLine(line: String, maxWidthPx: Float, advanceOf: (Char) -> Float): String {
    val ellipsis = "..."
    if (maxWidthPx.isInfinite()) {
        return "$line$ellipsis"
    }
    val ellipsisWidth = measureLineWidth(ellipsis, advanceOf)
    val currentWidth = measureLineWidth(line, advanceOf)
    if (currentWidth + ellipsisWidth <= maxWidthPx) {
        return "$line$ellipsis"
    }
    if (ellipsisWidth >= maxWidthPx) {
        return ellipsis.take(fitPrefixByWidth(ellipsis, maxWidthPx, advanceOf))
    }
    val prefixCount = fitPrefixByWidth(line, maxWidthPx - ellipsisWidth, advanceOf)
    return line.take(prefixCount) + ellipsis
}

private fun fitPrefixByWidth(line: String, maxWidthPx: Float, advanceOf: (Char) -> Float): Int {
    var width = 0f
    line.forEachIndexed { index, char ->
        val nextWidth = width + advanceOf(char)
        if (nextWidth > maxWidthPx) {
            return index
        }
        width = nextWidth
    }
    return line.length
}

private fun measureLineWidth(line: String, advanceOf: (Char) -> Float): Float {
    var width = 0f
    line.forEach { char -> width += advanceOf(char) }
    return width
}
