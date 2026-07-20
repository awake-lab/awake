// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

/**
 * Pressable two-state button (e.g. bold/italic toolbar buttons).
 * Different from [switchWidget] which is a boolean pill-shaped switch.
 */
fun UiScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    width: Dimension = Dimension.FillMax,
    height: Dimension = Dimension.Fixed(40f.dp),
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {}
): Boolean {
    val interaction = interact(
        id = id,
        width = width,
        height = height,
        modifier = modifier
    )

    if (interaction.clicked && enabled) {
        onCheckedChange(!checked)
    }

    val styleState = MutableStyleState(
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
        selected = checked,
        disabled = !enabled
    )
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.button then Style {
            if (checked) {
                background(theme.tokens.secondary)
                foreground(theme.tokens.secondaryForeground)
            } else {
                background(Color.Transparent)
                foreground(theme.tokens.mutedForeground)
            }
        },
        state = styleState
    )

    val contentSlot = interaction.slot.inset(resolved.contentPadding)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )

    if (label != null && font != null) {
        text(
            label = label,
            slot = contentSlot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            verticallyCentered = true,
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
        contentBounds = contentSlot,
        selected = checked
    )

    return checked
}

fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    onIndexChange: (Int) -> Unit = {}
) {
    row(modifier = modifier, gap = 0f) {
        options.forEachIndexed { index, option ->
            toggle(
                id = "$id.$index",
                checked = selectedIndex == index,
                label = option,
                onCheckedChange = { if (it) onIndexChange(index) }
            )
        }
    }
}
