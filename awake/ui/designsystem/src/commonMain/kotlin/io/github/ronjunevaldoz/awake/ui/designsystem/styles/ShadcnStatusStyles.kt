// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

fun shadcnProgressStyle(values: UiThemeValues): Style = Style {
    background(values.colors.primary.withAlpha(0.2f))
    foreground(values.colors.primary)
    border(0f.dp, Color.Transparent)
    shape(values.shapes.full)
}

fun shadcnSkeletonStyle(values: UiThemeValues): Style = Style {
    background(values.colors.muted)
    shape(values.shapes.md)
}

fun shadcnSpinnerStyle(values: UiThemeValues): Style = Style {
    foreground(values.colors.primary)
}
