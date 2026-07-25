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
import io.github.ronjunevaldoz.awake.ui.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.inputState
import io.github.ronjunevaldoz.awake.ui.isFocused
import io.github.ronjunevaldoz.awake.ui.pointerDownEdge
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.requestFocus
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.interact
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


private const val TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS = 1f
private const val TEXT_FIELD_CARET_WIDTH_PX = 1.5f

/**
 * Single-line text input -- immediate-mode like every other widget here: caller passes the
 * current [value] and gets back whatever it should be next frame. Cursor position and caret
 * blink phase live in [io.github.ronjunevaldoz.awake.ui.WidgetState], keyed on [id], the same way a dropdown's expanded flag
 * does. No selection, no multi-line, no clipboard yet -- the smallest version that lets a
 * user actually type and edit a value, not a mockup of one; those are real gaps to fill in
 * later, not corners silently cut and hoped nobody notices.
 */
fun UiScope.textField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false
): String {
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(40f.dp))
    )
    // Disabled fields never claim focus or consume input -- if a field was focused and then
    // became disabled mid-session, drop that focus too, the same way a real disabled input
    // stops receiving keystrokes immediately, not just stops accepting new clicks.
    val focused = enabled && (isFocused(id) || modifier.forceFocus == true)
    if (!enabled) {
        clearFocusIfMatches(id)
    } else if (pointerDownEdge() && (interaction.hovered || modifier.forceHover == true)) {
        requestFocus(id)
    }

    val surface = resolveInteractiveSurface(
        interaction = interaction,
        modifier = modifier,
        style = style,
        defaults = theme.components.textField,
        disabled = !enabled,
        focused = focused
    )
    val borderColor =
        if (isError) theme.tokens.destructive else (surface.resolved.borderColor ?: theme.tokens.border)
    paintSurface(
        slot = surface.interaction.slot,
        resolved = surface.resolved.copy(
            borderWidth = if (focused || isError) 1.5f.dp else surface.resolved.borderWidth
        ),
        borderColor = borderColor
    )

    val resolvedFont = font
    val glyphPx =
        resolveGlyphPx(resolvedFont, surface.resolved.textStyle)
    val contentSlot = surface.contentSlot

    val cursorState = widgetState(id)
    var cursor = cursorState.get("cursor", value.length).coerceIn(0, value.length)

    var nextValue = value
    if (focused) {
        val clickIndex =
            if (interaction.clicked || (pointerDownEdge() && interaction.hovered)) {
                indexForPointerX(
                    value,
                    resolvedFont,
                    glyphPx,
                    contentSlot.x,
                    inputState.pointerX
                )
            } else {
                null
            }
        if (clickIndex != null) {
            cursor = clickIndex
        }

        // Edit actions (cursor moves, deletes) before the newly typed text
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
                UiTextEditAction.ArrowUp -> {}
                UiTextEditAction.ArrowDown -> {}
                UiTextEditAction.Home -> cursor = 0
                UiTextEditAction.End -> cursor = nextValue.length
                UiTextEditAction.Enter -> clearFocusIfMatches(id)
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
                slot = UiSlot(contentSlot.x, contentSlot.y, contentSlot.width, contentSlot.height),
                font = resolvedFont,
                color = if (showingPlaceholder) theme.tokens.mutedForeground else (surface.resolved.foreground
                    ?: theme.tokens.foreground),
                verticallyCentered = true,
                overflow = UiTextOverflow.Clip,
                textStyle = surface.resolved.textStyle,
                semanticId = "$id.value"
            )
        }
        if (focused) {
            val elapsed = caretBlinkElapsedSeconds(id)
            val caretVisible =
                (elapsed % TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS) < TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS / 2f
            if (caretVisible) {
                val caretX =
                    contentSlot.x + cursorAdvancePx(nextValue, resolvedFont, glyphPx, cursor)
                emitFillAndBorder(
                    slot = UiSlot(
                        caretX,
                        contentSlot.y,
                        TEXT_FIELD_CARET_WIDTH_PX,
                        contentSlot.height
                    ),
                    fillColor = surface.resolved.foreground ?: theme.tokens.foreground,
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
        label = if (nextValue.isEmpty()) placeholder else nextValue,
        bounds = surface.interaction.slot,
        contentBounds = contentSlot,
        selected = focused
    )
    return nextValue
}

private fun UiScope.caretBlinkElapsedSeconds(id: String): Float {
    val state = widgetState(id)
    val elapsed = state.get("caretElapsed", 0f) + frameDeltaSeconds()
    state.set("caretElapsed", elapsed)
    return elapsed
}

private fun cursorAdvancePx(value: String, font: UiFont, glyphPx: Float, cursor: Int): Float {
    var advance = 0f
    for (index in 0 until cursor) {
        advance += font.advanceFor(value[index], glyphPx)
    }
    return advance
}

private fun indexForPointerX(
    value: String,
    font: UiFont,
    glyphPx: Float,
    contentX: Float,
    pointerX: Float
): Int {
    var advance = 0f
    value.forEachIndexed { index, char ->
        val charWidth = font.advanceFor(char, glyphPx)
        if (pointerX < contentX + advance + charWidth / 2f) {
            return index
        }
        advance += charWidth
    }
    return value.length
}
