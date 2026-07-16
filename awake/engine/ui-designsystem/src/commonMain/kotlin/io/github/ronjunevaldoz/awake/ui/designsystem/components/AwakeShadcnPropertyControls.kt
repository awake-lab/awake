// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.propertyCheckbox
import io.github.ronjunevaldoz.awake.ui.propertyRow

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    height: Float = 36f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyCheckbox(
    id = id,
    checked = checked,
    label = label,
    height = height,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style
)

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? {
    var resolved: Int? = null
    propertyRow(label = label, height = height, labelWidth = labelWidth) { slot ->
        resolved = awakeShadcnDropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float {
    var resolved = value
    propertyRow(label = label, height = height, labelWidth = labelWidth) { slot ->
        resolved = awakeShadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}
