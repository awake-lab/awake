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
    internal val card = oklch(0.205f, 0f)
    internal val popover = card
    internal val sidebar = oklch(0.18f, 0f)
    internal val input = oklch(1f, 0f, alpha = 0.15f)
    internal val ring = oklch(0.556f, 0f)
    private val hoverSurface = oklch(0.269f, 0f)
    private val activeSurface = oklch(0.32f, 0f)

    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = oklch(0.145f, 0f)
        override val foreground = oklch(0.985f, 0f)
        override val primary = oklch(0.922f, 0f)
        override val primaryForeground = oklch(0.205f, 0f)
        override val secondary = oklch(0.269f, 0f)
        override val secondaryForeground = oklch(0.985f, 0f)
        override val muted = oklch(0.269f, 0f)
        override val mutedForeground = oklch(0.708f, 0f)
        override val accent = oklch(0.269f, 0f)
        override val accentForeground = oklch(0.985f, 0f)
        override val destructive = oklch(0.704f, 0.191f, 22.216f)
        override val destructiveForeground = oklch(0.985f, 0f)
        override val border = oklch(1f, 0f, alpha = 0.1f)
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
