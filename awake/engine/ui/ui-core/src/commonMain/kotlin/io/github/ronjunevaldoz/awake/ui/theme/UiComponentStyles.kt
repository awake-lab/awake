// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.style.Style

interface UiComponentStyles {
    val button: Style
    val toggle: Style
    val checkbox: Style
    val slider: Style
    val dropdown: Style
    val surface: Style
    val textField: Style
    val avatar: Style
}

class CoreUiComponentStyles(
    tokens: UiColorTokens,
    typography: UiTypography = UiTypography.Default,
) : UiComponentStyles {
    override val button: Style = tokens.neutralStyle() then Style {
        textSize(typography.label)
    }
    override val toggle: Style = tokens.neutralStyle() then Style {
        textSize(typography.label)
    }
    override val checkbox: Style = tokens.neutralStyle() then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
        textSize(typography.label)
    }
    override val avatar: Style = Style {
        background(tokens.muted)
        foreground(tokens.foreground)
        textSize(typography.label)
    }
    override val slider: Style = Style {
        background(tokens.background)
        foreground(tokens.foreground)
        borderWidth(1f.dp)
        borderColor(tokens.border)
        hovered { background(tokens.muted) }
        active { borderColor(tokens.accent) }
        textSize(typography.label)
    }
    override val dropdown: Style = tokens.neutralStyle() then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
        shape(UiShape.sm)
        textSize(typography.label)
    }
    override val surface: Style = Style {
        background(tokens.background)
        foreground(tokens.foreground)
        contentPadding(UiSpacing.sm)
    }
    override val textField: Style = Style {
        background(tokens.background)
        foreground(tokens.foreground)
        borderWidth(1f.dp)
        borderColor(tokens.border)
        shape(UiShape.sm)
        contentPadding(UiSpacing.sm)
        textSize(typography.label)
        focused { borderColor(tokens.primary) }
        disabled {
            background(tokens.muted)
            foreground(tokens.mutedForeground)
            borderColor(tokens.border)
        }
    }
}
