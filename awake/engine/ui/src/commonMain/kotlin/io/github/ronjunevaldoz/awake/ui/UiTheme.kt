// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Color roles, independent of any widget kind -- the seam any widget (built-in or
 * consumer-authored) themes against. Names match shadcn/ui's public vocabulary (the author
 * also maintains a shadcn-inspired Compose Multiplatform design system elsewhere) rather than
 * a vocabulary invented for this module alone. Fields present but unused by today's 4 widgets
 * (`secondary`/`destructive`/`border`) exist because they cost nothing to declare and the
 * next widget kind needs somewhere to go without [UiTheme] growing again.
 */
interface UiColorTokens {
    val background: FloatArray
    val foreground: FloatArray
    val primary: FloatArray
    val primaryForeground: FloatArray
    val secondary: FloatArray
    val secondaryForeground: FloatArray
    val muted: FloatArray
    val mutedForeground: FloatArray
    val accent: FloatArray
    val accentForeground: FloatArray
    val destructive: FloatArray
    val destructiveForeground: FloatArray
    val border: FloatArray
}

/** hovered -> muted (shadcn's hover convention), active -> accent, base -> background. */
fun UiColorTokens.neutralStyle(): UiStyle = UiStyle { state ->
    if (state.active) accent else if (state.hovered) muted else background
}

/** Same one-color-scales-by-state shape as [neutralStyle], but for the destructive role --
 * without this, a naive destructive style would return the same color for every
 * [UiWidgetState], which would be a real regression versus a hand-rolled danger theme (base/
 * hover/active reds). Brightness scaling keeps it a one-color input. */
fun UiColorTokens.destructiveStyle(): UiStyle = UiStyle { state ->
    val brightness = if (state.active) 1.15f else if (state.hovered) 1.05f else 1f
    floatArrayOf(
        (destructive[0] * brightness).coerceAtMost(1f),
        (destructive[1] * brightness).coerceAtMost(1f),
        (destructive[2] * brightness).coerceAtMost(1f),
        destructive[3]
    )
}

/**
 * A complete, swappable look for a [UiScope] -- assigned once at `ui.column(..., theme =
 * MyTheme)` (or `absolute`), not per widget call. Mirrors Compose's `MaterialTheme.colorScheme`
 * role: the single place a consumer overrides to restyle an entire panel, while individual
 * widget calls can still pass their own [UiStyle] to override just one instance. Deliberately
 * has no widget-named fields (no `button`/`toggle`) -- each widget in `Widgets.kt` computes
 * its own default from [tokens], the same way Material's `ButtonDefaults`/`SwitchDefaults`
 * map `ColorScheme` roles to a specific component instead of `ColorScheme` naming components.
 */
interface UiTheme {
    val tokens: UiColorTokens
}

/**
 * The exact colors `UiContext` used to hardcode, extracted into tokens so they're swappable
 * instead of baked into a private `colorFor` method. Every existing widget call keeps
 * rendering identically by defaulting to this.
 */
object DefaultUiTheme : UiTheme {
    override val tokens = object : UiColorTokens {
        override val background = floatArrayOf(0.25f, 0.25f, 0.28f, 0.9f)
        override val foreground = floatArrayOf(1f, 1f, 1f, 1f)
        override val primary = floatArrayOf(0.5f, 0.5f, 0.6f, 0.9f)
        override val primaryForeground = floatArrayOf(1f, 1f, 1f, 1f)
        override val secondary = floatArrayOf(0.35f, 0.35f, 0.4f, 0.9f)
        override val secondaryForeground = floatArrayOf(1f, 1f, 1f, 1f)
        override val muted = floatArrayOf(0.35f, 0.35f, 0.4f, 0.9f) // = old "hover" color
        override val mutedForeground = floatArrayOf(0.8f, 0.8f, 0.8f, 1f)
        override val accent = floatArrayOf(0.5f, 0.5f, 0.6f, 0.9f) // = old "active" color
        override val accentForeground = floatArrayOf(1f, 1f, 1f, 1f)
        override val destructive = floatArrayOf(0.8f, 0.2f, 0.2f, 0.9f)
        override val destructiveForeground = floatArrayOf(1f, 1f, 1f, 1f)
        override val border = floatArrayOf(0.4f, 0.4f, 0.45f, 0.9f)
    }
}

/** Explicit named alias for [DefaultUiTheme]'s palette, which was already effectively dark
 * (light text on a dark-gray background) -- `by DefaultUiTheme` delegation, same shape
 * [DangerUiTheme] (Slice B) already established, so picking "dark mode" doesn't require a
 * second color palette to maintain in lockstep with [DefaultUiTheme]. Exists so a consumer's
 * dark/light switch has a real named `UiTheme` on the dark side, not just "the default." */
object DarkUiTheme : UiTheme by DefaultUiTheme

/**
 * Light-mode counterpart to [DarkUiTheme] -- inverted luminance (light background, dark
 * foreground text), not just [DefaultUiTheme] with colors negated: shadcn's own light theme
 * keeps `primary`/`accent`/`destructive` at roughly the same saturation as the dark theme
 * (only `background`/`foreground`/`muted`/`border` actually flip), which is what real design
 * systems do to keep a brand's accent color recognizable across both modes.
 */
object LightUiTheme : UiTheme {
    override val tokens = object : UiColorTokens {
        override val background = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val foreground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val primary = floatArrayOf(0.2f, 0.2f, 0.24f, 1f)
        override val primaryForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val secondary = floatArrayOf(0.9f, 0.9f, 0.92f, 1f)
        override val secondaryForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val muted = floatArrayOf(0.9f, 0.9f, 0.92f, 1f) // hover
        override val mutedForeground = floatArrayOf(0.4f, 0.4f, 0.45f, 1f)
        override val accent = floatArrayOf(0.85f, 0.85f, 0.88f, 1f) // active
        override val accentForeground = floatArrayOf(0.1f, 0.1f, 0.12f, 1f)
        override val destructive = floatArrayOf(0.8f, 0.2f, 0.2f, 1f)
        override val destructiveForeground = floatArrayOf(0.98f, 0.98f, 0.99f, 1f)
        override val border = floatArrayOf(0.8f, 0.8f, 0.83f, 1f)
    }
}
