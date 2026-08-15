// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnFieldLabelStyle(values: UiThemeValues, disabled: Boolean): Style = Style {
    foreground(if (disabled) values.colors.mutedForeground else values.colors.foreground)
    textSize(values.typography.label)
}
internal fun shadcnFieldDescriptionStyle(values: UiThemeValues): Style = Style { foreground(values.colors.mutedForeground); textSize(values.typography.caption) }
internal fun shadcnFieldErrorStyle(values: UiThemeValues): Style = Style { foreground(values.colors.destructive); textSize(values.typography.caption) }
internal fun shadcnFieldSeparatorStyle(values: UiThemeValues): Style = Style { background(values.colors.border) }
