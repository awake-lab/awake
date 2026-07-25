// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.style.*

internal val ShadcnTransparent = Color.Transparent

object ShadcnStyles {
    fun button(variant: ShadcnButtonVariant): Style = button(ShadcnTheme, variant)

    internal fun button(
        theme: ShadcnResolvedTheme,
        variant: ShadcnButtonVariant
    ): Style = when (variant) {
        ShadcnButtonVariant.Primary -> Style {
            background(theme.palette.primary)
            foreground(theme.palette.primaryForeground)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered { background(theme.palette.primaryHover) }
            active { background(theme.palette.primaryPressed) }
        }
        ShadcnButtonVariant.Secondary -> theme.components.button then Style {
            hovered { background(theme.palette.secondaryHover) }
            active { background(theme.palette.secondaryPressed) }
        }
        ShadcnButtonVariant.Outline -> Style {
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
        ShadcnButtonVariant.Ghost -> Style {
            background(ShadcnTransparent)
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
        ShadcnButtonVariant.Danger -> Style {
            background(theme.palette.destructive)
            foreground(theme.palette.destructiveForeground)
            shape(theme.radii.lg)
            textSize(theme.typography.label)
            hovered { background(theme.palette.destructiveHover) }
            active { background(theme.palette.destructivePressed) }
        }
        // Real shadcn's Link renders as underlined text with zero button chrome (no fill,
        // no border, no padding box). No hovered/active background rule here on purpose --
        // Ghost's resolveFill only shows a background when hovered/active AND the resolved
        // background differs from the base rule; since this style never overrides
        // background() in either state, it stays ShadcnTransparent throughout, so hover
        // only shifts foreground color, never paints a fill. No underline: this engine's
        // Style system has no text-decoration property yet -- a real, documented gap, not a
        // silently-dropped corner.
        ShadcnButtonVariant.Link -> Style {
            background(ShadcnTransparent)
            foreground(theme.palette.primary)
            shape(0f.dp)
            textSize(theme.typography.label)
            hovered { foreground(theme.palette.primaryHover) }
        }
    }

    fun badge(variant: ShadcnBadgeVariant): Style = badge(ShadcnTheme, variant)

    internal fun badge(
        theme: ShadcnResolvedTheme,
        variant: ShadcnBadgeVariant
    ): Style = when (variant) {
        ShadcnBadgeVariant.Primary -> Style {
            background(theme.palette.primary)
            foreground(theme.palette.primaryForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
        ShadcnBadgeVariant.Secondary -> Style {
            background(theme.palette.secondary)
            foreground(theme.palette.secondaryForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
        ShadcnBadgeVariant.Outline -> Style {
            background(ShadcnTransparent)
            foreground(theme.tokens.foreground)
            shape(theme.radii.full)
            borderWidth(1f.dp)
            borderColor(theme.input)
            textSize(theme.typography.caption)
        }
        ShadcnBadgeVariant.Danger -> Style {
            background(theme.palette.destructive)
            foreground(theme.palette.destructiveForeground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
        ShadcnBadgeVariant.Ghost -> Style {
            background(ShadcnTransparent)
            foreground(theme.tokens.foreground)
            shape(theme.radii.full)
            textSize(theme.typography.caption)
        }
    }

    fun surface(variant: ShadcnSurfaceVariant): Style = surface(ShadcnTheme, variant)

    internal fun surface(
        theme: ShadcnResolvedTheme,
        variant: ShadcnSurfaceVariant
    ): Style = when (variant) {
        ShadcnSurfaceVariant.Sidebar -> Style {
            background(theme.sidebar)
            foreground(theme.onSidebar)
            borderWidth(1f.dp)
            borderColor(theme.sidebarBorder)
            shape(theme.radii.xl)
            contentPadding(theme.metrics.surfacePadding)
        }
        ShadcnSurfaceVariant.Popover -> Style {
            background(theme.popover)
            foreground(theme.onPopover)
            borderWidth(1f.dp)
            borderColor(theme.tokens.border)
            shape(theme.radii.xl)
            contentPadding(theme.metrics.panelPadding)
        }
        ShadcnSurfaceVariant.Muted -> Style {
            background(theme.palette.muted)
            foreground(theme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(theme.input)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
    }

    val field: Style get() = field(ShadcnTheme)

    internal fun field(theme: ShadcnResolvedTheme): Style = field(theme, ShadcnTextFieldVariant.Default)

    fun field(variant: ShadcnTextFieldVariant): Style = field(ShadcnTheme, variant)

    internal fun field(theme: ShadcnResolvedTheme, variant: ShadcnTextFieldVariant): Style = when (variant) {
        ShadcnTextFieldVariant.Default -> Style {
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
            focused {
                borderWidth(1.5f.dp)
                borderColor(theme.ring)
            }
        }
        // Real shadcn's Filled text field: solid muted-gray fill, no border at all. Explicit
        // borderWidth(0) is required -- resolveStyle falls back to theme.components.textField's
        // 1dp default border for any property this style doesn't set, it doesn't start blank.
        ShadcnTextFieldVariant.Filled -> Style {
            background(theme.palette.muted)
            foreground(theme.tokens.foreground)
            borderWidth(UiShape.none)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.fieldPaddingX, theme.metrics.fieldPaddingY)
            textSize(theme.typography.label)
            hovered { background(theme.palette.secondary) }
        }
        // Real shadcn's Ghost text field: no fill, no border -- label only, chrome appears
        // only once focused so the user still gets an affordance while typing.
        ShadcnTextFieldVariant.Ghost -> Style {
            background(ShadcnTransparent)
            foreground(theme.tokens.foreground)
            borderWidth(UiShape.none)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.fieldPaddingX, theme.metrics.fieldPaddingY)
            textSize(theme.typography.label)
            focused {
                borderWidth(1f.dp)
                borderColor(theme.ring)
            }
        }
    }

    val checkbox: Style get() = checkbox(ShadcnTheme)

    internal fun checkbox(theme: ShadcnResolvedTheme): Style = Style {
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

    val slider: Style get() = slider(ShadcnTheme)

    internal fun slider(theme: ShadcnResolvedTheme): Style = Style {
        background(theme.input)
        foreground(theme.tokens.foreground)
        borderWidth(1f.dp)
        borderColor(theme.input)
        shape(theme.radii.full)
        textSize(theme.typography.label)
    }

    internal fun badgeContent(theme: ShadcnResolvedTheme): Style = Style {
        contentPadding(theme.metrics.badgePaddingX, theme.metrics.badgePaddingY)
    }

    val kbd: Style get() = kbd(ShadcnTheme)

    // Real shadcn's Kbd: small monospace-ish key cap -- muted fill, thin border, tight
    // padding, sm radius (not badge's full pill).
    internal fun kbd(theme: ShadcnResolvedTheme): Style = Style {
        background(theme.palette.muted)
        foreground(theme.tokens.mutedForeground)
        borderWidth(1f.dp)
        borderColor(theme.tokens.border)
        shape(theme.radii.sm)
        contentPadding(theme.metrics.badgePaddingX, theme.metrics.badgePaddingY)
        textSize(theme.typography.caption)
    }

    fun alert(variant: ShadcnAlertVariant): Style = alert(ShadcnTheme, variant)

    // Real shadcn's Alert has no hover/press states -- it's a static banner, not an
    // interactive control, so this is the only style call in this file with no state rules.
    internal fun alert(theme: ShadcnResolvedTheme, variant: ShadcnAlertVariant): Style = when (variant) {
        ShadcnAlertVariant.Default -> Style {
            background(theme.tokens.background)
            foreground(theme.tokens.foreground)
            borderWidth(1f.dp)
            borderColor(theme.tokens.border)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
        ShadcnAlertVariant.Destructive -> Style {
            background(theme.tokens.background)
            foreground(theme.palette.destructive)
            borderWidth(1f.dp)
            borderColor(theme.palette.destructive)
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
    }
}
