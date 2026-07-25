// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal data class ResolvedSurface(
    val slot: UiSlot,
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal data class InteractiveSurface(
    val interaction: UiInteraction,
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal data class SurfaceStyle(
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal fun UiScope.resolveSurface(
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    state: MutableStyleState = MutableStyleState()
): ResolvedSurface {
    val slot = claimModifiedSlot(modifier)
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = state
    )
    return ResolvedSurface(
        slot = slot,
        resolved = resolved,
        contentSlot = slot.inset(resolved.contentPadding)
    )
}

internal fun UiScope.resolveInteractiveSurface(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false
): InteractiveSurface {
    val interaction = interact(id, modifier)
    val surfaceStyle = resolveSurfaceStyle(
        slot = interaction.slot,
        style = style,
        defaults = defaults,
        selected = selected,
        disabled = disabled,
        focused = focused || modifier.forceFocus == true,
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true
    )
    return InteractiveSurface(
        interaction = interaction,
        resolved = surfaceStyle.resolved,
        contentSlot = surfaceStyle.contentSlot
    )
}

internal fun UiScope.resolveInteractiveSurface(
    interaction: UiInteraction,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false
): InteractiveSurface {
    val surfaceStyle = resolveSurfaceStyle(
        slot = interaction.slot,
        style = style,
        defaults = defaults,
        selected = selected,
        disabled = disabled,
        focused = focused || modifier.forceFocus == true,
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true
    )
    return InteractiveSurface(
        interaction = interaction,
        resolved = surfaceStyle.resolved,
        contentSlot = surfaceStyle.contentSlot
    )
}

private fun UiScope.resolveSurfaceStyle(
    slot: UiSlot,
    style: Style,
    defaults: Style,
    selected: Boolean,
    disabled: Boolean,
    focused: Boolean,
    hovered: Boolean,
    active: Boolean
): SurfaceStyle {
    val state = MutableStyleState(
        hovered = hovered,
        active = active,
        focused = focused,
        disabled = disabled,
        selected = selected
    )
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = state
    )
    return SurfaceStyle(
        resolved = resolved,
        contentSlot = slot.inset(resolved.contentPadding)
    )
}

internal fun UiScope.paintSurface(
    slot: UiSlot,
    resolved: ResolvedStyle,
    fillColor: Color? = null,
    borderColor: Color? = null
) {
    val theme = context.currentTheme
    emitFillAndBorder(
        slot = slot,
        fillColor = fillColor ?: resolved.background ?: theme.tokens.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = borderColor ?: resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
}
