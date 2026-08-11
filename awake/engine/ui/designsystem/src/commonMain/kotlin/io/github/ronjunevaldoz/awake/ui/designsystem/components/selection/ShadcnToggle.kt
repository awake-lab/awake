// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnToggleVariant
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggleSlot

private fun ShadcnToggleVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    ShadcnToggleVariant.Default -> UiButtonVariant.Filled
    ShadcnToggleVariant.Outline -> UiButtonVariant.Outline
}

/** Real shadcn's `Toggle`: a single pressable on/off button (distinct from [shadcnSwitch]'s
 * track-and-thumb look). Slot-API primary form -- the [content] lambda receives a [BoxScope],
 * allowing arbitrary layouts (icon-only, icon+text) inside the toggle. Delegates entirely to
 * [toggleSlot]. */
fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: ShadcnToggleVariant = ShadcnToggleVariant.Default,
    onCheckedChange: (Boolean) -> Unit = {},
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean = toggleSlot(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style,
    enabled = enabled,
    variant = variant.toUiButtonVariant(),
    onCheckedChange = onCheckedChange,
) { contentSlot ->
    childBox(contentSlot, contentAlignment = UiAlignment.Center).content(contentSlot)
}

/** Convenience wrapper over the [content]-slot [shadcnToggle] above for the common plain-text
 * label case. Delegates entirely to [toggle]. `label` is deliberately the last parameter here
 * (not [onCheckedChange]) so a trailing-lambda call always resolves to the content-slot overload
 * above instead of an ambiguous match against both overloads' same-arity callback/content
 * function types. */
fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    variant: ShadcnToggleVariant = ShadcnToggleVariant.Default,
    onCheckedChange: (Boolean) -> Unit = {},
    label: String? = null,
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = style,
    enabled = enabled,
    variant = variant.toUiButtonVariant(),
    onCheckedChange = onCheckedChange,
)
