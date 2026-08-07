// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.input.toggle

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.weight

/**
 * Row of pressable toggles with independent multi-select state (e.g. bold+italic both active
 * at once). Index-based (not shadcn's string-keyed `Set<String>`) to match this module's
 * existing [toggle]/index convention.
 */
fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: UiModifier = Modifier,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {}
) {
    row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(0f.dp)) {
        options.forEachIndexed { index, option ->
            toggle(
                id = "$id.$index",
                checked = index in selectedIndices,
                label = option,
                modifier = Modifier.weight(1f),
                onCheckedChange = { checked ->
                    onSelectedIndicesChange(
                        if (checked) selectedIndices + index else selectedIndices - index
                    )
                }
            )
        }
    }
}

/** Single-select convenience wrapper over the multi-select [toggleGroup] above; clicking the
 * already-selected option is a no-op (mirrors [io.github.ronjunevaldoz.awake.ui.headless.input.selection.radioGroup]). */
fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    onIndexChange: (Int) -> Unit = {}
) = toggleGroup(
    id = id,
    options = options,
    selectedIndices = setOf(selectedIndex),
    modifier = modifier,
    onSelectedIndicesChange = { indices ->
        (indices - selectedIndex).firstOrNull()?.let(onIndexChange)
    }
)
