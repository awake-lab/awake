// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiShapeTokens

internal data class ShadcnRadiusScale(
    override val xs: Dp,
    override val sm: Dp,
    override val md: Dp,
    override val lg: Dp,
    override val xl: Dp,
    override val full: Dp,
) : UiShapeTokens {
    companion object {
        // Multiplicative, matching Tailwind v4 / new-york-v4's real derivation (verified against
        // the pinned reference app's own index.css):
        //   --radius-sm: calc(var(--radius) * 0.6)   --radius-lg: var(--radius)
        //   --radius-md: calc(var(--radius) * 0.8)   --radius-xl: calc(var(--radius) * 1.4)
        // This was previously additive (base-4 / base-2 / base+4) with a comment asserting
        // shadcn's scale "is additive, not multiplicative" -- factually wrong, but it produced
        // IDENTICAL numbers at Vega's base of 10dp (6/8/10/14), so the preset the parity
        // comparisons use was correct by coincidence while every other preset drifted (Nova's
        // base of 5dp gave 1/3/9 instead of the real 3/4/7).
        // xs has no shadcn counterpart -- kept as one step below sm on the same ratio curve.
        // Clamped at 0 so a 0dp base preset (Lyra) can't go negative.
        fun fromBase(base: Dp): ShadcnRadiusScale = ShadcnRadiusScale(
            xs = (base.value * 0.4f).coerceAtLeast(0f).dp,
            sm = (base.value * 0.6f).coerceAtLeast(0f).dp,
            md = (base.value * 0.8f).coerceAtLeast(0f).dp,
            lg = base,
            xl = (base.value * 1.4f).coerceAtLeast(0f).dp,
            full = 9999f.dp,
        )
    }
}

/** shadcn-matching spacing names, wrapping [UiSpacing]'s dp values instead of duplicating them --
 * ui-core owns the raw scale, this just renames/extends it for shadcn call sites (e.g. `theme.spacing.xxl`).
 *
 * This object is the ONE spacing vocabulary `ui-designsystem` components should speak. Reaching
 * past it to [Tw.Spacing] directly inside a component is what splits the module into two competing
 * vocabularies for the same concept -- add the missing step here instead, the way [smd]/[lgx] were.
 * `Tw` is this object's source of truth, not a parallel API for call sites. */
internal object ShadcnSpacing {
    val xs: Dp = UiSpacing.xs
    val sm: Dp = UiSpacing.sm
    val md: Dp = UiSpacing.md
    val lg: Dp = UiSpacing.lg
    val xl: Dp = UiSpacing.xl
    val xxl: Dp = 48f.dp
}

internal data class ShadcnMetrics(
    // Card/Dialog/Muted-surface/Alert inset -- real shadcn's Card/Dialog p-6.
    val panelPadding: Dp,
    // Popover's own inset -- real shadcn's Popover p-4, deliberately smaller than
    // [panelPadding]; kept as its own field rather than reusing panelPadding since real shadcn
    // does NOT share one inset value across all three surfaces.
    val surfacePadding: Dp,
    val fieldPaddingX: Dp,
    // Select-trigger's own vertical inset (real shadcn's SelectTrigger py-2) -- see
    // [inputPaddingY] for the real text-Input's distinct (smaller) py-1 value; the two used to
    // incorrectly share this one field.
    val fieldPaddingY: Dp,
    val badgePaddingX: Dp,
    val badgePaddingY: Dp,
    // Real text-Input's vertical inset (real shadcn's Input py-1) -- smaller than
    // [fieldPaddingY]'s SelectTrigger py-2.
    val inputPaddingY: Dp,
)

internal data class ShadcnPalette(
    val background: Color,
    val foreground: Color,
    val primary: Color,
    val primaryForeground: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val secondary: Color,
    val secondaryForeground: Color,
    val secondaryHover: Color,
    val secondaryPressed: Color,
    val muted: Color,
    val mutedForeground: Color,
    val accent: Color,
    val accentForeground: Color,
    val accentHover: Color,
    val accentPressed: Color,
    val destructive: Color,
    val destructiveForeground: Color,
    val destructiveHover: Color,
    val destructivePressed: Color,
    val border: Color,
    val ring: Color,
    val input: Color,
    val card: Color,
    val cardForeground: Color,
    val popover: Color,
    val popoverForeground: Color,
    val sidebar: Color,
    val sidebarForeground: Color,
    val sidebarPrimary: Color,
    val sidebarPrimaryForeground: Color,
    val sidebarAccent: Color,
    val sidebarAccentForeground: Color,
    val sidebarBorder: Color,
    val sidebarRing: Color,
)
