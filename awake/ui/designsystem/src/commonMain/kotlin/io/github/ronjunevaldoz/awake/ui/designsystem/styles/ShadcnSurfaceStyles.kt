// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnSurfaceStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics, variant: ShadcnSurfaceVariant?): Style =
    when (variant) {
        ShadcnSurfaceVariant.Muted -> Style {
            background(values.colors.muted)
            foreground(values.colors.foreground)
            shape(values.shapes.lg)
            contentPadding(metrics.surfacePadding)
        }

        else -> Style {
            background(values.colors.card)
            foreground(values.colors.cardForeground)
            border(1f.dp, values.colors.border)
            shape(values.shapes.lg)
            contentPadding(metrics.panelPadding)
        }
    }

internal fun shadcnPopoverContentStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics): Style = Style {
    background(values.colors.popover)
    foreground(values.colors.popoverForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.md)
    contentPadding(metrics.surfacePadding)
}
