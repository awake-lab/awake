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
fun UiColorTokens.neutralStyle(): Style = Style {
    background(background)
    foreground(foreground)
    hovered { background(muted) }
    active { background(accent) }
}

/** Same state-varying shape as [neutralStyle], but for the destructive role. */
fun UiColorTokens.destructiveStyle(): Style = Style {
    background(destructive)
    foreground(destructiveForeground)
    hovered { background(brighten(destructive, 1.05f)) }
    active { background(brighten(destructive, 1.15f)) }
}

interface UiComponentStyles {
    val button: Style
    val toggle: Style
    val checkbox: Style
    val slider: Style
    val dropdown: Style
    val panel: Style
    val inspectorLabel: Style
}

/**
 * A complete, swappable look for a [UiScope] -- assigned once at `ui.column(..., theme =
 * MyTheme)` (or `absolute`), not per widget call. Mirrors Compose's `MaterialTheme.colorScheme`
 * role: the single place a consumer overrides to restyle an entire panel, while individual
 * widget calls can still pass their own [Style] to override just one instance. Tokens stay
 * the stable semantic palette; [components] maps those roles into concrete widget defaults.
 */
interface UiTheme {
    val tokens: UiColorTokens
    val components: UiComponentStyles
}

class DefaultUiComponentStyles(tokens: UiColorTokens) : UiComponentStyles {
    override val button: Style = tokens.neutralStyle()
    override val toggle: Style = tokens.neutralStyle()
    override val checkbox: Style = tokens.neutralStyle() then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
    }
    override val slider: Style = tokens.neutralStyle()
    override val dropdown: Style = tokens.neutralStyle() then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
        shape(UiShape.sm)
    }
    override val panel: Style = Style {
        background(tokens.background)
        foreground(tokens.foreground)
        contentPadding(UiSpacing.sm)
    }
    override val inspectorLabel: Style = Style {
        foreground(tokens.mutedForeground)
    }
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
    override val components: UiComponentStyles = DefaultUiComponentStyles(tokens)
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
    override val components: UiComponentStyles = DefaultUiComponentStyles(tokens)
}

private fun brighten(color: FloatArray, brightness: Float): FloatArray = floatArrayOf(
    (color[0] * brightness).coerceAtMost(1f),
    (color[1] * brightness).coerceAtMost(1f),
    (color[2] * brightness).coerceAtMost(1f),
    color[3]
)
