// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.math.floor

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

fun UiScope.text(
    label: String,
    slot: UiSlot? = null,
    font: BitmapFont? = this.font,
    color: Color = theme.tokens.foreground,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = 1,
    textScale: Float = this.textScale,
    textSize: Sp? = null
) {
    val resolvedFont = checkNotNull(font) { "text() requires a font, either from the UiScope or passed explicitly" }
    val glyphPx = resolveGlyphPx(resolvedFont, textScale, textSize)
    val resolvedSlot = slot ?: claimSlot(Dimension.FillMax, Dimension.Fixed(glyphPx.px))
    val layout = layoutBitmapText(
        label = label,
        glyphPx = glyphPx,
        maxWidthPx = resolvedSlot.width,
        wrap = wrap,
        overflow = overflow,
        maxLines = maxLines
    )
    val lineGap = glyphPx * 0.25f
    val blockHeight = layout.blockHeight(glyphPx, lineGap)
    val shouldClip = wrap != UiTextWrap.None || overflow != UiTextOverflow.Visible || maxLines > 1

    fun emitLines() {
        var penY = if (centered) resolvedSlot.y + (resolvedSlot.height - blockHeight) / 2f else resolvedSlot.y
        layout.lines.forEach { line ->
            val textWidth = line.length * glyphPx
            var penX = if (centered) resolvedSlot.x + (resolvedSlot.width - textWidth) / 2f else resolvedSlot.x
            for (char in line) {
                val uv = resolvedFont.uvFor(char)
                if (uv != null) {
                    emit(UiDrawPrimitive.Glyph(penX, penY, glyphPx, glyphPx, uv.u0, uv.v0, uv.u1, uv.v1, color))
                }
                penX += glyphPx
            }
            penY += glyphPx + lineGap
        }
    }

    if (shouldClip) {
        clip(resolvedSlot) {
            emitLines()
        }
    } else {
        emitLines()
    }
}

data class UiBitmapTextLayout(
    val lines: List<String>,
    val truncated: Boolean
) {
    fun blockHeight(glyphPx: Float, lineGap: Float): Float {
        if (lines.isEmpty()) {
            return 0f
        }
        return lines.size * glyphPx + (lines.size - 1) * lineGap
    }
}

fun layoutBitmapText(
    label: String,
    glyphPx: Float,
    maxWidthPx: Float,
    wrap: UiTextWrap,
    overflow: UiTextOverflow,
    maxLines: Int
): UiBitmapTextLayout {
    val normalizedMaxLines = maxLines.coerceAtLeast(1)
    val capacity = if (glyphPx <= 0f || maxWidthPx <= 0f) {
        Int.MAX_VALUE
    } else {
        floor(maxWidthPx / glyphPx).toInt().coerceAtLeast(1)
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
            val line = truncateLine(paragraph, capacity, overflow)
            truncated = truncated || line != paragraph
            result += line
            return@forEachIndexed
        }

        var remaining = paragraph
        if (remaining.isEmpty()) {
            result += ""
        }
        while (remaining.isNotEmpty() && result.size < normalizedMaxLines) {
            if (remaining.length <= capacity) {
                result += remaining
                remaining = ""
                break
            }
            val splitIndex = remaining.substring(0, capacity.coerceAtMost(remaining.length))
                .lastIndexOf(' ')
                .takeIf { it > 0 }
                ?: capacity
            val line = remaining.substring(0, splitIndex).trimEnd()
            val safeLine = if (line.isEmpty()) remaining.substring(0, capacity) else line
            result += safeLine
            remaining = remaining.substring(safeLine.length).trimStart()
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
            UiTextOverflow.Clip -> truncateLine(result[lastIndex], capacity, UiTextOverflow.Clip)
            UiTextOverflow.Ellipsis -> ellipsizeTruncatedLine(result[lastIndex], capacity)
        }
    }
    return UiBitmapTextLayout(lines = result.take(normalizedMaxLines), truncated = truncated)
}

private fun truncateLine(
    line: String,
    capacity: Int,
    overflow: UiTextOverflow
): String {
    if (capacity == Int.MAX_VALUE || line.length <= capacity) {
        return line
    }
    return when (overflow) {
        UiTextOverflow.Visible -> line
        UiTextOverflow.Clip -> line.take(capacity)
        UiTextOverflow.Ellipsis -> ellipsizeLine(line, capacity)
    }
}

private fun ellipsizeLine(line: String, capacity: Int): String {
    if (capacity == Int.MAX_VALUE || line.length <= capacity) {
        return line
    }
    if (capacity <= 3) {
        return ".".repeat(capacity)
    }
    return line.take(capacity - 3) + "..."
}

private fun ellipsizeTruncatedLine(line: String, capacity: Int): String {
    if (capacity == Int.MAX_VALUE) {
        return "$line..."
    }
    if (capacity <= 3) {
        return ".".repeat(capacity)
    }
    if (line.length >= capacity) {
        return line.take(capacity - 3) + "..."
    }
    return line.take((capacity - 3).coerceAtLeast(0)) + "..."
}
