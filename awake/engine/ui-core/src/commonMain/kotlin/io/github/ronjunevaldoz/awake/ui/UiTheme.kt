// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

/**
 * Semantic color roles consumed by widgets and higher-level UI compositions.
 *
 * These names are general-purpose enough to support both neutral built-ins and authored
 * design-system themes without growing [UiTheme] every time the widget surface expands.
 */
interface UiColorTokens {
    val background: Color
    val foreground: Color
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

/** hovered -> muted, active -> accent, base -> background. */
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

class CoreUiComponentStyles(tokens: UiColorTokens) : UiComponentStyles {
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
}

/**
 * Neutral fallback theme for low-level UI APIs.
 *
 * This lives in `ui-core` because root scopes need a no-extra-dependency default, but it is
 * intentionally generic. Named authored themes such as `DefaultUiTheme`, `DarkUiTheme`, and
 * `LightUiTheme` belong in `ui-designsystem`.
 */
object CoreUiTheme : UiTheme {
    override val tokens = object : UiColorTokens {
        override val background = Color(0.25f, 0.25f, 0.28f, 0.9f)
        override val foreground = Color.White
        override val primary = Color(0.5f, 0.5f, 0.6f, 0.9f)
        override val primaryForeground = Color.White
        override val secondary = Color(0.35f, 0.35f, 0.4f, 0.9f)
        override val secondaryForeground = Color.White
        override val muted = Color(0.35f, 0.35f, 0.4f, 0.9f) // = old "hover" color
        override val mutedForeground = Color(0.8f, 0.8f, 0.8f, 1f)
        override val accent = Color(0.5f, 0.5f, 0.6f, 0.9f) // = old "active" color
        override val accentForeground = Color.White
        override val destructive = Color(0.8f, 0.2f, 0.2f, 0.9f)
        override val destructiveForeground = Color.White
        override val border = Color(0.4f, 0.4f, 0.45f, 0.9f)
    }
    override val components: UiComponentStyles = CoreUiComponentStyles(tokens)
}

private fun brighten(color: Color, brightness: Float): Color = color.brighten(brightness)
