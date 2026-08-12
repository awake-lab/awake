// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch

private fun shadcnSwitchFieldStyle(theme: UiTheme, style: Style): Style {
    val shadcn = theme.asShadcnTheme()
    return ShadcnStyles.field(shadcn) then Style {
        // shadcn Switch uses the input token for its unchecked track, while the headless
        // primitive supplies the primary token for the checked state.
        background(shadcn.input, tokenId = "input")
    } then style
}

/** Real shadcn's `Switch`: an on/off track-and-thumb toggle, themed via the shared field
 * style. Delegates entirely to [switch]. */
@Deprecated(
    message = "Use the Headless UiScope shadcnSwitch overload; Core receivers are a migration bridge.",
    replaceWith = ReplaceWith("shadcnSwitch(id, checked, label, modifier, enabled)"),
)
fun UiScope.shadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = shadcnSwitchFieldStyle(theme, style),
    enabled = enabled,
)
