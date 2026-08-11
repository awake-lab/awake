// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.interact
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveInteractiveSurface

/** Result of a [toggle]/[toggleSlot] press: the toggle's new checked state, alongside its slot. */
private inline fun UiScope.toggleInternal(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    onCheckedChange: (Boolean) -> Unit = {},
    semanticLabel: String? = null,
    drawContent: AbsoluteScope.(contentSlot: UiBounds, resolved: ResolvedStyle) -> Unit,
): Boolean {
    val theme = context.currentTheme
    val interaction = interact(
        id = id,
        modifier = modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(40f.dp)),
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
            // Mirrors buttonSlotInternal's Outline treatment: always draw a border, regardless
            // of checked state, so an idle Outline toggle still reads as a bordered control.
            if (variant == UiButtonVariant.Outline) {
                borderWidth(1f.dp)
            }
            if (checked) {
                background(theme.colors.secondary)
                foreground(theme.colors.secondaryForeground)
            } else {
                background(Color.Transparent)
                foreground(theme.colors.mutedForeground)
            }
        },
        selected = newChecked,
        disabled = !enabled,
    )
    paintSurface(slot = interaction.slot, resolved = surface.resolved)

    childAbsolute(slot = surface.contentSlot).drawContent(surface.contentSlot, surface.resolved)

    recordSemantic(
        role = UiSemanticRole.Toggle,
        id = id,
        label = semanticLabel,
        bounds = interaction.slot,
        contentBounds = surface.contentSlot,
        selected = newChecked,
    )

    return newChecked
}

/**
 * Pressable two-state button (e.g. bold/italic toolbar buttons).
 * Different from [io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch] which is a boolean pill-shaped switch.
 */
fun UiScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    onCheckedChange: (Boolean) -> Unit = {},
): Boolean = toggleInternal(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style,
    enabled = enabled,
    variant = variant,
    onCheckedChange = onCheckedChange,
    semanticLabel = label,
) { contentSlot, resolved ->
    if (label != null) {
        text(
            label = label,
            slot = contentSlot,
            font = context.currentFont,
            color = resolved.foreground ?: context.currentTheme.colors.foreground,
            centered = true,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = resolved.textStyle,
            semanticId = "$id.label",
        )
    }
}

/**
 * Toggle with a Compose-style Slot API, matching [io.github.ronjunevaldoz.awake.ui.headless.buttonSlot].
 * The [content] lambda receives an [AbsoluteScope], allowing arbitrary layouts (e.g. an icon-only
 * toggle) inside the toggle instead of the fixed [toggle] text label.
 */
fun UiScope.toggleSlot(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    onCheckedChange: (Boolean) -> Unit = {},
    content: AbsoluteScope.(slot: UiBounds) -> Unit,
): Boolean = toggleInternal(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style,
    enabled = enabled,
    variant = variant,
    onCheckedChange = onCheckedChange,
) { contentSlot, _ -> content(contentSlot) }
