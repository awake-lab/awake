// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.selection

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggleGroup

private val ToggleGroupItemHeight = 40f.dp

/** Real shadcn's `ToggleGroup` multi-select form: toggles in [selectedIndices] can be active
 * simultaneously (e.g. bold+italic both pressed). */
fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: UiModifier = Modifier,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) = shadcnToggleGroupContainer(id, modifier) { itemStyle, itemModifier ->
    toggleGroup(
        id = id,
        options = options,
        selectedIndices = selectedIndices,
        modifier = itemModifier,
        itemStyle = itemStyle,
        onSelectedIndicesChange = onSelectedIndicesChange,
    )
}

/** Real shadcn's `ToggleGroup` single-select convenience form: a row of mutually exclusive
 * [shadcnToggle]-style buttons. */
fun UiScope.shadcnToggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    onIndexChange: (Int) -> Unit = {},
) = shadcnToggleGroupContainer(id, modifier) { itemStyle, itemModifier ->
    toggleGroup(
        id = id,
        options = options,
        selectedIndex = selectedIndex,
        modifier = itemModifier,
        itemStyle = itemStyle,
        onIndexChange = onIndexChange,
    )
}

/**
 * The bordered, rounded shell shadcn draws around a toggle group, plus the segment style that
 * keeps labels readable on it.
 *
 * The segment retint is the part that matters: [toggleGroup]'s unchecked default foreground is
 * `mutedForeground`, chosen against a transparent background. On this container it reads as
 * nearly invisible, so the group supplies its own foreground -- caller style wins over a
 * widget's defaults (`resolveStyle` composes `defaults then style`).
 */
private inline fun UiScope.shadcnToggleGroupContainer(
    id: String,
    modifier: UiModifier,
    crossinline body: UiScope.(itemStyle: Style, itemModifier: UiModifier) -> Unit,
) {
    val shadcnResolvedTheme = theme.asShadcnTheme()
    val colors = shadcnResolvedTheme.colors
    shadcnSurface(
        id = "$id.container",
        modifier = modifier,
        style = Style {
            // Zero padding: the segments run edge to edge, and the container's own rounding
            // clips their outer corners.
            contentPadding(0f.dp)
            borderWidth(1f.dp)
            borderColor(colors.border)
            // Real shadcn's ToggleGroup container is rounded-md, drawn from the active theme's
            // own radius scale rather than ui-core's disconnected `UiShape` global.
            shape(shadcnResolvedTheme.radii.md)
        },
    ) {
        // A wrap-height surface measures its content in a deliberately unbounded trial slot.
        // Passing fillMaxHeight() through in that case makes every segment report the trial
        // sentinel (100,000px) as its intrinsic height, so the real group receives the same
        // giant hit rectangle. An explicitly sized group is bounded and still needs its
        // segments to stretch to that caller-provided height.
        val itemModifier = if (modifier.heightDimension != null) {
            Modifier.fillMaxWidth().fillMaxHeight()
        } else {
            Modifier.fillMaxWidth().height(ToggleGroupItemHeight)
        }
        body(
            Style {
                foreground(colors.foreground)
                hovered { foreground(colors.foreground) }
            },
            itemModifier,
        )
    }
}
