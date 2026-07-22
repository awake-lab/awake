// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnTransparent
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.horizontalPx
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.basicText
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.verticalPx

/** Real shadcn's `Badge`: an inline status pill -- defaults to Secondary, but
 * [AwakeShadcnBadgeVariant] covers all semantic variants (Primary, Secondary, Outline,
 * Destructive). */
fun UiScope.awakeShadcnBadge(
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

/** Full-slot [awakeShadcnBadge] override for when the caller already owns the [width] and
 * [height] intent (e.g. from an absolute-anchored HUD corner). */
fun UiScope.awakeShadcnBadge(
    label: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnBadgeVariant = AwakeShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
): UiSlot {
    val shadcnTheme = theme.asAwakeShadcnTheme()
    val resolved = resolveStyle(style = style, defaults = AwakeShadcnStyles.badge(shadcnTheme, variant))
    val resolvedFont = font
    val glyphPx = resolveGlyphPx(resolvedFont, resolved.textStyle)
    val resolvedWidth = when (width) {
        Dimension.WrapContent -> Dimension.Fixed(
            (resolvedFont.measureTextWidth(label, glyphPx) +
                                    resolved.contentPadding.horizontalPx()).px
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
    basicText(
        label = label,
        slot = slot,
        font = font,
        color = resolved.foreground ?: shadcnTheme.tokens.foreground,
        centered = true,
        textStyle = resolved.textStyle,
    )
    return slot
}

/** Real shadcn's `Kbd`: an inline key-cap label, same "measure text, draw a box, draw the
 * label" mechanics as [awakeShadcnBadge] with a different (sm-radius, muted) style. */
fun UiScope.awakeShadcnKbd(
    label: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.kbd(theme.asAwakeShadcnTheme()) then style,
    centered = true
)
