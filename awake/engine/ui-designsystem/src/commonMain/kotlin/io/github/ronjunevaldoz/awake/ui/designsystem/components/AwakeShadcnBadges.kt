// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.verticalPx

fun UiScope.awakeShadcnBadge(
    label: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
) {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    val resolved = resolveStyle(style = style, defaults = AwakeShadcnStyles.badge(shadcnTheme, variant))
    val resolvedFont = font
    val glyphPx = resolvedFont?.let { resolveGlyphPx(it, resolved.textScale, resolved.textSize) } ?: 0f
    val resolvedWidth = when (width) {
        Dimension.WrapContent -> Dimension.Fixed(
            (
                (resolvedFont?.measureTextWidth(label, glyphPx) ?: label.length * glyphPx) +
                    resolved.contentPadding.horizontalPx()
                ).px
        )
        else -> width
    }
    val resolvedHeight = when (height) {
        Dimension.WrapContent -> Dimension.Fixed((glyphPx + resolved.contentPadding.verticalPx()).px)
        else -> height
    }
    val slot = claimModifiedSlot(resolvedWidth, resolvedHeight, modifier)
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: AwakeShadcnTransparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: shadcnTheme.tokens.border
    )
    if (font != null) {
        text(label, slot = slot, font = font, color = resolved.foreground ?: shadcnTheme.tokens.foreground, centered = true)
    }
}

fun UiScope.awakeShadcnBadge(
    label: String,
    width: Float = 96f,
    height: Float = 28f,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
) {
    awakeShadcnBadge(
        label = label,
        width = width.toDimension(),
        height = height.toDimension(),
        modifier = modifier,
        variant = variant,
        style = style
    )
}

fun UiDslScope.awakeShadcnBadge(
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.badge(theme.asAwakeShadcnTheme(), variant) then AwakeShadcnStyles.badgeContent(theme.asAwakeShadcnTheme()) then style,
    centered = true
)
