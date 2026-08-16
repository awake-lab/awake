// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnTableStyle(values: ShadcnThemeValues): Style = Style {
    border(1f.dp, values.colors.border)
    shape(values.shapes.md)
    contentPadding(8f.dp)
}
internal fun shadcnTableCellStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.foreground)
    textSize(values.typography.body)
}
internal fun shadcnTableHeaderStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
    textSize(values.typography.label)
}
internal fun shadcnTableCaptionStyle(values: ShadcnThemeValues): Style = Style {
    foreground(values.colors.mutedForeground)
    textSize(values.typography.caption)
}
