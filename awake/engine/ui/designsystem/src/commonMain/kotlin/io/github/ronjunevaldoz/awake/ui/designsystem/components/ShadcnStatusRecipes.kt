// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.pill
import io.github.ronjunevaldoz.awake.ui.headless.progress
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.skeleton
import io.github.ronjunevaldoz.awake.ui.headless.spinner
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.toast

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

private fun badgeStyle(
    values: io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues,
    variant: ShadcnBadgeVariant
): SurfaceStyle {
    val colors = values.colors
    return when (variant) {
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
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.border),
        )

        ShadcnBadgeVariant.Danger -> SurfaceStyle(
            background = colors.destructive,
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
        contentPadding = UiInsets(start = 10f.dp, top = 2.5f.dp, end = 10f.dp, bottom = 1.5f.dp),
        textSize = 12f.sp,
        lineHeight = 16f.sp,
        fontWeight = FontWeight.SemiBold,
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

fun ColumnScope.shadcnAlert(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier.fillMaxWidth(),
    style = SurfaceStyle(
        background = if (variant == ShadcnAlertVariant.Destructive) themeValues.colors.destructive.withAlpha(
            0.1f
        ) else themeValues.colors.muted,
        foreground = if (variant == ShadcnAlertVariant.Destructive) themeValues.colors.destructive else themeValues.colors.foreground,
        border = SurfaceBorder(
            1f.dp,
            if (variant == ShadcnAlertVariant.Destructive) themeValues.colors.destructive else themeValues.colors.border
        ),
        cornerRadius = themeValues.shapes.lg,
        contentPadding = UiInsets(16f.dp),
    ),
) {
    column(verticalArrangement = Arrangement.spacedBy(4f.dp)) {
        content()
    }
}

fun ColumnScope.shadcnAlert(
    id: String,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
): UiBounds = shadcnAlert(
    id = id,
    modifier = modifier,
    variant = variant,
) {
    shadcnText(title, visuals = SurfaceStyle(fontWeight = FontWeight.Medium))
    if (description != null) {
        shadcnText(description, visuals = SurfaceStyle(textSize = themeValues.typography.caption))
    }
}

fun ColumnScope.shadcnEmpty(
    id: String,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    action: (ColumnScope.() -> Unit)? = null,
): UiBounds = surface(
    id = id,
    modifier = modifier.fillMaxWidth(),
    style = SurfaceStyle(
        contentPadding = UiInsets(24f.dp),
    ),
) {
    column(
        horizontalAlignment = io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment.Horizontal.Center,
        verticalArrangement = Arrangement.spacedBy(8f.dp),
    ) {
        shadcnText(
            title,
            centered = true,
            visuals = SurfaceStyle(
                fontWeight = FontWeight.Medium,
                textSize = themeValues.typography.body
            ),
        )
        if (description != null) {
            shadcnText(
                description,
                centered = true,
                visuals = SurfaceStyle(textSize = themeValues.typography.caption),
            )
        }
        if (action != null) {
            column(
                horizontalAlignment = io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment.Horizontal.Center,
                verticalArrangement = Arrangement.spacedBy(4f.dp),
            ) {
                action()
            }
        }
    }
}

