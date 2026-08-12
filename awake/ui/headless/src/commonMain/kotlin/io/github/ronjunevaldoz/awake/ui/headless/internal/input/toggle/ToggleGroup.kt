// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.input.toggle

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Row of pressable toggles with independent multi-select state (e.g. bold+italic both active
 * at once). Index-based (not shadcn's string-keyed `Set<String>`) to match this module's
 * existing [toggle]/index convention.
 *
 * Stays visually flat: the bordered container is shadcn's look, so it lives in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup].
 * [itemStyle] is how that wrapper retints the segments to stay readable on its background.
 */
fun UiPrimitiveScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndices: Set<Int>,
    modifier: UiModifier = Modifier,
    itemStyle: Style = Style.Empty,
    onSelectedIndicesChange: (Set<Int>) -> Unit = {},
) {
    // A wrap-height group still has a concrete shadcn toggle height. Resolve that default before
    // the weighted child trial; otherwise fillMaxHeight() segments inherit the 4096px measurement
    // sentinel used for unconstrained row content.
    val resolvedModifier = if (modifier.heightDimension == null) modifier.height(40f.dp) else modifier
    row(modifier = resolvedModifier, horizontalArrangement = Arrangement.spacedBy(0f.dp)) {
        options.forEachIndexed { index, option ->
            toggle(
                id = "$id.$index",
                checked = index in selectedIndices,
                label = option,
                style = itemStyle,
                // toggle()'s own default height (40dp) doesn't stretch to the row's actual
                // height on its own -- a caller-supplied shorter row (e.g. 32dp) left every
                // unchecked segment silently overflowing by the same amount, invisible only
                // because its background is transparent; the checked segment's background
                // made the overflow visible as a box floating above the row. fillMaxHeight()
                // makes every segment match the row's real height instead of its own fallback.
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onCheckedChange = { checked ->
                    onSelectedIndicesChange(
                        if (checked) selectedIndices + index else selectedIndices - index,
                    )
                },
            )
        }
    }
}

/** Single-select convenience wrapper over the multi-select [toggleGroup] above; clicking the
 * already-selected option is a no-op (mirrors [io.github.ronjunevaldoz.awake.ui.headless.input.selection.radioGroup]). */
fun UiPrimitiveScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    itemStyle: Style = Style.Empty,
    onIndexChange: (Int) -> Unit = {},
) = toggleGroup(
    id = id,
    options = options,
    selectedIndices = setOf(selectedIndex),
    modifier = modifier,
    itemStyle = itemStyle,
    onSelectedIndicesChange = { indices ->
        (indices - selectedIndex).firstOrNull()?.let(onIndexChange)
    },
)
