// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle

import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ext.rawRow

fun UiScope.toggleGroup(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    onIndexChange: (Int) -> Unit = {}
) {
    rawRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(0f.dp)) {
        options.forEachIndexed { index, option ->
            toggle(
                id = "$id.$index",
                checked = selectedIndex == index,
                label = option,
                onCheckedChange = { if (it) onIndexChange(index) }
            )
        }
    }
}
