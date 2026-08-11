// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.sp

/** Immutable semantic color values shared by UI layers. */
interface UiColorTokens {
    val background: Color
    val foreground: Color
    /** Elevated container surface. Defaults to [background] for themes without a separate role. */
    val card: Color get() = background
    /** Foreground paired with [card]. Defaults to [foreground]. */
    val cardForeground: Color get() = foreground
    /** Floating container surface. Defaults to [background] for themes without a separate role. */
    val popover: Color get() = background
    /** Foreground paired with [popover]. Defaults to [foreground]. */
    val popoverForeground: Color get() = foreground
    val primary: Color
    val primaryForeground: Color
    val secondary: Color
    val secondaryForeground: Color
    val muted: Color
    val mutedForeground: Color
    val accent: Color
    val accentForeground: Color
    val destructive: Color
    val destructiveForeground: Color
    val border: Color
}

/** Immutable semantic corner-radius values shared by UI layers. */
interface UiShapeTokens {
    val xs: Dp
    val sm: Dp
    val md: Dp
    val lg: Dp
    val xl: Dp
    val full: Dp
}

/**
 * Runtime-free theme values visible to reusable UI layers.
 *
 * This deliberately excludes component recipes. Core may add runtime fallback behavior, while
 * Design System recipes map these values to Headless visual states.
 */
interface UiThemeValues {
    val colors: UiColorTokens
    val typography: UiTypography
    val shapes: UiShapeTokens
}

/** Immutable semantic text-size values shared by UI layers. */
data class UiTypography(
    val caption: Sp = 12.sp,
    val label: Sp = 14.sp,
    val body: Sp = 16.sp,
    val title: Sp = 20.sp,
    val headline: Sp = 24.sp,
    val display: Sp = 30.sp,
) {
    companion object {
        val Default = UiTypography()
    }
}
