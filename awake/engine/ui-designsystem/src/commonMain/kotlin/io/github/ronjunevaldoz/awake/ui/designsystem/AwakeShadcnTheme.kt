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
        override val background = floatArrayOf(0.06f, 0.07f, 0.09f, 1f)
        override val foreground = floatArrayOf(0.96f, 0.97f, 0.99f, 1f)
        override val primary = floatArrayOf(0.91f, 0.92f, 0.96f, 1f)
        override val primaryForeground = floatArrayOf(0.09f, 0.1f, 0.13f, 1f)
        override val secondary = floatArrayOf(0.15f, 0.17f, 0.21f, 1f)
        override val secondaryForeground = floatArrayOf(0.9f, 0.92f, 0.96f, 1f)
        override val muted = floatArrayOf(0.12f, 0.14f, 0.18f, 1f)
        override val mutedForeground = floatArrayOf(0.63f, 0.67f, 0.74f, 1f)
        override val accent = floatArrayOf(0.26f, 0.48f, 0.95f, 1f)
        override val accentForeground = floatArrayOf(0.96f, 0.97f, 0.99f, 1f)
        override val destructive = floatArrayOf(0.82f, 0.25f, 0.28f, 1f)
        override val destructiveForeground = floatArrayOf(0.99f, 0.98f, 0.98f, 1f)
        override val border = floatArrayOf(0.18f, 0.2f, 0.24f, 1f)
    }

    override val components: UiComponentStyles = object : UiComponentStyles {
        override val button: Style = Style {
            background(tokens.secondary)
            foreground(tokens.secondaryForeground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(6f.dp)
            hovered { background(tokens.muted) }
            active { background(tokens.accent) }
        }
        override val toggle: Style = button
        override val checkbox: Style = Style {
            background(tokens.background)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(6f.dp)
            hovered { background(tokens.muted) }
            active { background(tokens.secondary) }
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
            background(tokens.background)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(8f.dp)
            contentPadding(UiSpacing.sm)
        }
    }
}
