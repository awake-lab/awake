// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.spinner
import io.github.ronjunevaldoz.awake.ui.headless.Modifier as HeadlessModifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope
import io.github.ronjunevaldoz.awake.ui.headless.spinner as headlessSpinner

private fun shadcnSpinnerStyle(theme: UiTheme, style: Style): Style {
    val shadcnTheme = theme.asShadcnTheme()
    return Style { foreground(shadcnTheme.colors.mutedForeground) } then style
}

/** Real shadcn's `Spinner`: a muted-foreground loading indicator. Delegates entirely to
 * [spinner]. */
fun UiScope.shadcnSpinner(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Unit = spinner(id = id, modifier = modifier, style = shadcnSpinnerStyle(theme, style))

fun HeadlessUiScope.shadcnSpinner(
    id: String,
    modifier: HeadlessModifier = HeadlessModifier,
) {
    headlessSpinner(
        id = id,
        modifier = modifier,
        visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
    )
}
