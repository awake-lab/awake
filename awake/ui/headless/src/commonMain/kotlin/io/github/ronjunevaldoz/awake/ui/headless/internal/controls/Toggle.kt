// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.compositeContent
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.resolveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Result of a [toggle] press: the toggle's new checked state, alongside its slot. */
private inline fun UiPrimitiveScope.toggleInternal(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    onCheckedChange: (Boolean) -> Unit = {},
    semanticLabel: String? = null,
    // crossinline because the content is now dispatched inside compositeContent's lambda, which a
    // non-local return out of would skip -- leaving the suppression depth counter raised for the
    // rest of the frame.
    crossinline drawContent: AbsoluteScope.(contentSlot: UiBounds, resolved: ResolvedStyle) -> Unit,
): Boolean {
    val theme = theme
    // Reads no ambient theme -- shadcnToggle's shadcnToggleStyle already supplies a complete
    // Style (including textSize, added there so this removal wouldn't silently grow the label
    // to the ambient body text size). This still hardcodes the checked/unchecked background and
    // Outline's always-on border, same as before -- those are structural to what a toggle
    // communicates, not something a caller-supplied style should be able to accidentally drop
    // (same reasoning as Switch.kt's hardcoded track color).
    val defaults = Style.Companion {
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
    }
    val sizedModifier = modifier.withSizeFallback(
        semanticLabel?.let {
            withIntrinsicLabelWidth(
                modifier = modifier,
                label = it,
                style = style,
                defaults = defaults,
            ).widthDimension
        } ?: Dimension.FillMax,
        Dimension.Fixed(40f.dp),
    )
    val interaction = interact(
        id = id,
        modifier = sizedModifier,
    )

    val newChecked = if (interaction.clicked && enabled) !checked else checked
    if (newChecked != checked) {
        onCheckedChange(newChecked)
    }

    val surface = resolveInteractiveSurface(
        interaction = interaction,
        modifier = sizedModifier,
        style = style,
        defaults = defaults,
        selected = newChecked,
        disabled = !enabled,
    )
    paintSurface(slot = interaction.slot, resolved = surface.resolved)

    // Same reason as Buttons.kt -- a toggle's content is its own subtree, not siblings of the
    // toggle in whatever column contains it. See compositeContent().
    compositeContent {
        childAbsolute(slot = surface.contentSlot).drawContent(surface.contentSlot, surface.resolved)
    }

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
 * Different from [io.github.ronjunevaldoz.awake.ui.headless.internal.controls.switch] which is a boolean pill-shaped switch.
 */
fun UiPrimitiveScope.toggle(
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
            font = font,
            color = resolved.foreground ?: theme.colors.foreground,
            centered = true,
            verticallyCentered = true,
            overflow = UiTextOverflow.Ellipsis,
            textStyle = resolved.textStyle,
            semanticId = "$id.label",
        )
    }
}
