// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layout.inset
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.MutableStyleState
import io.github.ronjunevaldoz.awake.ui.style.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.toPx

internal data class ResolvedSurface(
    val slot: UiBounds,
    val resolved: ResolvedStyle,
    val contentSlot: UiBounds,
)

internal data class InteractiveSurface(
    val interaction: UiInteraction,
    val resolved: ResolvedStyle,
    val contentSlot: UiBounds,
)

internal data class SurfaceStyle(
    val resolved: ResolvedStyle,
    val contentSlot: UiBounds,
)

internal fun UiPrimitiveScope.resolveSurface(
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    state: MutableStyleState = MutableStyleState(),
): ResolvedSurface {
    val slot = claimModifiedSlot(modifier)
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = state,
    )
    return ResolvedSurface(
        slot = slot,
        resolved = resolved,
        contentSlot = slot.inset(resolved.contentPadding),
    )
}

internal fun UiPrimitiveScope.resolveInteractiveSurface(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false,
    enabled: Boolean = true,
): InteractiveSurface {
    val interaction = interact(id, modifier, enabled)
    val surfaceStyle = resolveSurfaceStyle(
        slot = interaction.slot,
        style = style,
        defaults = defaults,
        selected = selected,
        disabled = disabled,
        focused = focused || modifier.forceFocus == true,
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
    )
    return InteractiveSurface(
        interaction = interaction,
        resolved = surfaceStyle.resolved,
        contentSlot = surfaceStyle.contentSlot,
    )
}

internal fun UiPrimitiveScope.resolveInteractiveSurface(
    interaction: UiInteraction,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    selected: Boolean = false,
    disabled: Boolean = false,
    focused: Boolean = false,
): InteractiveSurface {
    val surfaceStyle = resolveSurfaceStyle(
        slot = interaction.slot,
        style = style,
        defaults = defaults,
        selected = selected,
        disabled = disabled,
        focused = focused || modifier.forceFocus == true,
        hovered = interaction.hovered || modifier.forceHover == true,
        active = interaction.active || modifier.forceActive == true,
    )
    return InteractiveSurface(
        interaction = interaction,
        resolved = surfaceStyle.resolved,
        contentSlot = surfaceStyle.contentSlot,
    )
}

private fun UiPrimitiveScope.resolveSurfaceStyle(
    slot: UiBounds,
    style: Style,
    defaults: Style,
    selected: Boolean,
    disabled: Boolean,
    focused: Boolean,
    hovered: Boolean,
    active: Boolean,
): SurfaceStyle {
    val state = MutableStyleState(
        hovered = hovered,
        active = active,
        focused = focused,
        disabled = disabled,
        selected = selected,
    )
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = state,
    )
    return SurfaceStyle(
        resolved = resolved,
        contentSlot = slot.inset(resolved.contentPadding),
    )
}

internal fun UiPrimitiveScope.paintSurface(
    slot: UiBounds,
    resolved: ResolvedStyle,
    fillColor: Color? = null,
    borderColor: Color? = null,
    shapeSpec: UiShapeSpec? = resolved.shapeSpec,
) {
    val theme = context.currentTheme
    emitFillAndBorder(
        slot = slot,
        fillColor = fillColor ?: resolved.background ?: theme.colors.background,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = borderColor ?: resolved.borderColor ?: theme.colors.border,
        shapeSpec = shapeSpec,
        fillTokenId = resolved.backgroundToken,
        borderTokenId = resolved.borderColorToken,
    )
}
