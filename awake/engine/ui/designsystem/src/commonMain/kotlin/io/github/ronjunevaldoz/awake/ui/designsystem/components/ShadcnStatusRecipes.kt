// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.pill
import io.github.ronjunevaldoz.awake.ui.headless.separator

/** Branded status pill. Behavior and layout remain owned by ui-headless. */
fun UiScope.shadcnBadge(
    id: String,
    label: String,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
): UiBounds = pill(id = id, label = label, style = badgeStyle(themeValues, variant))

fun ColumnScope.shadcnBadge(
    id: String,
    label: String,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
): UiBounds = pill(id = id, label = label, style = badgeStyle(themeValues, variant))

fun RowScope.shadcnBadge(
    id: String,
    label: String,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
): UiBounds = pill(id = id, label = label, style = badgeStyle(themeValues, variant))

private fun badgeStyle(values: io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues, variant: ShadcnBadgeVariant): SurfaceStyle {
    val colors = values.colors
    return when (variant) {
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
        cornerRadius = values.shapes.full,
        contentPadding = UiInsets(8f.dp, 2f.dp),
        textSize = values.typography.caption,
    )
}

/** Branded key-cap pill. */
fun UiScope.shadcnKbd(id: String, label: String): UiBounds = pill(
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

fun ColumnScope.shadcnKbd(id: String, label: String): UiBounds = pill(
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

fun RowScope.shadcnKbd(id: String, label: String): UiBounds = pill(
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

fun UiScope.shadcnSeparator(
    modifier: Modifier = Modifier,
    thickness: io.github.ronjunevaldoz.awake.ui.api.Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
): UiBounds = separator(
    modifier = modifier,
    thickness = thickness,
    orientation = orientation,
    color = themeValues.colors.border,
)

fun ColumnScope.shadcnSeparator(
    modifier: Modifier = Modifier,
    thickness: io.github.ronjunevaldoz.awake.ui.api.Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
): UiBounds = separator(modifier, thickness, orientation, themeValues.colors.border)

fun RowScope.shadcnSeparator(
    modifier: Modifier = Modifier,
    thickness: io.github.ronjunevaldoz.awake.ui.api.Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
): UiBounds = separator(modifier, thickness, orientation, themeValues.colors.border)
