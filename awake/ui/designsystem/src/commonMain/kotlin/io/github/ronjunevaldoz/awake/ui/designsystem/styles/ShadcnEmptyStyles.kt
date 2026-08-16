// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnEmptyTitleStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.foreground)
    textSize(values.typography.title)
}

internal fun shadcnEmptyDescriptionStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
    textSize(values.typography.body)
}
