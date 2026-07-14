// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

fun UiScope.propertyRow(label: String, height: Float, labelWidth: Dp = 64f.dp): UiSlot {
    val rowSlot = claimSlot(Dimension.FillMax, height.toDimension())
    val labelWidthPx = labelWidth.toPx()
    val labelStyle = resolveStyle(defaults = theme.components.inspectorLabel)
    val resolvedFont = font
    if (resolvedFont != null) {
        val glyphPx = resolvedFont.cellSize * resolvedTextScale()
        val labelSlot = UiSlot(rowSlot.x, rowSlot.y + (rowSlot.height - glyphPx) / 2f, labelWidthPx, glyphPx)
        text(label, labelSlot, font = resolvedFont, color = labelStyle.foreground ?: theme.tokens.mutedForeground, centered = false)
    }
    val gap = 8f
    return UiSlot(rowSlot.x + labelWidthPx + gap, rowSlot.y, (rowSlot.width - labelWidthPx - gap).coerceAtLeast(0f), rowSlot.height)
}

fun UiScope.propertyCheckbox(
    id: String,
    checked: Boolean,
    label: String,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean {
    val interaction = interact(id, Dimension.FillMax, modifier.height ?: height.toDimension())
    val labelStyle = resolveStyle(defaults = theme.components.inspectorLabel)
    val resolvedFont = font
    if (resolvedFont != null) {
        val glyphPx = resolvedFont.cellSize * resolvedTextScale()
        val labelSlot = UiSlot(interaction.slot.x, interaction.slot.y + (interaction.slot.height - glyphPx) / 2f, interaction.slot.width, glyphPx)
        text(label, labelSlot, font = resolvedFont, color = labelStyle.foreground ?: theme.tokens.mutedForeground, centered = false)
    }

    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.checkbox,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active, selected = checked)
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiSlot(interaction.slot.x + interaction.slot.width - boxPx, interaction.slot.y + (interaction.slot.height - boxPx) / 2f, boxPx, boxPx)
    emitFillAndBorder(
        slot = boxSlot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border
    )
    val newChecked = if (interaction.clicked) !checked else checked
    if (newChecked) {
        emitInsetAccent(boxSlot, boxPx * 0.25f, resolved.shape.toPx())
    }
    return newChecked
}
