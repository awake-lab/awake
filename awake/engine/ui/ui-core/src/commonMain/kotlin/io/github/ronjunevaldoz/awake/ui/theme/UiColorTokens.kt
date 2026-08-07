// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Semantic color roles consumed by widgets and higher-level UI compositions.
 *
 * These names are general-purpose enough to support both neutral built-ins and authored
 * design-system themes without growing [io.github.ronjunevaldoz.awake.ui.theme.UiTheme] every time the widget surface expands.
 */
interface UiColorTokens {
    val background: Color
    val foreground: Color

    /** The brand/emphasis color -- use this for a widget's persistent "on" state. */
    val primary: Color
    val primaryForeground: Color
    val secondary: Color
    val secondaryForeground: Color
    val muted: Color
    val mutedForeground: Color

    /** A transient surface-highlight color for hover/press/selection feedback. */
    val accent: Color
    val accentForeground: Color
    val destructive: Color
    val destructiveForeground: Color
    val border: Color
}

/** hovered -> muted, active -> accent, base -> background. */
fun UiColorTokens.neutralStyle(): Style =
    Style {
        background(background)
        foreground(foreground)
        hovered { background(muted) }
        active { background(accent) }
    }

/** Same state-varying shape as [neutralStyle], but for the destructive role. */
fun UiColorTokens.destructiveStyle(): Style =
    Style {
        background(destructive)
        foreground(destructiveForeground)
        hovered { background(brighten(destructive, 1.05f)) }
        active { background(brighten(destructive, 1.15f)) }
    }

private fun brighten(color: Color, brightness: Float): Color = color.brighten(brightness)
