// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope
import io.github.ronjunevaldoz.awake.ui.headless.pill

/** Real shadcn's `Badge`: an inline status pill -- defaults to Secondary, but
 * [ShadcnBadgeVariant] covers all semantic variants (Primary, Secondary, Outline,
 * Destructive). */
@Deprecated(
    message = "Use a Headless-native badge recipe. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnBadge(
    label: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
    style: Style = Style.Empty,
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.badge(
        theme.asShadcnTheme(),
        variant,
    ) then ShadcnStyles.badgeContent(theme.asShadcnTheme()) then style,
    centered = true,
)

/** Real shadcn's `Kbd`: an inline key-cap label, same "measure text, draw a box, draw the
 * label" mechanics as [shadcnBadge] with a different (sm-radius, muted) style. */
@Deprecated(
    message = "Use a Headless-native key-cap recipe. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnKbd(
    label: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): UiBounds = text(
    label = label,
    modifier = modifier,
    style = ShadcnStyles.kbd(theme.asShadcnTheme()) then style,
    centered = true,
)

/** Headless-native Shadcn badge recipe; variants only map neutral surface values here. */
fun HeadlessUiScope.shadcnBadge(
    id: String,
    label: String,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
): UiBounds {
    val colors = themeValues.colors
    val style = when (variant) {
        ShadcnBadgeVariant.Primary -> SurfaceStyle(colors.primary, colors.primaryForeground)
        ShadcnBadgeVariant.Secondary -> SurfaceStyle(colors.secondary, colors.secondaryForeground)
        ShadcnBadgeVariant.Outline -> SurfaceStyle(
            background = colors.background,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.border),
        )
        ShadcnBadgeVariant.Danger -> SurfaceStyle(colors.destructive, colors.destructiveForeground)
        ShadcnBadgeVariant.Ghost -> SurfaceStyle(colors.background, colors.foreground)
    }.copy(
        cornerRadius = colors.let { themeValues.shapes.full },
        contentPadding = UiInsets(8f.dp, 2f.dp),
        textSize = themeValues.typography.caption,
    )
    return pill(id = id, label = label, style = style)
}

/** Headless-native key-cap recipe built on the same neutral pill primitive as Badge. */
fun HeadlessUiScope.shadcnKbd(
    id: String,
    label: String,
): UiBounds = pill(
    id = id,
    label = label,
    style = SurfaceStyle(
        background = themeValues.colors.muted,
        foreground = themeValues.colors.foreground,
        border = SurfaceBorder(1f.dp, themeValues.colors.border),
        cornerRadius = themeValues.shapes.sm,
        contentPadding = UiInsets(6f.dp, 2f.dp),
        textSize = themeValues.typography.caption,
    ),
)
