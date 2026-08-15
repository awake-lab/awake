// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
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

    // Reference hover treatments (button.tsx): primary/90, secondary/80, destructive/90,
    // outline+ghost -> accent. Link changes only text decoration, which text styling cannot
    // express yet, so it keeps its rest colors. No separate pressed styles: shadcn buttons
    // keep the hover treatment while pressed. State styles must re-pass cornerRadius and
    // contentPadding -- a state rule's SurfaceStyle applies its (zero) padding unconditionally.
    fun hover(background: Color, foreground: Color) = SurfaceStyle(
        background = background,
        foreground = foreground,
        cornerRadius = if (this == ShadcnButtonVariant.Link) theme.shapes.xs else theme.shapes.md,
        contentPadding = insets,
    )
    return SurfaceVisuals(
        rest = rest,
        hovered = when (this) {
            ShadcnButtonVariant.Primary -> hover(colors.primary.withAlpha(0.9f), colors.primaryForeground)
            ShadcnButtonVariant.Secondary -> hover(colors.secondary.withAlpha(0.8f), colors.secondaryForeground)
            ShadcnButtonVariant.Danger -> hover(colors.destructive.withAlpha(0.9f), Color.White)
            ShadcnButtonVariant.Outline -> hover(colors.accent, colors.accentForeground)
            ShadcnButtonVariant.Ghost -> hover(colors.accent, colors.accentForeground)
            ShadcnButtonVariant.Link -> null
        },
    )
}
