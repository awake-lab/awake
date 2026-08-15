// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnSurfaceStyle(values: UiThemeValues, variant: ShadcnSurfaceVariant?): Style =
    when (variant) {
        ShadcnSurfaceVariant.Muted -> Style {
            background(values.colors.muted)
            foreground(values.colors.foreground)
            shape(values.shapes.lg)
            contentPadding(16f.dp)
        }

        else -> Style {
            background(values.colors.card)
            foreground(values.colors.cardForeground)
            border(1f.dp, values.colors.border)
            shape(values.shapes.lg)
            contentPadding(24f.dp)
        }
    }

internal fun shadcnPopoverContentStyle(values: UiThemeValues): Style = Style {
    background(values.colors.popover)
    foreground(values.colors.popoverForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.md)
    contentPadding(16f.dp)
}
