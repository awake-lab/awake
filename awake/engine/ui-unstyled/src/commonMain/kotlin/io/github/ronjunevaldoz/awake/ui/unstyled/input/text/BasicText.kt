// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.clip
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.intersect
import io.github.ronjunevaldoz.awake.ui.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

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
    slot: UiSlot,
    font: UiFont,
    color: Color?,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = 1,
    textStyle: TextStyle,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text
) : UiSlot {
    val glyphPx = resolveGlyphPx(font, textStyle)
    val layout = layoutBitmapText(
        label = label,
        glyphPx = glyphPx,
        maxWidthPx = slot.width,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        advanceOf = { char -> font.advanceFor(char, glyphPx) }
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

    recordSemantic(
        role = semanticRole,
        id = semanticId,
        label = label,
        bounds = slot,
        contentBounds = contentBounds,
        clippedBounds = clippedBounds,
        truncated = layout.truncated,
        lineCount = layout.lines.size
    )

    val textColor = color ?: context.currentTheme.tokens.foreground
    fun emitLines() {
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
                if (glyph != null) {
                    emit(
                        UiDrawPrimitive.Glyph(
                            pixelPerfectPixel(penX + glyph.offsetXEm * glyphPx),
                            pixelPerfectPixel(penY + glyph.offsetYEm * glyphPx),
                            pixelPerfectPixel(glyph.widthEm * glyphPx).coerceAtLeast(1f),
                            pixelPerfectPixel(glyph.heightEm * glyphPx).coerceAtLeast(1f),
                            glyph.u0,
                            glyph.v0,
                            glyph.u1,
                            glyph.v1,
                            textColor
                        )
                    )
                }
                penX += font.advanceFor(char, glyphPx)
            }
            penY += glyphPx + lineGap
        }
    }

    if (shouldClip) {
        clip(slot) {
            emitLines()
        }
    } else {
        emitLines()
    }
    return slot
}

@Deprecated(
    message = "Use text(...) for component-level text and reserve basicText(...) for low-level glyph primitives only.",
    replaceWith = ReplaceWith("text(label = label, slot = slot ?: claimSlot(Dimension.FillMax, Dimension.Fixed(resolveGlyphPx(checkNotNull(font), textStyle).px)), font = checkNotNull(font), color = color, centered = centered, verticallyCentered = verticallyCentered, wrap = wrap, overflow = overflow, maxLines = maxLines, textStyle = textStyle, semanticId = semanticId, semanticRole = semanticRole)")
)
fun UiScope.basicText(
    label: String,
    slot: UiSlot? = null,
    font: UiFont? = context.currentFont,
    color: Color? = context.currentTextStyle.color,
    centered: Boolean = false,
    verticallyCentered: Boolean = centered,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = 1,
    textStyle: TextStyle = context.currentTextStyle,
    semanticId: String? = null,
    semanticRole: UiSemanticRole = UiSemanticRole.Text
): UiSlot {
    val resolvedFont = checkNotNull(font) { "text() requires a font, either from the UiScope or passed explicitly" }
    val glyphPx = resolveGlyphPx(resolvedFont, textStyle)
    val resolvedSlot = slot ?: claimSlot(Dimension.FillMax, Dimension.Fixed(glyphPx.px))
    return renderTextBlock(
        label = label,
        slot = resolvedSlot,
        font = resolvedFont,
        color = color,
        centered = centered,
        verticallyCentered = verticallyCentered,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines,
        textStyle = textStyle,
        semanticId = semanticId,
        semanticRole = semanticRole
    )
}

private fun resolveTextContentBounds(
    slot: UiSlot,
    lineWidths: List<Float>,
    blockHeight: Float,
    verticallyCentered: Boolean,
    centered: Boolean
): UiSlot {
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
    return UiSlot(
        x = left,
        y = top,
        width = (right - left).coerceAtLeast(0f),
        height = blockHeight.coerceAtLeast(0f)
    )
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
    var topEm = Float.POSITIVE_INFINITY
    var bottomEm = Float.NEGATIVE_INFINITY
    line.forEach { char ->
        val glyph = font.uvFor(char) ?: return@forEach
        if (glyph.heightEm <= 0f) {
            return@forEach
        }
        topEm = minOf(topEm, glyph.offsetYEm)
        bottomEm = maxOf(bottomEm, glyph.offsetYEm + glyph.heightEm)
    }
    if (!topEm.isFinite() || !bottomEm.isFinite() || bottomEm <= topEm) {
        return font.visibleTopEm to font.visibleBottomEm
    }
    return topEm to bottomEm
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
