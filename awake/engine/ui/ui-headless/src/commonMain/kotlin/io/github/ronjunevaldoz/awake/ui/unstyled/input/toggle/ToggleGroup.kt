// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.input.toggle

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.weight

/**
 * Row of pressable toggles with independent multi-select state (e.g. bold+italic both active
 * at once). Index-based (not shadcn's string-keyed `Set<String>`) to match this module's
 * existing [toggle]/index convention.
 *
 * Missing vs real shadcn's `ToggleGroup`: that one wraps the row in a single bordered/rounded
 * container with only the outer corners rounded and one shared divider between segments.
 * Attempted and reverted -- painting a filled container underneath makes [toggle]'s own
 * unchecked label (resolved internally from `mutedForeground`, tuned for a transparent
 * background) unreadable, and a caller-supplied `style` does not win over that internal
 * resolution. Doing this properly needs [toggle] to expose its label color independently of
 * its surface resolution first; until then the group stays visually flat but readable.
 */
fun UiScope.toggleGroup(id: String, options: List<String>, selectedIndices: Set<Int>, modifier: UiModifier = Modifier, onSelectedIndicesChange: (Set<Int>) -> Unit = {}) {
    row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(0f.dp)) {
        options.forEachIndexed { index, option ->
            toggle(
                id = "$id.$index",
                checked = index in selectedIndices,
                label = option,
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
fun UiScope.toggleGroup(id: String, options: List<String>, selectedIndex: Int, modifier: UiModifier = Modifier, onIndexChange: (Int) -> Unit = {}) = toggleGroup(
    id = id,
    options = options,
    selectedIndices = setOf(selectedIndex),
    modifier = modifier,
    onSelectedIndicesChange = { indices ->
        (indices - selectedIndex).firstOrNull()?.let(onIndexChange)
    },
)
