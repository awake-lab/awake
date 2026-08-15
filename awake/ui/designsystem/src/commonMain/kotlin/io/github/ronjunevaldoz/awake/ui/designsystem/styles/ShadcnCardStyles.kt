// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Resolves the complete Style for a [ShadcnCardVariant].
 */
fun UiThemeValues.shadcnCardStyle(variant: ShadcnCardVariant): Style = Style {
    background(colors.card)
    foreground(colors.cardForeground)
    border(1f.dp, colors.border)
    shape(shapes.lg)
    contentPadding(24f.dp)
    if (variant == ShadcnCardVariant.Elevated) {
        shadow(color = Color.Black.withAlpha(0.16f), offsetY = 2f.dp, blurRadius = 4f.dp)
    }
}
