// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCheckboxStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSwitchStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnToggleStyle
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.checkbox
import io.github.ronjunevaldoz.awake.ui.headless.switch
import io.github.ronjunevaldoz.awake.ui.headless.toggle

fun UiScope.shadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    boxSize = 16f.dp,
    indeterminate = indeterminate,
    enabled = enabled,
    style = shadcnCheckboxStyle(themeValues, checked || indeterminate),
)

fun UiScope.shadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    enabled = enabled,
    style = shadcnSwitchStyle(themeValues, checked),
)

fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
    label: String? = null,
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    enabled = enabled,
    onCheckedChange = onCheckedChange,
    style = shadcnToggleStyle(themeValues, checked),
)
