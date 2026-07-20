// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

private const val CHECKBOX_LABEL_GAP = 8f

// A real iOS-style switch, not a stretched checkbox -- fixed compact size (a switch has one
// natural size, unlike a button/row that should fill available width), pill-shaped track, and
// a sliding circular knob instead of checkbox's centered inset-square "check" mark.
private const val TOGGLE_WIDTH_PX = 40f
private const val TOGGLE_HEIGHT_PX = 22f
private const val TOGGLE_KNOB_INSET_PX = 2f
private const val TOGGLE_LABEL_GAP = 8f

fun UiScope.switchWidget(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean {
    val interaction = interact(
        id = id,
        width = Dimension.Fixed(TOGGLE_WIDTH_PX.dp),
        height = Dimension.Fixed(TOGGLE_HEIGHT_PX.dp),
        modifier = modifier
    )
    val styleState = MutableStyleState(
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
        selected = checked
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.toggle,
        state = styleState
    )
    val newChecked = if (interaction.clicked) !checked else checked
    val trackFill = if (newChecked) theme.tokens.primary else (resolved.background ?: theme.tokens.background)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = trackFill,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = UiShapeSpec.Pill
    )
    val knobDiameter = interaction.slot.height - TOGGLE_KNOB_INSET_PX * 2f
    val knobX = if (newChecked) {
        interaction.slot.x + interaction.slot.width - TOGGLE_KNOB_INSET_PX - knobDiameter
    } else {
        interaction.slot.x + TOGGLE_KNOB_INSET_PX
    }
    emitFillAndBorder(
        slot = UiSlot(knobX, interaction.slot.y + TOGGLE_KNOB_INSET_PX, knobDiameter, knobDiameter),
        fillColor = theme.tokens.background,
        radiusPx = 0f,
        borderWidth = UiShape.none,
        borderColor = TransparentColor,
        shapeSpec = UiShapeSpec.Pill
    )
    if (label != null && font != null) {
        val labelWidth = (fillWidthOrNull()?.let { it - interaction.slot.width - TOGGLE_LABEL_GAP } ?: 160f).coerceAtLeast(0f)
        text(
            label,
            slot = UiSlot(
                interaction.slot.x + interaction.slot.width + TOGGLE_LABEL_GAP,
                interaction.slot.y,
                labelWidth,
                interaction.slot.height
            ),
            font = font,
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
        role = UiSemanticRole.Switch,
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
        height = Dimension.Fixed(24f.dp),
        modifier = modifier
    )
    val boxPx = boxSize.toPx()
    val boxSlot = UiSlot(interaction.slot.x, interaction.slot.y + (interaction.slot.height - boxPx) / 2f, boxPx, boxPx)
    val styleState = MutableStyleState(
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
        selected = checked
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.checkbox,
        state = styleState
    )
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
