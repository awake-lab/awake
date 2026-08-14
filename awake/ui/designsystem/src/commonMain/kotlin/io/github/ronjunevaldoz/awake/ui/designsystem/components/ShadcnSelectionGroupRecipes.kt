// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components


import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnRadioMetrics
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.radio
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.spacer
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
        visuals = SurfaceVisuals(
            rest = SurfaceStyle(
                background = themeValues.colors.card,
                foreground = themeValues.colors.foreground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                cornerRadius = themeValues.shapes.md,
            ),
        ),
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
        visuals = SurfaceVisuals(
            rest = SurfaceStyle(
                background = themeValues.colors.card,
                foreground = themeValues.colors.foreground,
                border = SurfaceBorder(1f.dp, themeValues.colors.border),
                cornerRadius = themeValues.shapes.md,
            ),
        ),
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
    visuals = SurfaceStyle(
        background = themeValues.colors.background,
        foreground = themeValues.colors.primary,
        border = SurfaceBorder(1f.dp, themeValues.colors.border),
        cornerRadius = themeValues.shapes.full,
    ),
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
        style = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(12f.dp)),
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
                    visuals = SurfaceStyle(
                        background = themeValues.colors.background,
                        foreground = themeValues.colors.primary,
                        border = SurfaceBorder(1f.dp, themeValues.colors.border),
                        cornerRadius = themeValues.shapes.full,
                    ),
                    onClick = {
                        resolved = index
                        onIndexChange(index)
                    },
                )
                text(
                    label = label,
                    visuals = SurfaceStyle(textSize = themeValues.typography.label),
                    semanticId = "$id.$index.label",
                )
            }
            if (next != wasSelected && next && !wasSelected) resolved = index
        }
    }
    return resolved
}
