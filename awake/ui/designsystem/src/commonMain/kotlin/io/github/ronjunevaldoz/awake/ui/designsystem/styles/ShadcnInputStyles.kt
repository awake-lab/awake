// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiThemeValues.shadcnTextFieldStyle(variant: ShadcnTextFieldVariant): Style =
    shadcnInputStyle(variant, UiInsets(12f.dp, ShadcnSpacing.xs))

fun UiThemeValues.shadcnTextareaStyle(variant: ShadcnTextFieldVariant): Style =
    shadcnInputStyle(variant, UiInsets(12f.dp, ShadcnSpacing.sm))

internal fun shadcnSelectOptionStyle(values: UiThemeValues): Style = Style {
    background(values.colors.popover)
    foreground(values.colors.popoverForeground)
    contentPadding(horizontal = 8f.dp, vertical = 6f.dp)
    textSize(values.typography.label)
    hovered { background(values.colors.accent); foreground(values.colors.accentForeground) }
    active { background(values.colors.accent); foreground(values.colors.accentForeground) }
    disabled { foreground(values.colors.mutedForeground) }
}

internal fun shadcnSelectedOptionStyle(values: UiThemeValues): Style = Style {
    background(values.colors.accent)
    foreground(values.colors.accentForeground)
    shape(values.shapes.md)
}

internal fun shadcnSliderStyle(values: UiThemeValues): Style = Style {
    background(values.colors.muted)
    foreground(values.colors.primary)
    shape(values.shapes.full)
}

internal fun shadcnInputGroupStyle(values: UiThemeValues): Style = Style {
    background(values.colors.background)
    border(1f.dp, values.colors.input)
    shape(values.shapes.md)
}

internal fun shadcnInputGroupAffixStyle(values: UiThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
}

internal fun shadcnInputGroupAffixTextStyle(values: UiThemeValues): Style = Style {
    textSize(values.typography.label)
}

internal fun shadcnInputOtpSlotStyle(values: UiThemeValues, enabled: Boolean, isError: Boolean): Style = Style {
    background(values.colors.card)
    foreground(if (enabled) values.colors.foreground else values.colors.mutedForeground)
    border(1f.dp, if (isError) values.colors.destructive else values.colors.input)
    shape(values.shapes.md)
}

private fun UiThemeValues.shadcnInputStyle(variant: ShadcnTextFieldVariant, padding: UiInsets): Style = Style {
    when (variant) {
        ShadcnTextFieldVariant.Default -> {
            background(Color.Transparent)
            foreground(colors.foreground)
            border(1f.dp, colors.input)
        }
        ShadcnTextFieldVariant.Filled -> {
            background(colors.muted)
            foreground(colors.foreground)
        }
        ShadcnTextFieldVariant.Ghost -> {
            background(Color.Transparent)
            foreground(colors.foreground)
        }
    }
    shape(shapes.md)
    contentPadding(padding.start, padding.top, padding.end, padding.bottom)
    textSize(typography.label)
    if (variant != ShadcnTextFieldVariant.Ghost) {
        hovered { background(colors.card) }
        active { background(colors.card) }
    }
    disabled { foreground(colors.mutedForeground) }
}
