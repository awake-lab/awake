// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.checkbox as primitiveCheckbox
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.switch as primitiveSwitch
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toggle as primitiveToggle
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toggleGroup as primitiveToggleGroup
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    boxSize: Dp,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    style: Style = Style.Empty,
): Boolean = primitive.primitiveCheckbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    boxSize = boxSize,
    indeterminate = indeterminate,
    enabled = enabled,
)

fun UiScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: Style = Style.Empty,
): Boolean = primitive.primitiveSwitch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    enabled = enabled,
)


fun UiScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
    style: Style = Style.Empty,
): Boolean = primitive.primitiveToggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    enabled = enabled,
    onCheckedChange = onCheckedChange,
)


fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndices = selectedIndices,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = style,
        onSelectedIndicesChange = onSelectedIndicesChange,
    )
}


fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    onIndexChange: (Int) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = style,
        onIndexChange = onIndexChange,
    )
}
