// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Resolves a stateful [Style] for a [ShadcnButtonVariant] and [ShadcnButtonSize].
 */
fun ShadcnButtonVariant.visuals(
    theme: UiThemeValues,
    size: ShadcnButtonSize,
): Style {
    val colors = theme.colors
    val shape = if (this == ShadcnButtonVariant.Link) theme.shapes.xs else theme.shapes.md
    return Style {
        when (this@visuals) {
            ShadcnButtonVariant.Primary -> {
                background(colors.primary, "primary")
                foreground(colors.primaryForeground, "primary-foreground")
            }
            ShadcnButtonVariant.Secondary -> {
                background(colors.secondary, "secondary")
                foreground(colors.secondaryForeground, "secondary-foreground")
            }
            ShadcnButtonVariant.Outline -> {
                background(colors.background, "background")
                foreground(colors.foreground, "foreground")
                border(1f.dp, colors.input, "input")
            }
            ShadcnButtonVariant.Ghost -> {
                background(Color.Transparent)
                foreground(colors.foreground, "foreground")
            }
            ShadcnButtonVariant.Danger -> {
                background(colors.destructive, "destructive")
                foreground(Color.White)
            }
            ShadcnButtonVariant.Link -> {
                background(Color.Transparent)
                foreground(colors.primary, "primary")
            }
        }
        shape(shape)
        contentPadding(size.paddingX, 0f.dp)
        textSize(theme.typography.body)
        fontWeight(FontWeight.Medium)
        when (this@visuals) {
            ShadcnButtonVariant.Primary -> hovered { background(colors.primary.withAlpha(0.9f), "primary") }
            ShadcnButtonVariant.Secondary -> hovered { background(colors.secondary.withAlpha(0.8f), "secondary") }
            ShadcnButtonVariant.Danger -> hovered { background(colors.destructive.withAlpha(0.9f), "destructive") }
            ShadcnButtonVariant.Outline, ShadcnButtonVariant.Ghost -> hovered {
                background(colors.accent, "accent")
                foreground(colors.accentForeground, "accent-foreground")
            }
            ShadcnButtonVariant.Link -> Unit
        }
    }
}
