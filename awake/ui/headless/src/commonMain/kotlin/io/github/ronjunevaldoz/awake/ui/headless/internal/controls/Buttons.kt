// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.paintSurface
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.resolveInteractiveSurface
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
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

/** [button] with the resolved [UiBounds] alongside the click result. */
data class UiButtonResult(val clicked: Boolean, val slot: UiBounds)

private inline fun UiPrimitiveScope.buttonSlotInternal(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    semanticLabel: String? = null,
    intrinsicWidth: Dimension? = null,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    crossinline drawContent: AbsoluteScope.(contentSlot: UiBounds, resolved: ResolvedStyle) -> Unit,
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
        modifier = modifier.withSizeFallback(intrinsicWidth ?: Dimension.FillMax, Dimension.Fixed(40f.dp)),
        style = style,
        defaults = defaults,
        selected = false,
        disabled = !enabled,
        enabled = enabled,
    )
    val baseFill = surface.resolved.background ?: theme.colors.background
    val fillColor =
        variant.resolveFill(baseFill, surface.interaction.hovered, surface.interaction.active)
    // Reference's `disabled:opacity-50` treatment (shadcn-compose's `disabledDim()`), applied
    // here as a single [withGraphicsLayerAlpha] group covering the fill/border paint and the
    // content lambda's own text/graphics, the same "one flat composited result, dimmed once"
    // shape `ShadcnSlider.kt` in the reference repo settled on -- not a per-color alpha tweak,
    // which double-dims anything the content draws on top of the fill.
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        paintSurface(
            slot = surface.interaction.slot,
            resolved = surface.resolved,
            fillColor = fillColor,
        )
        // Slot-API content (an arbitrary caller-composed lambda) has no other way to know this
        // button's resolved themed foreground -- push it as the ambient text style so any `text()`
        // called inside `drawContent` picks up the right contrast automatically, the same way
        // `surface()`/`column()`/`row()`/`box()` already propagate their resolved text style to
        // their own children. Mirrors the explicit `color = resolved.foreground` passed to the
        // label overload's own internal `text()` call below.
        context.pushTextStyle(
            surface.resolved.textStyle then TextStyle(color = surface.resolved.foreground),
        )
        childAbsolute(slot = surface.contentSlot).drawContent(surface.contentSlot, surface.resolved)
        context.popTextStyle()
    }
    recordSemantic(
        role = semanticRole,
        id = id,
        label = semanticLabel,
        bounds = surface.interaction.slot,
        contentBounds = surface.contentSlot,
        backgroundColor = fillColor,
        backgroundToken = surface.resolved.backgroundToken,
        foregroundColor = surface.resolved.foreground,
        foregroundToken = surface.resolved.foregroundToken,
        borderColor = surface.resolved.borderColor,
        borderToken = surface.resolved.borderColorToken,
        borderRadius = surface.resolved.shape.toPx(),
        textStyleToken = surface.resolved.textStyleToken,
    )
    return UiButtonResult(surface.interaction.clicked, surface.interaction.slot)
}

fun UiPrimitiveScope.button(
    id: String,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = buttonSlot(
    id = id,
    label = label,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    centered = centered,
    verticallyCentered = verticallyCentered,
    enabled = enabled,
    semanticRole = semanticRole,
).clicked

fun UiPrimitiveScope.buttonSlot(
    id: String,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): UiButtonResult = buttonSlotInternal(
    id = id,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    semanticLabel = label,
    intrinsicWidth = label?.let { labelText ->
        val theme = context.currentTheme
        val defaults = theme.components.button then Style.Companion {
            shape(radius)
            if (variant == UiButtonVariant.Outline) {
                borderWidth(1f.dp)
            }
        }
        withIntrinsicLabelWidth(
            modifier = modifier,
            label = labelText,
            style = style,
            defaults = defaults,
        ).widthDimension
    },
    enabled = enabled,
    semanticRole = semanticRole,
    drawContent = { contentSlot, resolved ->
        if (label != null) {
            val theme = context.currentTheme
            text(
                label = label,
                slot = contentSlot,
                font = context.currentFont,
                color = resolved.foreground ?: theme.colors.foreground,
                centered = centered,
                verticallyCentered = verticallyCentered,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = resolved.textStyle,
                semanticId = "$id.label",
            )
        }
    },
)

fun UiPrimitiveScope.buttonSlot(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: AbsoluteScope.(slot: UiBounds) -> Unit,
): UiButtonResult = buttonSlotInternal(
    id = id,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    enabled = enabled,
    semanticRole = semanticRole,
) { contentSlot, _ ->
    content(contentSlot)
}

fun UiPrimitiveScope.button(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    variant: UiButtonVariant = UiButtonVariant.Filled,
    radius: Dp = UiShape.none,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: AbsoluteScope.(slot: UiBounds) -> Unit,
): Boolean = buttonSlot(
    id = id,
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    enabled = enabled,
    semanticRole = semanticRole,
    content = content,
).clicked

/**
 * shadcn/ui's button variant vocabulary, scoped down to what a fill/border decision needs --
 * [Filled] (the only variant that existed before this) always paints its resolved [Style]
 * color; [Outline]/[Ghost] paint no fill at rest, only on hover/active (so an idle Outline/
 * Ghost button reads as "just a border" / "just a label" the way shadcn's own CSS variants
 * do), and [Outline] additionally always draws a `theme.tokens.border` stroke regardless of
 * hover state. [buttonSlot] is the single place that interprets this -- a consumer widget
 * built on the same primitives ([UiPrimitiveScope.claimSlot]/[UiPrimitiveScope.emit]/[io.github.ronjunevaldoz.awake.ui.graphics.border]) can
 * define its own variant vocabulary instead of this one; nothing else in the library assumes
 * these three exist.
 */
enum class UiButtonVariant {
    Filled,
    Outline,
    Ghost,
}

internal fun UiButtonVariant.resolveFill(
    baseColor: Color,
    hovered: Boolean,
    active: Boolean,
): Color =
    when (this) {
        UiButtonVariant.Filled -> baseColor
        UiButtonVariant.Outline, UiButtonVariant.Ghost -> if (hovered || active) baseColor else Color.Transparent
    }
