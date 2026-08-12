// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

/** API-owned neutral tab identity; visual treatment belongs to the design system. */
typealias UiTabItem = io.github.ronjunevaldoz.awake.ui.api.UiTabItem

/** Selection-only tab behavior; callers provide the tab trigger visuals. */
fun ColumnScope.tabs(
    id: String,
    items: List<UiTabItem>,
    selected: String,
    modifier: Modifier = Modifier,
    content: ColumnScope.(item: UiTabItem, selected: Boolean, onSelect: () -> Unit) -> Unit,
): String {
    var resolved = selected
    items.forEach { item ->
        content(item, item.value == selected) {
            resolved = item.value
        }
    }
    return resolved
}
