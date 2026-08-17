// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiTextEditAction
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.layout.inset
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.clearFocusIfMatches
import io.github.ronjunevaldoz.awake.ui.scope.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.scope.inputState
import io.github.ronjunevaldoz.awake.ui.scope.isFocused
import io.github.ronjunevaldoz.awake.ui.scope.pointerDownEdge
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.requestFocus
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

private const val TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS = 1f
private const val TEXT_FIELD_CARET_WIDTH_PX = 1.5f

/**
 * Multi-line text input. Immediate-mode like [textField].
 * Handles manual newlines (\n), automatic wrapping, and cursor navigation across visual lines.
 */
fun UiPrimitiveScope.textarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3,
): String {
    // Determine height based on minLines
    // Reads no ambient theme -- shadcnTextarea's shadcnTextareaStyle already supplies a
    // complete Style (background/foreground/border/shape/contentPadding/textSize).
    val resolvedDefaults = Style.Empty
    val resolved = resolveStyle(
        style = style,
        defaults = resolvedDefaults,
        state = MutableStyleState(disabled = !enabled),
    )
    val fontHeight = resolveGlyphPx(font, resolved.textStyle)
    val padding = resolved.contentPadding
    val totalPadding = padding.top + padding.bottom
    val lineGap = fontHeight * 0.25f
    // minLines occupies minLines line BOXES, not minLines font sizes: Roboto's ink band is
    // ~1.19em, so reserving fontHeight per line leaves the text overflowing its own field.
    val minHeight =
        (fontHeight * font.lineHeightEm * minLines) +
            (lineGap * (minLines - 1)).coerceAtLeast(0f) + totalPadding.toPx()

    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(minHeight.px)),
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
        disabled = !enabled,
    )
    val resolvedWithInteraction = resolveStyle(
        style = style,
        defaults = resolvedDefaults,
        state = styleState,
    )

    val borderColor =
        if (isError) {
            theme.colors.destructive
        } else {
            (
                resolvedWithInteraction.borderColor
                    ?: theme.colors.border
                )
        }
    // Reference's `disabled:opacity-50` treatment, same single group-alpha shape as
    // `Buttons.kt`'s `buttonSlotInternal` -- covers the fill/border paint and the typed
    // text/caret as one composited unit so nothing drawn on top of the fill gets double-dimmed.
    return withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        emitFillAndBorder(
            slot = interaction.slot,
            fillColor = resolvedWithInteraction.background ?: theme.colors.background,
            radiusPx = resolvedWithInteraction.shape.toPx(),
            borderWidth = if (focused || isError) 1.5f.dp else resolvedWithInteraction.borderWidth,
            borderColor = borderColor,
            shapeSpec = resolvedWithInteraction.shapeSpec,
        )

        val resolvedFont = font
        val glyphPx = fontHeight
        val contentSlot = interaction.slot.inset(padding)

        val cursorState = widgetState(id)
        var cursor = cursorState.get("cursor", value.length).coerceIn(0, value.length)

        val previousLayout = layoutBitmapText(
            label = value,
            glyphPx = glyphPx,
            maxWidthPx = contentSlot.width,
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Clip,
            maxLines = Int.MAX_VALUE,
            trim = false,
            advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) },
        )

        // Read last frame's applied scroll offset before this frame's click hit-testing -- a click
        // lands on whatever was actually rendered (last frame's scrolled position), not this frame's
        // not-yet-recomputed one.
        val lastScrollOffsetY = cursorState.get("scrollOffsetY", 0f)
        val lastDrawSlot = UiBounds(
            contentSlot.x,
            contentSlot.y - lastScrollOffsetY,
            contentSlot.width,
            contentSlot.height,
        )

        var nextValue = value
        if (focused) {
            val clickIndex =
                if (interaction.clicked || (pointerDownEdge() && interaction.hovered)) {
                    indexForPointerXY(
                        previousLayout,
                        value,
                        resolvedFont,
                        glyphPx,
                        lineGap,
                        lastDrawSlot,
                        inputState.pointerX,
                        inputState.pointerY,
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
                    UiTextEditAction.ArrowRight ->
                        cursor =
                            (cursor + 1).coerceAtMost(nextValue.length)

                    UiTextEditAction.ArrowUp -> cursor = moveCursorVertical(
                        previousLayout,
                        nextValue,
                        resolvedFont,
                        glyphPx,
                        lineGap,
                        cursor,
                        -1,
                    )

                    UiTextEditAction.ArrowDown ->
                        cursor =
                            moveCursorVertical(
                                previousLayout,
                                nextValue,
                                resolvedFont,
                                glyphPx,
                                lineGap,
                                cursor,
                                1,
                            )

                    UiTextEditAction.Home ->
                        cursor =
                            cursorForLineStart(previousLayout, nextValue, cursor)

                    UiTextEditAction.End ->
                        cursor =
                            cursorForLineEnd(previousLayout, nextValue, cursor)

                    UiTextEditAction.Enter -> {
                        nextValue =
                            nextValue.substring(0, cursor) + "\n" + nextValue.substring(cursor)
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

        val currentLayout = if (nextValue == value) {
            previousLayout
        } else {
            layoutBitmapText(
                label = nextValue,
                glyphPx = glyphPx,
                maxWidthPx = contentSlot.width,
                wrap = UiTextWrap.Word,
                overflow = UiTextOverflow.Clip,
                maxLines = Int.MAX_VALUE,
                trim = false,
                advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) },
            )
        }

        // Vertical scroll: content taller than the box's fixed viewport (contentSlot) used to just
        // clip silently with no way to reach the hidden lines. Auto-follow the caret's line instead
        // -- the same "cursor scrolls into view" behavior a native multi-line text input gives you,
        // simpler than wiring a draggable scrollbar for a field whose scroll is driven by typing/
        // arrow-key position, not by dragging.
        val contentHeight = currentLayout.blockHeight(glyphPx * resolvedFont.lineHeightEm, lineGap)
        val maxScrollY = (contentHeight - contentSlot.height).coerceAtLeast(0f)
        var scrollOffsetY = lastScrollOffsetY.coerceIn(0f, maxScrollY)
        if (focused) {
            val (caretLineIdx, _) = cursorToLineAndCol(currentLayout, nextValue, cursor)
            val caretTop = caretLineIdx * (glyphPx * resolvedFont.lineHeightEm + lineGap)
            val caretBottom = caretTop + glyphPx * resolvedFont.lineHeightEm
            if (caretTop - scrollOffsetY < 0f) {
                scrollOffsetY = caretTop
            } else if (caretBottom - scrollOffsetY > contentSlot.height) {
                scrollOffsetY = caretBottom - contentSlot.height
            }
            scrollOffsetY = scrollOffsetY.coerceIn(0f, maxScrollY)
        }
        cursorState.set("scrollOffsetY", scrollOffsetY)
        val drawSlot = UiBounds(
            contentSlot.x,
            contentSlot.y - scrollOffsetY,
            contentSlot.width,
            maxOf(contentSlot.height, contentHeight),
        )

        clip(contentSlot) {
            val showingPlaceholder = nextValue.isEmpty() && !focused
            val displayed = if (showingPlaceholder) placeholder else nextValue
            if (displayed.isNotEmpty()) {
                text(
                    label = displayed,
                    slot = drawSlot,
                    font = resolvedFont,
                    color = if (showingPlaceholder) {
                        theme.colors.mutedForeground
                    } else {
                        (
                            resolvedWithInteraction.foreground
                                ?: theme.colors.foreground
                            )
                    },
                    verticallyCentered = false,
                    overflow = UiTextOverflow.Clip,
                    wrap = UiTextWrap.Word,
                    textStyle = resolvedWithInteraction.textStyle,
                    semanticId = "$id.value",
                    maxLines = Int.MAX_VALUE,
                )
            }
            if (focused) {
                val elapsed = caretBlinkElapsedSeconds(id)
                val caretVisible =
                    (elapsed % TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS) < TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS / 2f
                if (caretVisible) {
                    val caretPos = cursorPositionPx(
                        currentLayout,
                        nextValue,
                        resolvedFont,
                        glyphPx,
                        lineGap,
                        drawSlot,
                        cursor,
                    )
                    emitFillAndBorder(
                        slot = UiBounds(
                            caretPos.first,
                            caretPos.second,
                            TEXT_FIELD_CARET_WIDTH_PX,
                            glyphPx,
                        ),
                        fillColor = resolvedWithInteraction.foreground ?: theme.colors.foreground,
                        radiusPx = 0f,
                        borderWidth = UiShape.none,
                        borderColor = Color.Transparent,
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
            lineCount = currentLayout.lines.size,
        )
        nextValue
    }
}

private fun UiPrimitiveScope.caretBlinkElapsedSeconds(id: String): Float {
    val state = widgetState(id)
    val elapsed = state.get("caretElapsed", 0f) + frameDeltaSeconds()
    state.set("caretElapsed", elapsed)
    return elapsed
}

private fun cursorToLineAndCol(
    layout: UiBitmapTextLayout,
    value: String,
    cursor: Int,
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
    contentSlot: UiBounds,
    cursor: Int,
): Pair<Float, Float> {
    val (lineIdx, colIdx) = cursorToLineAndCol(layout, value, cursor)
    val line = layout.lines.getOrNull(lineIdx) ?: ""
    var x = contentSlot.x
    for (i in 0 until colIdx.coerceAtMost(line.length)) {
        x += font.advanceFor(line[i], glyphPx)
    }
    val lineStep = glyphPx * font.lineHeightEm + lineGap
    val y = contentSlot.y + lineIdx * lineStep
    return x to y
}

private fun indexForPointerXY(
    layout: UiBitmapTextLayout,
    value: String,
    font: UiFont,
    glyphPx: Float,
    lineGap: Float,
    contentSlot: UiBounds,
    pointerX: Float,
    pointerY: Float,
): Int {
    val lineStep = glyphPx * font.lineHeightEm + lineGap
    val lineIdx = ((pointerY - contentSlot.y) / lineStep).toInt()
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
    direction: Int,
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
