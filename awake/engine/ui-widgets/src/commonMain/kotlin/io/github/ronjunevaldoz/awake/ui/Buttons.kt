// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

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
    val interaction = interact(id, width, height, modifier)
    val defaults = theme.components.button then Style {
        shape(radius)
        if (variant == UiButtonVariant.Outline) {
            borderWidth(1f.dp)
        }
    }
    val resolved = resolveStyle(
        style = style,
        defaults = defaults,
        state = MutableStyleState(hovered = interaction.hovered, active = interaction.active)
    )
    val baseFill = resolved.background ?: theme.tokens.background
    val fillColor = variant.resolveFill(baseFill, interaction.hovered, interaction.active)
    val contentSlot = interaction.slot.inset(resolved.contentPadding)
    emitFillAndBorder(
        slot = interaction.slot,
        fillColor = fillColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = resolved.shapeSpec
    )
    context.absolute(
        slot = contentSlot,
        font = font,
        theme = theme,
        textScale = resolved.textScale
    ).drawContent(contentSlot, resolved)
    recordSemantic(
        role = UiSemanticRole.Button,
        id = id,
        label = semanticLabel,
        bounds = interaction.slot,
        contentBounds = contentSlot
    )
    return UiButtonResult(interaction.clicked, interaction.slot)
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
    height = Dimension.Fixed(36f.px),
    modifier = modifier,
    style = style,
    variant = variant,
    radius = radius,
    semanticLabel = label,
    drawContent = { contentSlot, resolved ->
        if (label != null && font != null) {
            text(
                label = label,
                slot = contentSlot,
                font = font,
                color = resolved.foreground ?: theme.tokens.foreground,
                centered = centered,
                verticallyCentered = verticallyCentered,
                overflow = UiTextOverflow.Ellipsis,
                textScale = resolved.textScale,
                textSize = resolved.textSize,
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
    height = Dimension.Fixed(36f.px),
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
