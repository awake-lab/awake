// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnAvatarStyle(values: UiThemeValues): Style = Style {
    background(values.colors.muted)
    foreground(values.colors.foreground)
    shape(values.shapes.full)
}

internal fun shadcnAvatarBadgeStyle(values: UiThemeValues, color: Color? = null): Style = Style {
    background(color ?: values.colors.primary)
    shape(values.shapes.full)
}
