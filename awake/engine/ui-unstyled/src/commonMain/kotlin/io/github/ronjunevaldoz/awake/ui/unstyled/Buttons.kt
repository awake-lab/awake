// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.ResolvedStyle
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** [button] with the resolved [UiSlot] alongside the click result. */
data class UiButtonResult(val clicked: Boolean, val slot: UiSlot)

private inline fun UiScope.buttonSlotInternal(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    semanticLabel: String? = null,
    drawContent: AbsoluteScope.(contentSlot: UiSlot, resolved: ResolvedStyle) -> Unit
): UiButtonResult {
    val theme = context.currentTheme
    val defaults = theme.components.button then Style.Companion {
        shape(radius)
        if (variant == UiButtonVariant.Outline) {
            borderWidth(1f.dp)
        }
    }
    val surface = resolveInteractiveSurface(
        id = id,
        width = width,
        height = height,
        modifier = modifier,
        style = style,
        defaults = defaults,
        selected = false
    )
    val baseFill = surface.resolved.background ?: theme.tokens.background
    val fillColor = variant.resolveFill(baseFill, surface.interaction.hovered, surface.interaction.active)
    paintSurface(
        slot = surface.interaction.slot,
        resolved = surface.resolved,
        fillColor = fillColor,
    )
    childAbsolute(slot = surface.contentSlot).drawContent(surface.contentSlot, surface.resolved)
    recordSemantic(
        role = UiSemanticRole.Button,
        id = id,
        label = semanticLabel,
        bounds = surface.interaction.slot,
        contentBounds = surface.contentSlot
    )
    return UiButtonResult(surface.interaction.clicked, surface.interaction.slot)
}

fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): Boolean = buttonSlot(
    id = id,
    label = label,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    centered = centered,
    verticallyCentered = verticallyCentered
).clicked

fun UiScope.buttonSlot(
    id: String,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered
): UiButtonResult = buttonSlotInternal(
    id = id,
    width = Dimension.FillMax,
    height = Dimension.Fixed(40f.dp),
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    semanticLabel = label,
    drawContent = { contentSlot, resolved ->
        if (label != null) {
            val theme = context.currentTheme
            text(
                label = label,
                slot = contentSlot,
                font = context.currentFont,
                color = resolved.foreground ?: theme.tokens.foreground,
                centered = centered,
                verticallyCentered = verticallyCentered,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = resolved.textStyle,
                semanticId = "$id.label"
            )
        }
    }
)

fun UiScope.buttonSlot(
    id: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): UiButtonResult = buttonSlotInternal(
    id = id,
    width = Dimension.FillMax,
    height = Dimension.Fixed(40f.dp),
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius
) { contentSlot, _ ->
    content(contentSlot)
}

fun UiScope.button(
    id: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    content: AbsoluteScope.(slot: UiSlot) -> Unit
): Boolean = buttonSlot(
    id = id,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    content = content
).clicked

/**
 * shadcn/ui's button variant vocabulary, scoped down to what a fill/border decision needs --
 * [Filled] (the only variant that existed before this) always paints its resolved [Style]
 * color; [Outline]/[Ghost] paint no fill at rest, only on hover/active (so an idle Outline/
 * Ghost button reads as "just a border" / "just a label" the way shadcn's own CSS variants
 * do), and [Outline] additionally always draws a `theme.tokens.border` stroke regardless of
 * hover state. [buttonSlot] is the single place that interprets this -- a consumer widget
 * built on the same primitives ([UiScope.claimSlot]/[UiScope.emit]/[io.github.ronjunevaldoz.awake.ui.core.graphics.border]) can
 * define its own variant vocabulary instead of this one; nothing else in the library assumes
 * these three exist.
 */
enum class UiButtonVariant {
    Filled,
    Outline,
    Ghost
}

internal fun UiButtonVariant.resolveFill(baseColor: Color, hovered: Boolean, active: Boolean): Color =
    when (this) {
        UiButtonVariant.Filled -> baseColor
        UiButtonVariant.Outline, UiButtonVariant.Ghost -> if (hovered || active) baseColor else Color.Transparent
    }
