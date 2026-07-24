// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTransparent
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Real shadcn's `Badge`: an inline status pill -- defaults to Secondary, but
 * [ShadcnBadgeVariant] covers all semantic variants (Primary, Secondary, Outline,
 * Destructive). */
fun UiScope.shadcnBadge(
    label: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.badge(theme.asShadcnTheme(), variant) then ShadcnStyles.badgeContent(theme.asShadcnTheme()) then style,
    centered = true
)

/** Full-slot [shadcnBadge] override for when the caller already owns the [width] and
 * [height] intent (e.g. from an absolute-anchored HUD corner). */
fun UiScope.shadcnBadge(
    label: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = Modifier,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty
): UiSlot {
    val shadcnTheme = theme.asShadcnTheme()
    val resolved = resolveStyle(style = style, defaults = ShadcnStyles.badge(shadcnTheme, variant))
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
    val slot = claimModifiedSlot(modifier.withSizeFallback(resolvedWidth, resolvedHeight))
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: ShadcnTransparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: shadcnTheme.tokens.border
    )
    text(
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
 * label" mechanics as [shadcnBadge] with a different (sm-radius, muted) style. */
fun UiScope.shadcnKbd(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): UiSlot = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.kbd(theme.asShadcnTheme()) then style,
    centered = true
)
