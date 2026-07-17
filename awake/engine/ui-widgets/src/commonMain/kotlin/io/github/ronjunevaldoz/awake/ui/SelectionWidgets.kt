// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

private const val CHECKBOX_LABEL_GAP = 8f

fun UiScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean {
    val interaction = interact(
        id = id,
        width = Dimension.FillMax,
        height = Dimension.Fixed(32f.px),
        modifier = modifier
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.toggle,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active, selected = checked)
    )
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val newChecked = if (interaction.clicked) !checked else checked
    if (newChecked) {
        val inset = minOf(interaction.slot.width, interaction.slot.height) * 0.2f
        emitInsetAccent(interaction.slot, inset, resolved.shape.toPx(), resolved.shapeSpec)
    }
    if (label != null && font != null) {
        text(
            label,
            slot = interaction.slot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            overflow = UiTextOverflow.Ellipsis,
            textScale = resolved.textScale,
            textSize = resolved.textSize,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Toggle,
        id = id,
        label = label,
        bounds = interaction.slot,
        truncated = false,
        selected = newChecked
    )
    return newChecked
}

fun UiScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    boxSize: Dp = 16f.dp
): Boolean {
    val interaction = interact(
        id = id,
        width = Dimension.FillMax,
        height = Dimension.Fixed(24f.px),
        modifier = modifier
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.checkbox,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active, selected = checked)
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiSlot(interaction.slot.x, interaction.slot.y + (interaction.slot.height - boxPx) / 2f, boxPx, boxPx)
    emitFillAndBorder(
        slot = boxSlot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    val newChecked = if (interaction.clicked) !checked else checked
    if (newChecked) {
        val inset = boxPx * 0.25f
        emitInsetAccent(boxSlot, inset, resolved.shape.toPx(), resolved.shapeSpec)
    }
    val resolvedFont = font
    if (label != null && resolvedFont != null) {
        val labelSlot = UiSlot(
            boxSlot.x + boxPx + CHECKBOX_LABEL_GAP,
            interaction.slot.y,
            interaction.slot.width - boxPx - CHECKBOX_LABEL_GAP,
            interaction.slot.height
        )
        text(
            label,
            slot = labelSlot,
            font = resolvedFont,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = false,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textScale = resolved.textScale,
            textSize = resolved.textSize,
            semanticId = "$id.label"
        )
    }
    recordSemantic(
        role = UiSemanticRole.Checkbox,
        id = id,
        label = label,
        bounds = interaction.slot,
        contentBounds = boxSlot,
        selected = newChecked
    )
    return newChecked
}
