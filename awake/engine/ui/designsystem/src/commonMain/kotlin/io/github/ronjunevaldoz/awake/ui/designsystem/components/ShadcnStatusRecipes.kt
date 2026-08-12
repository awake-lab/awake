// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
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
ottr545r4456import io.github.ronjunevaldoz.awake.ui.headless.progress
import io.github.ronjunevaldoz.awake.ui.headless.skeleton
import io.github.ronjunevaldoz.awake.ui.headless.spinner
import io.github.ronjunevaldoz.awake.ui.headless.toast
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight

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
        // The upstream Badge recipe always includes `border`, even for filled variants; those
        // borders are transparent but still contribute one pixel on each side to box metrics.
        ShadcnBadgeVariant.Primary -> SurfaceStyle(
            background = colors.primary,
            foreground = colors.primaryForeground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )
        ShadcnBadgeVariant.Secondary -> SurfaceStyle(
            background = colors.secondary,
            foreground = colors.secondaryForeground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )
        ShadcnBadgeVariant.Outline -> SurfaceStyle(
            // The reference leaves the badge background transparent; the page/card beneath it
            // supplies the surface. This matters on dark cards and is not the same as a baked
            // background token.
            background = Color.Transparent,
            foreground = colors.foreground,
            // shadcn Badge outline uses `border-border` (not the input/control token).
            border = SurfaceBorder(1f.dp, colors.border),
        )
        ShadcnBadgeVariant.Danger -> SurfaceStyle(
            background = colors.destructive,
            // badge.tsx uses `text-white`, not the palette's destructive-foreground token.
            foreground = Color.White,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )
        ShadcnBadgeVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, Color.Transparent),
        )
    }.copy(
        cornerRadius = values.shapes.full,
        contentPadding = UiInsets(8f.dp, 2f.dp),
        // shadcn Badge is `text-xs` (12px), independent of the preset's compact caption tier.
        textSize = 12f.sp,
        // Tailwind `text-xs` carries a 1rem line-height; keeping it explicit makes the
        // border-box height 16px line box + 4px vertical padding + 2px border = 22px.
        lineHeight = 16f.sp,
        fontWeight = FontWeight.Medium,
    )
}

/** Branded key-cap pill. */
fun UiScope.shadcnKbd(id: String, label: String): UiBounds = pill(
    id = id,
    label = label,
    style = SurfaceStyle(
        background = themeValues.colors.muted,
        foreground = themeValues.colors.foreground,
        border = SurfaceBorder(1f.dp, themeValues.colors.input),
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
        border = SurfaceBorder(1f.dp, themeValues.colors.input),
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
        border = SurfaceBorder(1f.dp, themeValues.colors.input),
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

fun UiScope.shadcnProgress(
    id: String,
    value: Float,
    modifier: Modifier = Modifier,
): Unit = progress(
    id = id,
    value = value,
    modifier = modifier,
    visuals = SurfaceStyle(
        background = themeValues.colors.primary,
        foreground = themeValues.colors.primary.withAlpha(0.2f),
        cornerRadius = themeValues.shapes.full,
    ),
)

fun ColumnScope.shadcnProgress(
    id: String,
    value: Float,
    modifier: Modifier = Modifier,
): Unit = progress(
    id = id,
    value = value,
    modifier = modifier,
    visuals = SurfaceStyle(
        background = themeValues.colors.primary,
        foreground = themeValues.colors.primary.withAlpha(0.2f),
        cornerRadius = themeValues.shapes.full,
    ),
)

fun ColumnScope.shadcnSkeleton(
    id: String,
    modifier: Modifier = Modifier,
    shimmer: Boolean = false,
): Unit = skeleton(
    id = id,
    modifier = modifier,
    shimmer = shimmer,
    visuals = SurfaceStyle(
        background = themeValues.colors.muted,
        cornerRadius = themeValues.shapes.md,
    ),
)

fun UiScope.shadcnSpinner(
    id: String,
    modifier: Modifier = Modifier,
): Unit = spinner(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.primary,
    ),
)

fun ColumnScope.shadcnSpinner(
    id: String,
    modifier: Modifier = Modifier,
): Unit = spinner(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.primary,
    ),
)

fun UiScope.shadcnToast(
    id: String,
    message: String,
    modifier: Modifier = Modifier,
    durationMs: Float = 3000f,
): Boolean = toast(
    id = id,
    message = message,
    modifier = modifier,
    durationMs = durationMs,
    visuals = SurfaceStyle(
        background = themeValues.colors.card,
        foreground = themeValues.colors.cardForeground,
        border = SurfaceBorder(1f.dp, themeValues.colors.border),
        cornerRadius = themeValues.shapes.lg,
    ),
)
