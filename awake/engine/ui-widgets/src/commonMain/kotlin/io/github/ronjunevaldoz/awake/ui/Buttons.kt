// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/** [button] with the resolved [UiSlot] alongside the click result. */
data class UiButtonResult(val clicked: Boolean, val slot: UiSlot)

fun UiScope.buttonSlot(
    id: String,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none
): UiButtonResult {
    val interaction = interact(id, width.toDimension(), height.toDimension(), modifier)
    val defaults = theme.components.button then Style {
        shape(radius)
        if (variant == UiButtonVariant.Outline) {
            borderWidth(1f.dp)
        }
    }
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active)
    )
    val baseFill = resolved.background ?: theme.tokens.background
    val fillColor = variant.resolveFill(baseFill, interaction.hovered, interaction.active)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = fillColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    if (label != null && font != null) {
        text(
            label,
            interaction.slot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            overflow = UiTextOverflow.Ellipsis
        )
    }
    return UiButtonResult(interaction.clicked, interaction.slot)
}

fun UiScope.button(
    id: String,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none
): Boolean = buttonSlot(id, width, height, label, modifier, style, variant, radius).clicked
