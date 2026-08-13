// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
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
    /** Control-outline/input surface token. Defaults to [border] for neutral themes. */
    val input: Color get() = border
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
    /** Runtime-free component visuals consumed by Core's adapter and Headless recipes. */
    val componentVisuals: UiThemeComponents get() = UiThemeComponents.Default
}

data class UiComponentVisuals(
    val background: Color? = null,
    val backgroundToken: String? = null,
    val foreground: Color? = null,
    val foregroundToken: String? = null,
    val borderWidth: Dp? = null,
    val borderColor: Color? = null,
    val borderColorToken: String? = null,
    val shape: Dp? = null,
    val contentPadding: UiInsets = UiInsets.Zero,
    val textSize: Sp? = null,
)

data class UiThemeComponents(
    val button: UiComponentVisuals = UiComponentVisuals(),
    val toggle: UiComponentVisuals = button,
    val checkbox: UiComponentVisuals = UiComponentVisuals(),
    val slider: UiComponentVisuals = UiComponentVisuals(),
    val dropdown: UiComponentVisuals = UiComponentVisuals(),
    val surface: UiComponentVisuals = UiComponentVisuals(),
    val textField: UiComponentVisuals = UiComponentVisuals(),
    val avatar: UiComponentVisuals = UiComponentVisuals(),
) {
    companion object {
        val Default = UiThemeComponents()
    }
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
