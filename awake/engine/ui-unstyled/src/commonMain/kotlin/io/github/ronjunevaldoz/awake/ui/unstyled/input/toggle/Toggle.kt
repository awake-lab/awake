package io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.inset
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.interact

/**
 * Pressable two-state button (e.g. bold/italic toolbar buttons).
 * Different from [io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch] which is a boolean pill-shaped switch.
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
        defaults = theme.components.button then Style.Companion {
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

    if (label != null) {
        text(
            label = label,
            slot = contentSlot,
            font = font,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = resolved.textStyle,
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
