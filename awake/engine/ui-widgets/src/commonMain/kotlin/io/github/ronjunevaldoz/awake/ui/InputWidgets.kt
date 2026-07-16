// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input

fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float {
    val slot = claimModifiedSlot(width.toDimension(), height.toDimension(), modifier)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val dragging = isActive(id) && Input.pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(Input.pointerX, slot.x, slot.width, min, max) else value
    releaseActiveIfMatches(id)

    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.slider,
        state = MutableStyleState(hovered = hovered, active = dragging)
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (slot.width * fraction).coerceAtLeast(0f)
    if (handleWidth > 0f) {
        emitFillAndBorder(
            slot = UiSlot(slot.x, slot.y, handleWidth, slot.height),
            fillColor = theme.tokens.accent,
            radiusPx = resolved.shape.toPx(),
            borderWidth = UiShape.none,
            borderColor = TransparentColor,
            shapeSpec = resolved.shapeSpec
        )
    }
    if (label != null && font != null) {
        text(
            label,
            slot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            overflow = UiTextOverflow.Ellipsis
        )
    }
    return newValue
}

fun UiScope.dropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? {
    val expandedState = rememberBooleanState(id, key = "expanded")
    val resolvedDefaults = theme.components.dropdown
    val selectedLabel = options.getOrNull(selectedIndex)
    val (clicked, slot) = buttonSlot(id, width, height, selectedLabel, modifier, style = resolvedDefaults then style)
    if (clicked) {
        expandedState.update { !it }
    }
    var picked: Int? = null
    val popupResult = popup(
        anchorSlot = slot,
        expanded = expandedState.value,
        width = Dimension.Fixed(slot.width.px),
        height = Dimension.WrapContent,
        gap = 0f,
        positionProvider = UiPopupDefaults.dropdown()
    ) {
        options.forEachIndexed { index, option ->
            val optionStyle = if (index == selectedIndex) {
                Style {
                    background(theme.tokens.accent)
                    foreground(theme.tokens.accentForeground)
                }
            } else {
                Style.Empty
            }
            if (
                button(
                    id = "$id.option$index",
                    width = slot.width,
                    height = slot.height,
                    label = option,
                    style = resolvedDefaults then style then optionStyle
                )
            ) {
                picked = index
            }
        }
    }
    if (popupResult.dismissed) {
        expandedState.value = false
    }
    if (picked != null) {
        expandedState.value = false
    }
    return picked
}
