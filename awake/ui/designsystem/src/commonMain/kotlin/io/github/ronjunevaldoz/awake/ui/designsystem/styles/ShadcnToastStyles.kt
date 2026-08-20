// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnToastStyle(values: UiThemeValues): Style = Style {
    background(values.colors.card)
    foreground(values.colors.cardForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.lg)
    // toast()'s message text sits in `contentSlot`, which insets by this -- without it the
    // message rendered flush against the toast's own border, the ui-headless default this used
    // to fall back on (`theme.components.surface`, now deleted) was the only thing supplying it.
    contentPadding(8f.dp)
}
