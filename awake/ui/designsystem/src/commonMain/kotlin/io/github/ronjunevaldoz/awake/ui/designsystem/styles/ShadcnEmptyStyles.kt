// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnEmptyTitleStyle(values: UiThemeValues): Style = Style {
    foreground(values.colors.foreground)
    textSize(values.typography.title)
}

internal fun shadcnEmptyDescriptionStyle(values: UiThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
    textSize(values.typography.body)
}
