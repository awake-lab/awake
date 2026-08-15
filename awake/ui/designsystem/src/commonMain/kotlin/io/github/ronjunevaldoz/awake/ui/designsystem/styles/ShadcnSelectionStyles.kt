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
