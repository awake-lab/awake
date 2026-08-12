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

/**
 * Resolves [SurfaceVisuals] for a [ShadcnTextFieldVariant].
 */
fun ShadcnTextFieldVariant.visuals(values: UiThemeValues): SurfaceVisuals {
    val colors = values.colors
    val shapes = values.shapes
    val rest = when (this) {
        ShadcnTextFieldVariant.Default -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.input),
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Filled -> SurfaceStyle(
            background = colors.muted,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
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
