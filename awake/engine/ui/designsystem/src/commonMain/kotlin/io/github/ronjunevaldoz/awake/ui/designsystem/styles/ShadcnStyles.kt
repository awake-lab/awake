// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.sp
import io.github.ronjunevaldoz.awake.ui.style.Style

internal val ShadcnTransparent = Color.Transparent

// Real shadcn's Badge is text-xs regardless of the active preset's own `typography.caption`
// role (Vega's caption is 11sp, tuned for other small-text spots) -- pinned to the literal
// Tailwind value (0.75rem = 12dp) rather than reusing that shared token.
private val ShadcnBadgeTextSize = 12f.sp

object ShadcnStyles {
    fun button(variant: ShadcnButtonVariant): Style = button(ShadcnTheme, variant)

    internal fun button(
        theme: ShadcnResolvedTheme,
        variant: ShadcnButtonVariant,
    ): Style = when (variant) {
        ShadcnButtonVariant.Primary -> Style {
            background(theme.palette.primary, tokenId = "primary")
            foreground(theme.palette.primaryForeground, tokenId = "primary-foreground")
            shape(theme.radii.md)
            textSize(theme.typography.label, tokenId = "label")
            hovered { background(theme.palette.primaryHover, tokenId = "primary-hover") }
            active { background(theme.palette.primaryPressed, tokenId = "primary-pressed") }
        }
        ShadcnButtonVariant.Secondary -> theme.components.button then Style {
            background(theme.palette.secondary, tokenId = "secondary")
            foreground(theme.palette.secondaryForeground, tokenId = "secondary-foreground")
            hovered { background(theme.palette.secondaryHover, tokenId = "secondary-hover") }
            active { background(theme.palette.secondaryPressed, tokenId = "secondary-pressed") }
        }
        ShadcnButtonVariant.Outline -> Style {
            background(theme.colors.background, tokenId = "background")
            foreground(theme.colors.foreground, tokenId = "foreground")
            borderWidth(1f.dp)
            borderColor(theme.colors.border, tokenId = "border")
            shape(theme.radii.md)
            textSize(theme.typography.label, tokenId = "label")
            hovered {
                background(theme.palette.secondary, tokenId = "secondary")
                foreground(theme.palette.secondaryForeground, tokenId = "secondary-foreground")
            }
            active {
                background(theme.palette.secondaryHover, tokenId = "secondary-hover")
                foreground(theme.palette.secondaryForeground, tokenId = "secondary-foreground")
            }
        }
        ShadcnButtonVariant.Ghost -> Style {
            background(ShadcnTransparent, tokenId = "transparent")
            foreground(theme.colors.foreground, tokenId = "foreground")
            shape(theme.radii.md)
            textSize(theme.typography.label, tokenId = "label")
            hovered {
                background(theme.palette.accent, tokenId = "accent")
                foreground(theme.palette.accentForeground, tokenId = "accent-foreground")
            }
            active {
                background(theme.palette.accentHover, tokenId = "accent-hover")
                foreground(theme.palette.accentForeground, tokenId = "accent-foreground")
            }
        }
        ShadcnButtonVariant.Danger -> Style {
            background(theme.palette.destructive, tokenId = "destructive")
            foreground(theme.palette.destructiveForeground, tokenId = "destructive-foreground")
            shape(theme.radii.md)
            textSize(theme.typography.label, tokenId = "label")
            hovered { background(theme.palette.destructiveHover, tokenId = "destructive-hover") }
            active { background(theme.palette.destructivePressed, tokenId = "destructive-pressed") }
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
            background(ShadcnTransparent, tokenId = "transparent")
            foreground(theme.palette.primary, tokenId = "primary")
            shape(0f.dp)
            textSize(theme.typography.label, tokenId = "label")
            hovered { foreground(theme.palette.primaryHover, tokenId = "primary-hover") }
        }
    }

    fun badge(variant: ShadcnBadgeVariant): Style = badge(ShadcnTheme, variant)

    internal fun badge(
        theme: ShadcnResolvedTheme,
        variant: ShadcnBadgeVariant,
    ): Style = when (variant) {
        ShadcnBadgeVariant.Primary -> Style {
            background(theme.palette.primary, tokenId = "primary")
            foreground(theme.palette.primaryForeground, tokenId = "primary-foreground")
            shape(theme.radii.full)
            textSize(ShadcnBadgeTextSize, tokenId = "caption")
        }
        ShadcnBadgeVariant.Secondary -> Style {
            background(theme.palette.secondary, tokenId = "secondary")
            foreground(theme.palette.secondaryForeground, tokenId = "secondary-foreground")
            shape(theme.radii.full)
            textSize(ShadcnBadgeTextSize, tokenId = "caption")
        }
        ShadcnBadgeVariant.Outline -> Style {
            background(ShadcnTransparent, tokenId = "transparent")
            foreground(theme.colors.foreground, tokenId = "foreground")
            shape(theme.radii.full)
            borderWidth(1f.dp)
            borderColor(theme.input, tokenId = "input")
            textSize(ShadcnBadgeTextSize, tokenId = "caption")
        }
        ShadcnBadgeVariant.Danger -> Style {
            background(theme.palette.destructive, tokenId = "destructive")
            foreground(theme.palette.destructiveForeground, tokenId = "destructive-foreground")
            shape(theme.radii.full)
            textSize(ShadcnBadgeTextSize, tokenId = "caption")
        }
        // Real shadcn has no "ghost" badge variant; Awake's addition should still read as a
        // pill at rest, so it takes the muted (not fully transparent) fill rather than
        // ShadcnTransparent, which rendered with no visible background at all.
        ShadcnBadgeVariant.Ghost -> Style {
            background(theme.palette.muted, tokenId = "muted")
            foreground(theme.palette.mutedForeground, tokenId = "muted-foreground")
            shape(theme.radii.full)
            textSize(ShadcnBadgeTextSize, tokenId = "caption")
        }
    }

    fun surface(variant: ShadcnSurfaceVariant): Style = surface(ShadcnTheme, variant)

    internal fun surface(
        theme: ShadcnResolvedTheme,
        variant: ShadcnSurfaceVariant,
    ): Style = when (variant) {
        ShadcnSurfaceVariant.Muted -> Style {
            background(theme.palette.muted, tokenId = "muted")
            foreground(theme.colors.foreground, tokenId = "foreground")
            borderWidth(1f.dp)
            borderColor(theme.input, tokenId = "input")
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
    }

    val field: Style get() = field(ShadcnTheme)

    internal fun field(theme: ShadcnResolvedTheme): Style = field(theme, ShadcnTextFieldVariant.Default)

    fun field(variant: ShadcnTextFieldVariant): Style = field(ShadcnTheme, variant)

    internal fun field(theme: ShadcnResolvedTheme, variant: ShadcnTextFieldVariant): Style = when (variant) {
        ShadcnTextFieldVariant.Default -> Style {
            background(theme.colors.background, tokenId = "background")
            foreground(theme.colors.foreground, tokenId = "foreground")
            borderWidth(1f.dp)
            borderColor(theme.input, tokenId = "input")
            shape(theme.radii.md)
            contentPadding(theme.metrics.fieldPaddingX, theme.metrics.inputPaddingY)
            textSize(theme.typography.label, tokenId = "label")
            hovered {
                background(theme.card, tokenId = "card")
                borderColor(theme.colors.border, tokenId = "border")
            }
            active {
                background(theme.card, tokenId = "card")
                borderColor(theme.ring, tokenId = "ring")
            }
            // Border width stays constant across states (Default's rest width already matches
            // focus) -- only borderColor changes, so focusing never re-measures/shifts the field.
            focused {
                borderColor(theme.ring, tokenId = "ring")
            }
        }
        // Real shadcn's Filled text field: solid muted-gray fill, no border at all. Explicit
        // borderWidth(0) is required -- resolveStyle falls back to theme.components.textField's
        // 1dp default border for any property this style doesn't set, it doesn't start blank.
        ShadcnTextFieldVariant.Filled -> Style {
            background(theme.palette.muted, tokenId = "muted")
            foreground(theme.colors.foreground, tokenId = "foreground")
            borderWidth(UiShape.none)
            shape(theme.radii.md)
            contentPadding(theme.metrics.fieldPaddingX, theme.metrics.inputPaddingY)
            textSize(theme.typography.label, tokenId = "label")
            hovered { background(theme.palette.secondary, tokenId = "secondary") }
        }
        // Real shadcn's Ghost text field: no fill, no border -- label only, chrome appears
        // only once focused so the user still gets an affordance while typing.
        ShadcnTextFieldVariant.Ghost -> Style {
            background(ShadcnTransparent, tokenId = "transparent")
            foreground(theme.colors.foreground, tokenId = "foreground")
            // Border width is reserved at rest (transparent, invisible) so focusing only swaps
            // borderColor instead of introducing a border width that wasn't there before --
            // avoids a focus-triggered re-measure/shift while keeping the "chrome appears only
            // once focused" affordance from this variant's own doc comment above.
            borderWidth(1f.dp)
            borderColor(ShadcnTransparent, tokenId = "transparent")
            shape(theme.radii.md)
            contentPadding(theme.metrics.fieldPaddingX, theme.metrics.inputPaddingY)
            textSize(theme.typography.label, tokenId = "label")
            focused {
                borderColor(theme.ring, tokenId = "ring")
            }
        }
    }

    val checkbox: Style get() = checkbox(ShadcnTheme)

    internal fun checkbox(theme: ShadcnResolvedTheme): Style = Style {
        background(theme.colors.background, tokenId = "background")
        foreground(theme.colors.foreground, tokenId = "foreground")
        borderWidth(1f.dp)
        borderColor(theme.input, tokenId = "input")
        shape(theme.radii.md)
        textSize(theme.typography.label, tokenId = "label")
        hovered {
            background(theme.card, tokenId = "card")
            borderColor(theme.colors.border, tokenId = "border")
        }
        active {
            background(theme.card, tokenId = "card")
            borderColor(theme.ring, tokenId = "ring")
        }
    }

    val slider: Style get() = slider(ShadcnTheme)

    internal fun slider(theme: ShadcnResolvedTheme): Style = Style {
        background(theme.input, tokenId = "input")
        foreground(theme.colors.foreground, tokenId = "foreground")
        borderWidth(1f.dp)
        borderColor(theme.input, tokenId = "input")
        shape(theme.radii.full)
        textSize(theme.typography.label, tokenId = "label")
    }

    internal fun badgeContent(theme: ShadcnResolvedTheme): Style = Style {
        contentPadding(theme.metrics.badgePaddingX, theme.metrics.badgePaddingY)
    }

    val kbd: Style get() = kbd(ShadcnTheme)

    // Real shadcn's Kbd: small monospace-ish key cap -- muted fill, thin border, tight
    // padding, sm radius (not badge's full pill).
    internal fun kbd(theme: ShadcnResolvedTheme): Style = Style {
        background(theme.palette.muted, tokenId = "muted")
        foreground(theme.colors.mutedForeground, tokenId = "muted-foreground")
        borderWidth(1f.dp)
        borderColor(theme.colors.border, tokenId = "border")
        shape(theme.radii.sm)
        contentPadding(theme.metrics.badgePaddingX, theme.metrics.badgePaddingY)
        textSize(theme.typography.caption, tokenId = "caption")
    }

    fun alert(variant: ShadcnAlertVariant): Style = alert(ShadcnTheme, variant)

    // Real shadcn's Alert has no hover/press states -- it's a static banner, not an
    // interactive control, so this is the only style call in this file with no state rules.
    internal fun alert(theme: ShadcnResolvedTheme, variant: ShadcnAlertVariant): Style = when (variant) {
        ShadcnAlertVariant.Default -> Style {
            background(theme.colors.background, tokenId = "background")
            foreground(theme.colors.foreground, tokenId = "foreground")
            borderWidth(1f.dp)
            borderColor(theme.colors.border, tokenId = "border")
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
        ShadcnAlertVariant.Destructive -> Style {
            background(theme.colors.background, tokenId = "background")
            foreground(theme.palette.destructive, tokenId = "destructive")
            borderWidth(1f.dp)
            borderColor(theme.palette.destructive, tokenId = "destructive")
            shape(theme.radii.lg)
            contentPadding(theme.metrics.panelPadding)
        }
    }
}
