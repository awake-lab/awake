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
    internal val card = floatArrayOf(0.090527f, 0.090527f, 0.090527f, 1f)
    internal val popover = card
    internal val input = floatArrayOf(1f, 1f, 1f, 0.15f)
    internal val ring = floatArrayOf(0.451519f, 0.451519f, 0.451519f, 1f)
    private val hoverSurface = floatArrayOf(0.188f, 0.188f, 0.188f, 1f)
    private val activeSurface = floatArrayOf(0.225f, 0.225f, 0.225f, 1f)

    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = floatArrayOf(0.039388f, 0.039388f, 0.039388f, 1f)
        override val foreground = floatArrayOf(0.980256f, 0.980256f, 0.980256f, 1f)
        override val primary = floatArrayOf(0.898161f, 0.898161f, 0.898161f, 1f)
        override val primaryForeground = floatArrayOf(0.090527f, 0.090527f, 0.090527f, 1f)
        override val secondary = floatArrayOf(0.149382f, 0.149382f, 0.149382f, 1f)
        override val secondaryForeground = floatArrayOf(0.980256f, 0.980256f, 0.980256f, 1f)
        override val muted = floatArrayOf(0.149382f, 0.149382f, 0.149382f, 1f)
        override val mutedForeground = floatArrayOf(0.630163f, 0.630163f, 0.630163f, 1f)
        override val accent = floatArrayOf(0.149382f, 0.149382f, 0.149382f, 1f)
        override val accentForeground = floatArrayOf(0.980256f, 0.980256f, 0.980256f, 1f)
        override val destructive = floatArrayOf(1f, 0.391153f, 0.403857f, 1f)
        override val destructiveForeground = floatArrayOf(0.980256f, 0.980256f, 0.980256f, 1f)
        override val border = floatArrayOf(1f, 1f, 1f, 0.1f)
    }

    override val components: UiComponentStyles = object : UiComponentStyles {
        override val button: Style = Style {
            background(tokens.secondary)
            foreground(tokens.secondaryForeground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(6f.dp)
            hovered { background(hoverSurface) }
            active { background(activeSurface) }
        }
        override val toggle: Style = button
        override val checkbox: Style = Style {
            background(card)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(input)
            shape(6f.dp)
            hovered { background(hoverSurface) }
            active { background(activeSurface) }
        }
        override val slider: Style = Style {
            background(card)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(input)
            shape(999f.dp)
        }
        override val dropdown: Style = button then Style {
            contentPadding(UiSpacing.xs, UiSpacing.sm)
        }
        override val panel: Style = Style {
            background(card)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            shape(10f.dp)
            contentPadding(UiSpacing.sm)
        }
    }
}
