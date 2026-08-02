// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.spinner
import io.github.ronjunevaldoz.awake.ui.style.*

private fun shadcnSpinnerStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style { foreground(shadcnTheme.colors.mutedForeground) } then style
}

/** Real shadcn's `Spinner`: a muted-foreground loading indicator. Delegates entirely to
 * [spinner]. */
fun UiScope.shadcnSpinner(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Unit = spinner(id = id, modifier = modifier, style = shadcnSpinnerStyle(theme, style))
