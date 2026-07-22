package io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface
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
    val theme = context.currentTheme
    val interaction = interact(
        id = id,
        width = width,
        height = height,
        modifier = modifier
    )

    val newChecked = if (interaction.clicked && enabled) !checked else checked
    if (newChecked != checked) {
        onCheckedChange(newChecked)
    }

    val surface = resolveInteractiveSurface(
        interaction = interaction,
        modifier = modifier,
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
        selected = newChecked,
        disabled = !enabled
    )
    paintSurface(slot = interaction.slot, resolved = surface.resolved)

    if (label != null) {
        text(
            label = label,
            slot = surface.contentSlot,
            font = context.currentFont,
            color = surface.resolved.foreground ?: theme.tokens.foreground,
            centered = true,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = surface.resolved.textStyle,
            semanticId = "$id.label"
        )
    }

    recordSemantic(
        role = UiSemanticRole.Toggle,
        id = id,
        label = label,
        bounds = interaction.slot,
        contentBounds = surface.contentSlot,
        selected = newChecked
    )

    return newChecked
}
