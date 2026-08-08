// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.theme.UiShapeTokens

internal data class ShadcnRadiusScale(
    override val xs: Dp,
    override val sm: Dp,
    override val md: Dp,
    override val lg: Dp,
    override val xl: Dp,
    override val full: Dp,
) : UiShapeTokens {
    companion object {
        // shadcn's own scale is additive, not multiplicative: sm/md are offset DOWN from
        // --radius by a fixed 4dp/2dp step, lg IS --radius, xl is offset UP by 4dp (see
        // tailwind's `calc(var(--radius) - Npx)` convention in new-york-v4's globals.css). xs
        // has no shadcn counterpart -- extended one more 2dp step below sm to keep the scale's
        // even spacing. Clamped at 0 so a small base preset (e.g. Lyra's 0dp) can't go negative.
        fun fromBase(base: Dp): ShadcnRadiusScale = ShadcnRadiusScale(
            xs = (base.value - 6f).coerceAtLeast(0f).dp,
            sm = (base.value - 4f).coerceAtLeast(0f).dp,
            md = (base.value - 2f).coerceAtLeast(0f).dp,
            lg = base,
            xl = (base.value + 4f).dp,
            full = 9999f.dp,
        )
    }
}

/** shadcn-matching spacing names, wrapping [UiSpacing]'s dp values instead of duplicating them --
 * ui-core owns the raw scale, this just renames/extends it for shadcn call sites (e.g. `theme.spacing.xxl`). */
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
