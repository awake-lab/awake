// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp

internal val AwakeShadcnTransparent = Color.Transparent

object AwakeShadcnStyles {
    fun button(variant: AwakeShadcnButtonVariant): Style = button(AwakeShadcnTheme, variant)

    internal fun button(
        theme: AwakeShadcnResolvedTheme,
        variant: AwakeShadcnButtonVariant
    ): Style = when (variant) {
        AwakeShadcnButtonVariant.Primary -> Style {
            background(theme.palette.primary)
            foreground(theme.palette.primaryForeground)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered { background(theme.palette.primaryHover) }
            active { background(theme.palette.primaryPressed) }
        }
        AwakeShadcnButtonVariant.Secondary -> theme.components.button then Style {
            hovered { background(theme.palette.secondaryHover) }
            active { background(theme.palette.secondaryPressed) }
        }
        AwakeShadcnButtonVariant.Outline -> Style {
            background(theme.tokens.background)
            foreground(theme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(theme.tokens.border)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered {
                background(theme.palette.secondary)
                foreground(theme.palette.secondaryForeground)
            }
            active {
                background(theme.palette.secondaryHover)
                foreground(theme.palette.secondaryForeground)
            }
        }
        AwakeShadcnButtonVariant.Ghost -> Style {
            background(AwakeShadcnTransparent)
            foreground(theme.tokens.foreground)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered {
                background(theme.palette.accent)
                foreground(theme.palette.accentForeground)
            }
            active {
                background(theme.palette.accentHover)
                foreground(theme.palette.accentForeground)
            }
        }
        AwakeShadcnButtonVariant.Danger -> Style {
            background(theme.palette.destructive)
            foreground(theme.palette.destructiveForeground)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered { background(theme.palette.destructiveHover) }
            active { background(theme.palette.destructivePressed) }
        }
    }

    fun badge(variant: AwakeShadcnBadgeVariant): Style = badge(AwakeShadcnTheme, variant)

    internal fun badge(
        theme: AwakeShadcnResolvedTheme,
        variant: AwakeShadcnBadgeVariant
    ): Style = when (variant) {
        AwakeShadcnBadgeVariant.Primary -> Style {
            background(theme.palette.primary)
            foreground(theme.palette.primaryForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
        AwakeShadcnBadgeVariant.Secondary -> Style {
            background(theme.palette.secondary)
            foreground(theme.palette.secondaryForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
        AwakeShadcnBadgeVariant.Outline -> Style {
            background(AwakeShadcnTransparent)
            foreground(theme.tokens.foreground)
            shape(theme.radii.full)
            borderWidth(1f.dp)
            borderColor(theme.input)
            textSize(theme.typography.caption)
        }
        AwakeShadcnBadgeVariant.Danger -> Style {
            background(theme.palette.destructive)
            foreground(theme.palette.destructiveForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
    }

    fun surface(variant: AwakeShadcnSurfaceVariant): Style = surface(AwakeShadcnTheme, variant)

    internal fun surface(
        theme: AwakeShadcnResolvedTheme,
        variant: AwakeShadcnSurfaceVariant
    ): Style = when (variant) {
        AwakeShadcnSurfaceVariant.Card -> theme.components.panel
        AwakeShadcnSurfaceVariant.Sidebar -> Style {
            background(theme.sidebar)
            foreground(theme.onSidebar)
            borderWidth(1f.dp)
            borderColor(theme.sidebarBorder)
            shape(theme.radii.xl)
            contentPadding(theme.metrics.surfacePadding)
        }
        AwakeShadcnSurfaceVariant.Popover -> Style {
            background(theme.popover)
            foreground(theme.onPopover)
            borderWidth(1f.dp)
            borderColor(theme.tokens.border)
            shape(theme.radii.xl)
            contentPadding(theme.metrics.panelPadding)
        }
        AwakeShadcnSurfaceVariant.Muted -> Style {
            background(theme.palette.muted)
            foreground(theme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(theme.input)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
    }

    val field: Style get() = field(AwakeShadcnTheme)

    internal fun field(theme: AwakeShadcnResolvedTheme): Style = Style {
        background(theme.tokens.background)
        foreground(theme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(theme.input)
        shape(theme.radii.lg)
        contentPadding(theme.metrics.fieldPaddingX, theme.metrics.fieldPaddingY)
        textSize(theme.typography.label)
        hovered {
            background(theme.card)
            borderColor(theme.tokens.border)
        }
        active {
            background(theme.card)
            borderColor(theme.ring)
        }
    }

    val checkbox: Style get() = checkbox(AwakeShadcnTheme)

    internal fun checkbox(theme: AwakeShadcnResolvedTheme): Style = Style {
        background(theme.tokens.background)
        foreground(theme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(theme.input)
        shape(theme.radii.md)
        textSize(theme.typography.label)
        hovered {
            background(theme.card)
            borderColor(theme.tokens.border)
        }
        active {
            background(theme.card)
            borderColor(theme.ring)
        }
    }

    val slider: Style get() = slider(AwakeShadcnTheme)

    internal fun slider(theme: AwakeShadcnResolvedTheme): Style = Style {
        background(theme.input)
        foreground(theme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(theme.input)
        shape(theme.radii.full)
        textSize(theme.typography.label)
        active { borderColor(theme.ring) }
    }

    internal fun badgeContent(theme: AwakeShadcnResolvedTheme): Style = Style {
        contentPadding(theme.metrics.badgePaddingX, theme.metrics.badgePaddingY)
    }
}
