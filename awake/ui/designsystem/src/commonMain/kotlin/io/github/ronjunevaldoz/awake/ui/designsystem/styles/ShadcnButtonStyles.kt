// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals

/**
 * Resolves [SurfaceVisuals] for a [ShadcnButtonVariant] and [ShadcnButtonSize].
 */
fun ShadcnButtonVariant.visuals(
    theme: UiThemeValues,
    size: ShadcnButtonSize,
): SurfaceVisuals {
    val colors = theme.colors
    val insets = UiInsets(horizontal = size.paddingX, vertical = 0f.dp)
    val rest = when (this) {
        ShadcnButtonVariant.Primary -> SurfaceStyle(
            background = colors.primary,
            foreground = colors.primaryForeground,
            cornerRadius = theme.shapes.md,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
        ShadcnButtonVariant.Secondary -> SurfaceStyle(
            background = colors.secondary,
            foreground = colors.secondaryForeground,
            cornerRadius = theme.shapes.md,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
        ShadcnButtonVariant.Outline -> SurfaceStyle(
            background = colors.background,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.input),
            cornerRadius = theme.shapes.md,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
        ShadcnButtonVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            cornerRadius = theme.shapes.md,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
        ShadcnButtonVariant.Danger -> SurfaceStyle(
            background = colors.destructive,
            foreground = Color.White,
            cornerRadius = theme.shapes.md,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
        ShadcnButtonVariant.Link -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.primary,
            cornerRadius = theme.shapes.xs,
            contentPadding = insets,
            textSize = theme.typography.body,
            fontWeight = FontWeight.Medium,
        )
    }

    return SurfaceVisuals(
        rest = rest,
        hovered = when (this) {
            ShadcnButtonVariant.Outline -> SurfaceStyle(colors.secondary, colors.secondaryForeground)
            ShadcnButtonVariant.Ghost -> SurfaceStyle(colors.accent, colors.accentForeground)
            else -> null
        },
        pressed = when (this) {
            ShadcnButtonVariant.Outline, ShadcnButtonVariant.Ghost -> SurfaceStyle(colors.accent, colors.accentForeground)
            else -> null
        },
    ).withDisabledDim(theme)
}

