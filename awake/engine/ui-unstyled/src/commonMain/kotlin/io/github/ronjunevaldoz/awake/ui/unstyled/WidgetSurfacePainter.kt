// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.inset
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.toPx

internal data class UiResolvedSurface(
    val slot: UiSlot,
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal data class UiInteractiveSurface(
    val interaction: UiInteraction,
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal data class UiSurfaceStyle(
    val resolved: ResolvedStyle,
    val contentSlot: UiSlot
)

internal fun UiScope.resolveSurface(
    defaultWidth: Dimension,
    defaultHeight: Dimension,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    state: MutableStyleState = MutableStyleState()
): UiResolvedSurface {
    val slot = claimModifiedSlot(
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        modifier = modifier
    )
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = state
    )
    return UiResolvedSurface(
        slot = slot,
        resolved = resolved,
        contentSlot = slot.inset(resolved.contentPadding)
    )
}

internal fun UiScope.resolveInteractiveSurface(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false
): UiInteractiveSurface {
    val interaction = interact(id, width, height, modifier)
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
    return UiInteractiveSurface(
        interaction = interaction,
        resolved = surfaceStyle.resolved,
        contentSlot = surfaceStyle.contentSlot
    )
}

internal fun UiScope.resolveInteractiveSurface(
    interaction: UiInteraction,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false
): UiInteractiveSurface {
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
    return UiInteractiveSurface(
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
): UiSurfaceStyle {
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
    return UiSurfaceStyle(
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
