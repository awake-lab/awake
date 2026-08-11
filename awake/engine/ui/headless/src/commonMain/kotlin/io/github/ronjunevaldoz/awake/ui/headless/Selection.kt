// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.checkbox as primitiveCheckbox
import io.github.ronjunevaldoz.awake.ui.unstyled.input.selection.switch as primitiveSwitch
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle as primitiveToggle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggleGroup as primitiveToggleGroup

fun UiScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    boxSize: Dp,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveCheckbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
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
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveSwitch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.rest.asPrimitiveStyle(),
    enabled = enabled,
)

fun UiScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveToggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    onCheckedChange = onCheckedChange,
)

fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndices = selectedIndices,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = visuals.asPrimitiveStyle(),
        onSelectedIndicesChange = onSelectedIndicesChange,
    )
}

fun RowScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    onIndexChange: (Int) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = visuals.asPrimitiveStyle(),
        onIndexChange = onIndexChange,
    )
}

fun ColumnScope.checkbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    boxSize: Dp,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveCheckbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    boxSize = boxSize,
    indeterminate = indeterminate,
    enabled = enabled,
)

fun ColumnScope.switch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveSwitch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.rest.asPrimitiveStyle(),
    enabled = enabled,
)

fun ColumnScope.toggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = primitive.primitiveToggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    onCheckedChange = onCheckedChange,
)

fun ColumnScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndices = selectedIndices,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = visuals.asPrimitiveStyle(),
        onSelectedIndicesChange = onSelectedIndicesChange,
    )
}

fun ColumnScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    onIndexChange: (Int) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = visuals.asPrimitiveStyle(),
        onIndexChange = onIndexChange,
    )
}

fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    onIndexChange: (Int) -> Unit = {},
) {
    primitive.primitiveToggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = modifier.asPrimitiveModifier(),
        itemStyle = visuals.asPrimitiveStyle(),
        onIndexChange = onIndexChange,
    )
}
