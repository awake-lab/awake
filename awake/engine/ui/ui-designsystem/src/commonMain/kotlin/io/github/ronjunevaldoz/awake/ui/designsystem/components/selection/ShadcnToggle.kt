// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.style.*

/** Real shadcn's `Toggle`: a single pressable on/off button (distinct from [shadcnSwitch]'s
 * track-and-thumb look). Delegates entirely to [toggle]. */
fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {}
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = style,
    enabled = enabled,
    onCheckedChange = onCheckedChange
)
