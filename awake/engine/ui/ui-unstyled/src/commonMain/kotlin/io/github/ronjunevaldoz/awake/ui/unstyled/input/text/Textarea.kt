// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.UiTextEditAction
import io.github.ronjunevaldoz.awake.ui.clearFocusIfMatches
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.inputState
import io.github.ronjunevaldoz.awake.ui.isFocused
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.pointerDownEdge
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.requestFocus
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.interact
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS = 1f
private const val TEXT_FIELD_CARET_WIDTH_PX = 1.5f

/**
 * Multi-line text input. Immediate-mode like [textField].
 * Handles manual newlines (\n) and cursor navigation across lines.
 * Wrapping is not yet implemented (fixed-width layout for now).
 */
fun UiScope.textarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3
): String {
    // Determine height based on minLines
    val resolvedDefaults = theme.components.textField
    val resolved = resolveStyle(
        style = style,
        defaults = resolvedDefaults,
        state = MutableStyleState(disabled = !enabled)
    )
    val fontHeight = resolveGlyphPx(font, resolved.textStyle)
    val padding = resolved.contentPadding
    val totalPadding = padding.top + padding.bottom
    val lineGap = fontHeight * 0.25f
    val minHeight =
        (fontHeight * minLines) + (lineGap * (minLines - 1)).coerceAtLeast(0f) + totalPadding.toPx()

    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(minHeight.px))
    )

    val focused = enabled && isFocused(id)
    if (!enabled) {
        clearFocusIfMatches(id)
    } else if (pointerDownEdge() && interaction.hovered) {
        requestFocus(id)
    }

    val styleState = MutableStyleState(
        hovered = interaction.hovered || modifier.forceHover == true,
        focused = focused,
        disabled = !enabled
    )
    val resolvedWithInteraction = resolveStyle(
        style = style,
        defaults = resolvedDefaults,
        state = styleState
    )

    val borderColor =
        if (isError) theme.tokens.destructive else (resolvedWithInteraction.borderColor
            ?: theme.tokens.border)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = resolvedWithInteraction.background ?: theme.tokens.background,
        radiusPx = resolvedWithInteraction.shape.toPx(),
        borderWidth = if (focused || isError) 1.5f.dp else resolvedWithInteraction.borderWidth,
        borderColor = borderColor,
        shapeSpec = resolvedWithInteraction.shapeSpec
    )

    val resolvedFont = font
    val glyphPx = fontHeight
    val contentSlot = interaction.slot.inset(padding)

    val cursorState = widgetState(id)
    var cursor = cursorState.get("cursor", value.length).coerceIn(0, value.length)

    val layout = layoutBitmapText(
        label = value,
        glyphPx = glyphPx,
        maxWidthPx = contentSlot.width,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Clip,
        maxLines = Int.MAX_VALUE,
        trim = false,
        advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) }
    )

    var nextValue = value
    if (focused) {
        val clickIndex =
            if (interaction.clicked || (pointerDownEdge() && interaction.hovered)) {
                indexForPointerXY(
                    layout,
                    value,
                    resolvedFont,
                    glyphPx,
                    lineGap,
                    contentSlot,
                    inputState.pointerX,
                    inputState.pointerY
                )
            } else {
                null
            }
        if (clickIndex != null) {
            cursor = clickIndex
        }

        inputState.editActions.forEach { action ->
            when (action) {
                UiTextEditAction.Backspace -> if (cursor > 0) {
                    nextValue = nextValue.substring(0, cursor - 1) + nextValue.substring(cursor)
                    cursor -= 1
                }

                UiTextEditAction.Delete -> if (cursor < nextValue.length) {
                    nextValue = nextValue.substring(0, cursor) + nextValue.substring(cursor + 1)
                }

                UiTextEditAction.ArrowLeft -> cursor = (cursor - 1).coerceAtLeast(0)
                UiTextEditAction.ArrowRight -> cursor = (cursor + 1).coerceAtMost(nextValue.length)
                UiTextEditAction.ArrowUp -> cursor = moveCursorVertical(
                    layout,
                    nextValue,
                    resolvedFont,
                    glyphPx,
                    lineGap,
                    cursor,
                    -1
                )

                UiTextEditAction.ArrowDown -> cursor =
                    moveCursorVertical(layout, nextValue, resolvedFont, glyphPx, lineGap, cursor, 1)

                UiTextEditAction.Home -> cursor = cursorForLineStart(layout, nextValue, cursor)
                UiTextEditAction.End -> cursor = cursorForLineEnd(layout, nextValue, cursor)
                UiTextEditAction.Enter -> {
                    nextValue = nextValue.substring(0, cursor) + "\n" + nextValue.substring(cursor)
                    cursor += 1
                }
            }
        }
        val typed = inputState.typedText
        if (typed.isNotEmpty()) {
            nextValue = nextValue.substring(0, cursor) + typed + nextValue.substring(cursor)
            cursor += typed.length
        }
        cursor = cursor.coerceIn(0, nextValue.length)
    }
    cursorState.set("cursor", cursor)

    clip(contentSlot) {
        val showingPlaceholder = nextValue.isEmpty() && !focused
        val displayed = if (showingPlaceholder) placeholder else nextValue
        if (displayed.isNotEmpty()) {
            text(
                label = displayed,
                slot = contentSlot,
                font = resolvedFont,
                color = if (showingPlaceholder) theme.tokens.mutedForeground else (resolvedWithInteraction.foreground
                    ?: theme.tokens.foreground),
                verticallyCentered = false,
                overflow = UiTextOverflow.Clip,
                wrap = UiTextWrap.Word,
                textStyle = resolvedWithInteraction.textStyle,
                semanticId = "$id.value",
                maxLines = Int.MAX_VALUE
            )
        }
        if (focused) {
            val elapsed = caretBlinkElapsedSeconds(id)
            val caretVisible =
                (elapsed % TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS) < TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS / 2f
            if (caretVisible) {
                val caretPos = cursorPositionPx(
                    layout,
                    nextValue,
                    resolvedFont,
                    glyphPx,
                    lineGap,
                    contentSlot,
                    cursor
                )
                emitFillAndBorder(
                    slot = UiSlot(
                        caretPos.first,
                        caretPos.second,
                        TEXT_FIELD_CARET_WIDTH_PX,
                        glyphPx
                    ),
                    fillColor = resolvedWithInteraction.foreground ?: theme.tokens.foreground,
                    radiusPx = 0f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent
                )
            }
        }
    }

    recordSemantic(
        role = UiSemanticRole.Text,
        id = id,
        label = nextValue.ifEmpty { placeholder },
        bounds = interaction.slot,
        contentBounds = contentSlot,
        selected = focused,
        lineCount = layout.lines.size
    )
    return nextValue
}

private fun UiScope.caretBlinkElapsedSeconds(id: String): Float {
    val state = widgetState(id)
    val elapsed = state.get("caretElapsed", 0f) + frameDeltaSeconds()
    state.set("caretElapsed", elapsed)
    return elapsed
}

private fun cursorToLineAndCol(
    layout: UiBitmapTextLayout,
    value: String,
    cursor: Int
): Pair<Int, Int> {
    // layout.lines contains the visual lines. 
    // Since trim = false, layoutBitmapText splits by \n first, then by width.
    // Each \n in the original string creates a new line in layout.lines.
    // If a line was split due to wrapping, no \n was consumed.

    var currentOriginalIdx = 0
    for (i in layout.lines.indices) {
        val line = layout.lines[i]
        if (currentOriginalIdx + line.length >= cursor) {
            return i to (cursor - currentOriginalIdx)
        }
        currentOriginalIdx += line.length
        // Check if there was a \n in the original string at this point
        if (currentOriginalIdx < value.length && value[currentOriginalIdx] == '\n') {
            currentOriginalIdx++ // Skip the \n
        }
    }

    return layout.lines.lastIndex.coerceAtLeast(0) to (layout.lines.lastOrNull()?.length ?: 0)
}

private fun cursorPositionPx(
    layout: UiBitmapTextLayout,
    value: String,
    font: UiFont,
    glyphPx: Float,
    lineGap: Float,
    contentSlot: UiSlot,
    cursor: Int
): Pair<Float, Float> {
    val (lineIdx, colIdx) = cursorToLineAndCol(layout, value, cursor)
    val line = layout.lines.getOrNull(lineIdx) ?: ""
    var x = contentSlot.x
    for (i in 0 until colIdx.coerceAtMost(line.length)) {
        x += font.advanceFor(line[i], glyphPx)
    }
    val y = contentSlot.y + lineIdx * (glyphPx + lineGap)
    return x to y
}

private fun indexForPointerXY(
    layout: UiBitmapTextLayout,
    value: String,
    font: UiFont,
    glyphPx: Float,
    lineGap: Float,
    contentSlot: UiSlot,
    pointerX: Float,
    pointerY: Float
): Int {
    val lineIdx = ((pointerY - contentSlot.y) / (glyphPx + lineGap)).toInt()
        .coerceIn(0, layout.lines.lastIndex.coerceAtLeast(0))
    val line = layout.lines.getOrNull(lineIdx) ?: ""

    var advance = contentSlot.x
    var colIdx = line.length
    for (i in line.indices) {
        val charWidth = font.advanceFor(line[i], glyphPx)
        if (pointerX < advance + charWidth / 2f) {
            colIdx = i
            break
        }
        advance += charWidth
    }

    // Map (lineIdx, colIdx) back to original string index
    var currentOriginalIdx = 0
    for (i in 0 until lineIdx) {
        currentOriginalIdx += layout.lines[i].length
        if (currentOriginalIdx < value.length && value[currentOriginalIdx] == '\n') {
            currentOriginalIdx++
        }
    }
    return (currentOriginalIdx + colIdx).coerceIn(0, value.length)
}

private fun moveCursorVertical(
    layout: UiBitmapTextLayout,
    value: String,
    font: UiFont,
    glyphPx: Float,
    lineGap: Float,
    cursor: Int,
    direction: Int
): Int {
    val (lineIdx, colIdx) = cursorToLineAndCol(layout, value, cursor)
    val targetLineIdx = (lineIdx + direction).coerceIn(0, layout.lines.lastIndex.coerceAtLeast(0))
    if (targetLineIdx == lineIdx) return cursor

    val currentLine = layout.lines[lineIdx]
    var currentX = 0f
    for (i in 0 until colIdx.coerceAtMost(currentLine.length)) {
        currentX += font.advanceFor(currentLine[i], glyphPx)
    }

    val targetLine = layout.lines[targetLineIdx]
    var targetColIdx = targetLine.length
    var targetX = 0f
    for (i in targetLine.indices) {
        val charWidth = font.advanceFor(targetLine[i], glyphPx)
        if (targetX + charWidth / 2f > currentX) {
            targetColIdx = i
            break
        }
        targetX += charWidth
    }

    // Map (targetLineIdx, targetColIdx) back
    var currentOriginalIdx = 0
    for (i in 0 until targetLineIdx) {
        currentOriginalIdx += layout.lines[i].length
        if (currentOriginalIdx < value.length && value[currentOriginalIdx] == '\n') {
            currentOriginalIdx++
        }
    }
    return (currentOriginalIdx + targetColIdx).coerceIn(0, value.length)
}

private fun cursorForLineStart(layout: UiBitmapTextLayout, value: String, cursor: Int): Int {
    val (lineIdx, _) = cursorToLineAndCol(layout, value, cursor)
    var currentOriginalIdx = 0
    for (i in 0 until lineIdx) {
        currentOriginalIdx += layout.lines[i].length
        if (currentOriginalIdx < value.length && value[currentOriginalIdx] == '\n') {
            currentOriginalIdx++
        }
    }
    return currentOriginalIdx
}

private fun cursorForLineEnd(layout: UiBitmapTextLayout, value: String, cursor: Int): Int {
    val (lineIdx, _) = cursorToLineAndCol(layout, value, cursor)
    var currentOriginalIdx = 0
    for (i in 0..lineIdx) {
        currentOriginalIdx += layout.lines[i].length
        if (i < lineIdx && currentOriginalIdx < value.length && value[currentOriginalIdx] == '\n') {
            currentOriginalIdx++
        }
    }
    return currentOriginalIdx
}
