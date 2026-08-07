// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.headless.input.selection.checkbox
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.*
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

private fun shadcnCheckboxStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.checkbox(theme.asShadcnTheme()) then style

/** Real shadcn's `Checkbox`: a boxed check control, themed via the shared checkbox style.
 * Delegates entirely to [checkbox]. */
fun UiScope.shadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    indeterminate: Boolean = false,
    style: Style = Style.Empty,
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = shadcnCheckboxStyle(theme, style),
    indeterminate = indeterminate,
)
