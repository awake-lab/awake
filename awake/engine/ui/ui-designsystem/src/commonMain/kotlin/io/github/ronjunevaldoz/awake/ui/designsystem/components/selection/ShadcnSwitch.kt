// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch
import io.github.ronjunevaldoz.awake.ui.style.*

private fun shadcnSwitchFieldStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.field(theme.asShadcnTheme()) then style

/** Real shadcn's `Switch`: an on/off track-and-thumb toggle, themed via the shared field
 * style. Delegates entirely to [switch]. */
fun UiScope.shadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = shadcnSwitchFieldStyle(theme, style)
)
