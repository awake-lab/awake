// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.UiComponentStyles
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.dp

/**
 * A neutral-first, shadcn-inspired design-system theme that lives OUTSIDE the engine core.
 * It proves Awake's public UI API is enough to host a branded layer without modifying
 * `ui-core` or `ui-widgets`.
 */
object AwakeShadcnTheme : UiTheme {
    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = floatArrayOf(0.035f, 0.035f, 0.043f, 1f)
        override val foreground = floatArrayOf(0.98f, 0.98f, 0.985f, 1f)
        override val primary = floatArrayOf(0.98f, 0.98f, 0.985f, 1f)
        override val primaryForeground = floatArrayOf(0.07f, 0.07f, 0.08f, 1f)
        override val secondary = floatArrayOf(0.102f, 0.11f, 0.137f, 1f)
        override val secondaryForeground = floatArrayOf(0.98f, 0.98f, 0.985f, 1f)
        override val muted = floatArrayOf(0.168f, 0.176f, 0.215f, 1f)
        override val mutedForeground = floatArrayOf(0.72f, 0.74f, 0.79f, 1f)
        override val accent = floatArrayOf(0.195f, 0.208f, 0.258f, 1f)
        override val accentForeground = floatArrayOf(0.98f, 0.98f, 0.985f, 1f)
        override val destructive = floatArrayOf(0.64f, 0.18f, 0.22f, 1f)
        override val destructiveForeground = floatArrayOf(0.99f, 0.98f, 0.98f, 1f)
        override val border = floatArrayOf(0.246f, 0.258f, 0.316f, 1f)
    }

    override val components: UiComponentStyles = object : UiComponentStyles {
        override val button: Style = Style {
            background(tokens.secondary)
            foreground(tokens.secondaryForeground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(6f.dp)
            hovered { background(tokens.accent) }
            active { background(tokens.muted) }
        }
        override val toggle: Style = button
        override val checkbox: Style = Style {
            background(tokens.background)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(6f.dp)
            hovered { background(tokens.secondary) }
            active { background(tokens.accent) }
        }
        override val slider: Style = Style {
            background(tokens.muted)
            foreground(tokens.foreground)
            shape(999f.dp)
        }
        override val dropdown: Style = button then Style {
            contentPadding(UiSpacing.xs, UiSpacing.sm)
        }
        override val panel: Style = Style {
            background(tokens.secondary)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(8f.dp)
            contentPadding(UiSpacing.sm)
        }
    }
}
