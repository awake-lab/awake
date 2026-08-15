// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiThemeValues.shadcnTextFieldStyle(variant: ShadcnTextFieldVariant): Style =
    shadcnInputStyle(variant, UiInsets(12f.dp, ShadcnSpacing.xs))

fun UiThemeValues.shadcnTextareaStyle(variant: ShadcnTextFieldVariant): Style =
    shadcnInputStyle(variant, UiInsets(12f.dp, ShadcnSpacing.sm))

private fun UiThemeValues.shadcnInputStyle(variant: ShadcnTextFieldVariant, padding: UiInsets): Style = Style {
    when (variant) {
        ShadcnTextFieldVariant.Default -> {
            background(Color.Transparent)
            foreground(colors.foreground)
            border(1f.dp, colors.input)
        }
        ShadcnTextFieldVariant.Filled -> {
            background(colors.muted)
            foreground(colors.foreground)
        }
        ShadcnTextFieldVariant.Ghost -> {
            background(Color.Transparent)
            foreground(colors.foreground)
        }
    }
    shape(shapes.md)
    contentPadding(padding.start, padding.top, padding.end, padding.bottom)
    textSize(typography.label)
    if (variant != ShadcnTextFieldVariant.Ghost) {
        hovered { background(colors.card) }
        active { background(colors.card) }
    }
    disabled { foreground(colors.mutedForeground) }
}

/**
 * Resolves [SurfaceVisuals] for a [ShadcnTextFieldVariant].
 */
fun ShadcnTextFieldVariant.visuals(values: UiThemeValues): SurfaceVisuals {
    val colors = values.colors
    val shapes = values.shapes
    val inputPadding = UiInsets(12f.dp, ShadcnSpacing.xs)
    val rest = when (this) {
        ShadcnTextFieldVariant.Default -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.input),
            cornerRadius = shapes.md,
            contentPadding = inputPadding,
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Filled -> SurfaceStyle(
            background = colors.muted,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = inputPadding,
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = inputPadding,
            textSize = values.typography.label,
        )
    }
    return SurfaceVisuals(
        rest = rest,
        hovered = if (this == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        pressed = if (this == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        disabled = rest.copy(foreground = colors.mutedForeground),
    )
}

fun ShadcnTextFieldVariant.textareaVisuals(values: UiThemeValues): SurfaceVisuals {
    val colors = values.colors
    val shapes = values.shapes
    val textareaPadding = UiInsets(12f.dp, ShadcnSpacing.sm)
    val rest = when (this) {
        ShadcnTextFieldVariant.Default -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.input),
            cornerRadius = shapes.md,
            contentPadding = textareaPadding,
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Filled -> SurfaceStyle(
            background = colors.muted,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = textareaPadding,
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = textareaPadding,
            textSize = values.typography.label,
        )
    }
    return SurfaceVisuals(
        rest = rest,
        hovered = if (this == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        pressed = if (this == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        disabled = rest.copy(foreground = colors.mutedForeground),
    )
}
