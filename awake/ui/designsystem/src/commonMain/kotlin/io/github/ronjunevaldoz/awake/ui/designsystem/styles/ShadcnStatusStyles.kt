// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

fun shadcnProgressStyle(values: UiThemeValues): Style = Style {
    background(values.colors.primary.withAlpha(0.2f))
    foreground(values.colors.primary)
    border(0f.dp, Color.Transparent)
    shape(values.shapes.full)
}

fun shadcnSkeletonStyle(values: UiThemeValues): Style = Style {
    background(values.colors.muted)
    shape(values.shapes.md)
}

fun shadcnSpinnerStyle(values: UiThemeValues): Style = Style {
    foreground(values.colors.primary)
}

internal fun shadcnKbdStyle(values: UiThemeValues): Style = Style {
    background(values.colors.muted)
    foreground(values.colors.mutedForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.xs)
    contentPadding(6f.dp, 0f.dp, 6f.dp, 0f.dp)
    textSize(10f.sp)
}

internal fun shadcnAlertStyle(values: UiThemeValues, variant: ShadcnAlertVariant): Style = Style {
    background(if (variant == ShadcnAlertVariant.Destructive) values.colors.destructive.withAlpha(0.1f) else values.colors.muted)
    foreground(if (variant == ShadcnAlertVariant.Destructive) values.colors.destructive else values.colors.foreground)
    border(1f.dp, if (variant == ShadcnAlertVariant.Destructive) values.colors.destructive else values.colors.border)
    shape(values.shapes.lg)
    contentPadding(16f.dp)
}

internal fun shadcnStatusEmptyStyle(): Style = Style { contentPadding(24f.dp) }
