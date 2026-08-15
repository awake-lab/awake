// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

fun shadcnToastStyle(values: UiThemeValues): Style = Style {
    background(values.colors.card)
    foreground(values.colors.cardForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.lg)
}
