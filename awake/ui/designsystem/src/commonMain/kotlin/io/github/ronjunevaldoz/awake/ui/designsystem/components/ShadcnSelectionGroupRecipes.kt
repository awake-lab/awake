// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter", "MatchingDeclarationName") // ShadcnRadioMetrics is a
// supporting type; the file's primary export is the shadcnRadio*/shadcnToggleGroup recipes below

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioGroupStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnRadioStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnToggleGroupItemStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.radio
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.toggleGroup

/**
 * Measurements from `new-york-v4/ui/radio-group.tsx` in the pinned shadcn-ui checkout.
 *
 * `RadioGroupItem` is `size-4`; its indicator icon is `size-2`; an authored label row is
 * `flex items-center gap-2`; and the group root is `grid gap-3`. Keep these named so a recipe
 * cannot silently turn a radio into a checkbox-shaped 24dp control again.
 */
@Suppress("MatchingDeclarationName") // supporting type; file's primary export is the recipes below
internal object ShadcnRadioMetrics {
    val itemSize: Dp = 16f.dp
    val indicatorSize: Dp = 8f.dp
    val labelGap: Dp = 8f.dp
    val groupGap: Dp = 12f.dp
}

fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
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
