// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.input.text

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiTextEditAction
import io.github.ronjunevaldoz.awake.ui.clearFocusIfMatches
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.inputState
import io.github.ronjunevaldoz.awake.ui.isFocused
import io.github.ronjunevaldoz.awake.ui.pointerDownEdge
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.requestFocus
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.headless.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.headless.interact
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


private const val TEXT_FIELD_CARET_BLINK_PERIOD_SECONDS = 1f
// Dp, not raw px: used as the caret rect's width against `textContentSlot`, which is already
// density-scaled, so a raw literal would render a half-size caret at 2x.
private val TEXT_FIELD_CARET_WIDTH = 1.5f.dp

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
    isError: Boolean = false,
    leadingIcon: (BoxScope.() -> Unit)? = null,
    trailingIcon: (BoxScope.() -> Unit)? = null,
    // Applied only to what's drawn -- `value`/the returned string always carry the real typed
    // text, same as real shadcn's Input; a password field passes `{ "*".repeat(it.length) }`
    // here and stores/returns the actual characters untouched.
    visualTransformation: (String) -> String = { it }
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
        if (isError) theme.colors.destructive else (surface.resolved.borderColor ?: theme.colors.border)
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

    // Icon slots are fixed-width squares (matching content height) carved out of either end
    // of contentSlot; the text area shrinks and shifts to make room so a leading icon never
    // overlaps typed text (a search box's magnifier sitting on top of the first character is
    // the bug this guards against).
    val iconSlotWidth = if (leadingIcon != null || trailingIcon != null) contentSlot.height else 0f
    // Dp, not raw px: `contentSlot` is already density-scaled, so a raw 6f would leave a
    // half-size gap between icon and text at 2x.
    val iconGap = 6f.dp.toPx()
    val textAreaX = contentSlot.x + if (leadingIcon != null) iconSlotWidth + iconGap else 0f
    val textAreaWidth = (contentSlot.width
        - (if (leadingIcon != null) iconSlotWidth + iconGap else 0f)
        - (if (trailingIcon != null) iconSlotWidth + iconGap else 0f)).coerceAtLeast(0f)
    val textContentSlot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
        textAreaX, contentSlot.y, textAreaWidth, contentSlot.height
    )
    if (leadingIcon != null) {
        val iconBounds = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            contentSlot.x, contentSlot.y, iconSlotWidth, contentSlot.height
        )
        childBox(iconBounds, contentAlignment = UiAlignment.Center).leadingIcon()
    }
    if (trailingIcon != null) {
        val iconBounds = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
            contentSlot.x + contentSlot.width - iconSlotWidth, contentSlot.y, iconSlotWidth, contentSlot.height
        )
        childBox(iconBounds, contentAlignment = UiAlignment.Center).trailingIcon()
    }

    val cursorState = widgetState(id)
    var cursor = cursorState.get("cursor", value.length).coerceIn(0, value.length)

    // Read last frame's applied horizontal scroll before this frame's click hit-testing -- a
    // click lands on whatever was actually rendered (last frame's scrolled position).
    val lastScrollOffsetX = cursorState.get("scrollOffsetX", 0f)

    var nextValue = value
    if (focused) {
        val clickIndex =
            if (interaction.clicked || (pointerDownEdge() && interaction.hovered)) {
                indexForPointerX(
                    // Click-to-cursor mapping walks pixel advances against what's actually on
                    // screen, i.e. the transformed text (a password field's dots), not the raw
                    // stored value -- indices still line up 1:1 since a transformation that
                    // changes the string length would desync display from `value` anyway.
                    visualTransformation(value),
                    resolvedFont,
                    glyphPx,
                    textContentSlot.x - lastScrollOffsetX,
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

    // Horizontal scroll: content wider than the field's fixed viewport used to just clip
    // silently with no way to reach the hidden characters. Auto-follow the caret, same
    // "cursor scrolls into view" behavior a native single-line text input gives you.
    val displayedForMeasure = visualTransformation(nextValue)
    val totalTextWidth = cursorAdvancePx(displayedForMeasure, resolvedFont, glyphPx, displayedForMeasure.length)
    val maxScrollX = (totalTextWidth - textContentSlot.width).coerceAtLeast(0f)
    var scrollOffsetX = lastScrollOffsetX.coerceIn(0f, maxScrollX)
    if (focused) {
        val caretAdvance = cursorAdvancePx(displayedForMeasure, resolvedFont, glyphPx, cursor)
        if (caretAdvance - scrollOffsetX < 0f) {
            scrollOffsetX = caretAdvance
        } else if (caretAdvance - scrollOffsetX > textContentSlot.width) {
            scrollOffsetX = caretAdvance - textContentSlot.width
        }
        scrollOffsetX = scrollOffsetX.coerceIn(0f, maxScrollX)
    }
    cursorState.set("scrollOffsetX", scrollOffsetX)
    val drawTextX = textContentSlot.x - scrollOffsetX

    clip(textContentSlot) {
        val showingPlaceholder = nextValue.isEmpty() && !focused
        // visualTransformation only ever touches what's drawn here -- `nextValue`, the return
        // value, and everything stored in WidgetState above stay the real typed text.
        val displayed = if (showingPlaceholder) placeholder else displayedForMeasure
        if (displayed.isNotEmpty()) {
            // overflow=Clip truncates `displayed` to whatever prefix fits the slot's own
            // width -- pass a slot at least as wide as the full string so scrolled-into-view
            // characters past the field's visible width never get truncated away before the
            // shifted drawTextX has a chance to bring them on-screen; the surrounding clip()
            // still hides everything outside textContentSlot regardless of this slot's width.
            val measureWidth = if (showingPlaceholder) textContentSlot.width else totalTextWidth.coerceAtLeast(textContentSlot.width)
            text(
                label = displayed,
                slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(drawTextX, textContentSlot.y, measureWidth, textContentSlot.height),
                font = resolvedFont,
                color = if (showingPlaceholder) theme.colors.mutedForeground else (surface.resolved.foreground
                    ?: theme.colors.foreground),
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
                    drawTextX + cursorAdvancePx(visualTransformation(nextValue), resolvedFont, glyphPx, cursor)
                emitFillAndBorder(
                    slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
                        caretX,
                        textContentSlot.y,
                        TEXT_FIELD_CARET_WIDTH.toPx(),
                        textContentSlot.height
                    ),
                    fillColor = surface.resolved.foreground ?: theme.colors.foreground,
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
