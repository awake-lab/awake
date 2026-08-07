// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.toast
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

private fun shadcnToastStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style {
        shape(UiShape.md)
        borderWidth(1f.dp)
    } then shadcnTheme.components.surface then style
}

/** Real shadcn's `Toast` (Sonner): a self-dismissing message, no Radix primitive underneath it
 * (Radix has no Toast) -- delegates entirely to [toast]. */
fun UiScope.shadcnToast(
    id: String,
    message: String,
    modifier: UiModifier = Modifier,
    durationMs: Float = 3000f,
    style: Style = Style.Empty,
): Boolean = toast(
    id = id,
    message = message,
    modifier = modifier,
    durationMs = durationMs,
    style = shadcnToastStyle(theme, style),
)
