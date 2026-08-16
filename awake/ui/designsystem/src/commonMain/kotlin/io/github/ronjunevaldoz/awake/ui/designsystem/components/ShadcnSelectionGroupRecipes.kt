// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnRadioMetrics
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioGroupStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnToggleGroupItemStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.radio
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.toggleGroup

fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: Modifier = Modifier,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) {
    toggleGroup(
        id = id,
        options = options,
        selectedIndices = selectedIndices,
        modifier = modifier,
        style = shadcnToggleGroupItemStyle(themeValues),
        onSelectedIndicesChange = onSelectedIndicesChange,
    )
}

fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onIndexChange: (Int) -> Unit = {},
) {
    toggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = modifier,
        style = shadcnToggleGroupItemStyle(themeValues),
        onIndexChange = onIndexChange,
    )
}

fun UiScope.shadcnRadioButton(
    id: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Boolean = radio(
    id = id,
    selected = selected,
    modifier = modifier,
    enabled = enabled,
    style = shadcnRadioStyle(themeValues),
    onClick = onClick,
)

fun UiScope.shadcnRadioGroup(
    id: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: ColumnScope.() -> Unit,
) {
    surface(
        id = id,
        modifier = modifier,
        style = shadcnRadioGroupStyle(),
        verticalArrangement = Arrangement.spacedBy(ShadcnRadioMetrics.groupGap),
        content = { content() },
    )
}

fun UiScope.shadcnRadioGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gap: io.github.ronjunevaldoz.awake.ui.api.Dp = ShadcnRadioMetrics.groupGap,
    onIndexChange: (Int) -> Unit = {},
): Int {
    var resolved = selectedIndex
    // The group owns its own column, like upstream's RadioGroup (`grid gap-3`). Emitting rows
    // straight into the caller's scope made the layout depend on that scope being a column --
    // anywhere else every option stacked at (0,0), only the first ever hit-tested, and the group
    // read as "not clickable". ShadcnRadioGroupClickProbeTest pins the bare-scope case.
    column(verticalArrangement = Arrangement.spacedBy(gap)) {
        options.forEachIndexed { index, label ->
            val wasSelected = index == selectedIndex
            var next = wasSelected
            row(
                horizontalArrangement = Arrangement.spacedBy(ShadcnRadioMetrics.labelGap),
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                next = radio(
                    id = "$id.$index",
                    selected = wasSelected,
                    modifier = Modifier.height(ShadcnRadioMetrics.itemSize),
                    enabled = enabled,
                    style = shadcnRadioStyle(themeValues),
                    onClick = {
                        resolved = index
                        onIndexChange(index)
                    },
                )
                text(
                    label = label,
                    style = shadcnRadioLabelStyle(themeValues),
                    semanticId = "$id.$index.label",
                )
            }
            if (next != wasSelected && next && !wasSelected) resolved = index
        }
    }
    return resolved
}
