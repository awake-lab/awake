// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.selection

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitInsetAccent
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.interact

private const val CHECKBOX_LABEL_GAP = 8f

// A real iOS-style switch, not a stretched checkbox -- fixed compact size (a switch has one
// natural size, unlike a button/row that should fill available width), pill-shaped track, and
// a sliding circular knob instead of checkbox's centered inset-square "check" mark.


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
    val boxSlot = UiSlot(
        interaction.slot.x,
        interaction.slot.y + (interaction.slot.height - boxPx) / 2f,
        boxPx,
        boxPx
    )
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
    if (label != null) {
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
            textStyle = resolved.textStyle,
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
