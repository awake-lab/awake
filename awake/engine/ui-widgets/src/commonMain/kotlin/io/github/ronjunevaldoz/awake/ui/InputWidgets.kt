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
    val slot = claimSlot(modifier.width ?: width.toDimension(), modifier.height ?: height.toDimension())
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
        text(label, slot, font = font, color = resolved.foreground ?: theme.tokens.foreground, centered = true)
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
    val state = widgetState(id)
    val resolvedDefaults = theme.components.dropdown
    val selectedLabel = options.getOrNull(selectedIndex)
    val (clicked, slot) = buttonSlot(id, width, height, selectedLabel, modifier, style = resolvedDefaults then style)
    if (clicked) {
        state.set("expanded", !state.get("expanded", false))
    }
    var picked: Int? = null
    if (state.get("expanded", false)) {
        var anyOptionHovered = false
        options.forEachIndexed { index, option ->
            val optionSlot = UiSlot(slot.x, slot.y + slot.height * (index + 1), slot.width, slot.height)
            val optionHovered = hitTest(optionSlot)
            if (optionHovered) anyOptionHovered = true
            val optionId = "$id.option$index"
            tryClaimActive(optionId, optionHovered)
            val wasActiveBeforeRelease = isActive(optionId)
            releaseActiveIfMatches(optionId)
            val optionActive = isActive(optionId)
            val optionClicked = wasActiveBeforeRelease && !optionActive && optionHovered
            if (optionClicked) picked = index
            val resolved = resolveStyle(
                style = style,
                defaults = resolvedDefaults,
                state = MutableStyleState(
                    hovered = optionHovered,
                    active = optionActive,
                    selected = index == selectedIndex
                )
            )
            val fillColor = resolved.background ?: theme.tokens.background
            emitFillAndBorder(
                slot = optionSlot,
                fillColor = fillColor,
                radiusPx = resolved.shape.toPx(),
                borderWidth = UiShape.none,
                borderColor = TransparentColor,
                shapeSpec = resolved.shapeSpec,
                overlay = true
            )
            val resolvedFont = font
            if (resolvedFont != null) {
                val glyphPx = resolvedFont.cellSize * resolvedTextScale()
                val textWidth = option.length * glyphPx
                var penX = optionSlot.x + (optionSlot.width - textWidth) / 2f
                val penY = optionSlot.y + (optionSlot.height - glyphPx) / 2f
                for (char in option) {
                    val uv = resolvedFont.uvFor(char)
                    if (uv != null) {
                        emitOverlay(
                            UiDrawPrimitive.Glyph(
                                penX,
                                penY,
                                glyphPx,
                                glyphPx,
                                uv.u0,
                                uv.v0,
                                uv.u1,
                                uv.v1,
                                resolved.foreground ?: theme.tokens.foreground
                            )
                        )
                    }
                    penX += glyphPx
                }
            }
        }
        val headerHovered = hitTest(slot)
        if (!clicked && Input.pointerDown && !headerHovered && !anyOptionHovered) {
            state.set("expanded", false)
        }
    }
    return picked
}
