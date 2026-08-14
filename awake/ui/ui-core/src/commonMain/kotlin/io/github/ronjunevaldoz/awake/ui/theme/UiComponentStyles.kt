// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiComponentVisuals
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeComponents
import io.github.ronjunevaldoz.awake.ui.api.theme.UiTypography
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
    visuals: UiThemeComponents = UiThemeComponents.Default,
) : UiComponentStyles {
    private fun style(visual: UiComponentVisuals, fallback: Style): Style = fallback then Style {
        visual.background?.let { background(it, tokenId = visual.backgroundToken) }
        visual.foreground?.let { foreground(it, tokenId = visual.foregroundToken) }
        visual.borderWidth?.let { borderWidth(it) }
        visual.borderColor?.let { borderColor(it, tokenId = visual.borderColorToken) }
        visual.shape?.let { shape(it) }
        if (visual.contentPadding != io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets.Zero) {
            contentPadding(
                visual.contentPadding.start,
                visual.contentPadding.top,
                visual.contentPadding.end,
                visual.contentPadding.bottom,
            )
        }
        visual.textSize?.let { textSize(it) }
    }

    override val button: Style = style(visuals.button, tokens.neutralStyle()) then Style {
        textSize(typography.label)
    }
    override val toggle: Style = style(visuals.toggle, tokens.neutralStyle()) then Style {
        textSize(typography.label)
    }
    override val checkbox: Style = style(visuals.checkbox, tokens.neutralStyle()) then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
        textSize(typography.label)
    }
    override val avatar: Style = style(
        visuals.avatar,
        Style {
            background(tokens.muted)
            foreground(tokens.foreground)
            textSize(typography.label)
        },
    )
    override val slider: Style = style(
        visuals.slider,
        Style {
            background(tokens.background)
            foreground(tokens.foreground)
            borderWidth(1f.dp)
            borderColor(tokens.border)
            hovered { background(tokens.muted) }
            active { borderColor(tokens.accent) }
            textSize(typography.label)
        },
    )
    override val dropdown: Style = style(visuals.dropdown, tokens.neutralStyle()) then Style {
        borderWidth(1f.dp)
        borderColor(tokens.border)
        shape(UiShape.sm)
        textSize(typography.label)
    }
    override val surface: Style = style(
        visuals.surface,
        Style {
            background(tokens.background)
            foreground(tokens.foreground)
            contentPadding(UiSpacing.sm)
        },
    )
    override val textField: Style = style(
        visuals.textField,
        Style {
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
        },
    )
}
