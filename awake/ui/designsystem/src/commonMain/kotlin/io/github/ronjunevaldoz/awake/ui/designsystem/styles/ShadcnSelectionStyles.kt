// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Shared Shadcn visual treatment for toggle-group items. */
fun shadcnToggleGroupItemStyle(values: UiThemeValues): Style = Style {
    background(values.colors.card, "card")
    foreground(values.colors.foreground, "foreground")
    border(1f.dp, values.colors.border, "border")
    shape(values.shapes.md)
}

/** Shared Shadcn visual treatment for radio indicators. */
fun shadcnRadioStyle(values: UiThemeValues): Style = Style {
    background(values.colors.background, "background")
    foreground(values.colors.primary, "primary")
    border(1f.dp, values.colors.border, "border")
    shape(values.shapes.full)
}

internal fun shadcnCheckboxStyle(values: UiThemeValues, checked: Boolean): Style = Style {
    background(if (checked) values.colors.primary else values.colors.background)
    foreground(if (checked) values.colors.primaryForeground else values.colors.foreground)
    border(1f.dp, if (checked) values.colors.primary else values.colors.input)
    shape(4f.dp)
    disabled { foreground(values.colors.mutedForeground) }
}

internal fun shadcnSwitchStyle(values: UiThemeValues, checked: Boolean): Style = Style {
    background(if (checked) values.colors.primary else values.colors.input)
    foreground(if (checked) values.colors.primaryForeground else values.colors.foreground)
    shape(values.shapes.full)
    disabled { foreground(values.colors.mutedForeground) }
}

internal fun shadcnToggleStyle(values: UiThemeValues, checked: Boolean): Style = Style {
    background(if (checked) values.colors.accent else values.colors.background)
    foreground(values.colors.foreground)
    shape(values.shapes.md)
    contentPadding(horizontal = 12f.dp, vertical = 10f.dp)
    hovered {
        background(if (checked) values.colors.accent else values.colors.muted)
        foreground(if (checked) values.colors.foreground else values.colors.mutedForeground)
    }
}

internal fun shadcnRadioGroupStyle(): Style = Style { contentPadding(12f.dp) }

internal fun shadcnRadioLabelStyle(values: UiThemeValues): Style = Style { textSize(values.typography.label) }
