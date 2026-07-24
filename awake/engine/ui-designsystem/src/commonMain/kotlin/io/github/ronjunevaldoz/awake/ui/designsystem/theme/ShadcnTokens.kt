// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp

internal data class ShadcnRadiusScale(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val full: Dp
) {
    companion object {
        fun fromBase(base: Dp): ShadcnRadiusScale = ShadcnRadiusScale(
            xs = Dp(base.value * 0.4f),
            sm = Dp(base.value * 0.6f),
            md = Dp(base.value * 0.8f),
            lg = base,
            xl = Dp(base.value * 1.4f),
            full = 9999f.dp
        )
    }
}

internal data class ShadcnMetrics(
    val panelPadding: Dp,
    val surfacePadding: Dp,
    val fieldPaddingX: Dp,
    val fieldPaddingY: Dp,
    val badgePaddingX: Dp,
    val badgePaddingY: Dp
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
    val sidebarRing: Color
)
