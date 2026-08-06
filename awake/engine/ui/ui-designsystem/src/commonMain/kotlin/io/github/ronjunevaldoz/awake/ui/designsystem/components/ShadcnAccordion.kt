// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/**
 * Real shadcn's `Accordion`: a group of collapsible items where a single item can be open at a time.
 */
fun <T> ColumnScope.shadcnAccordion(
    items: List<T>,
    selectedId: String?,
    onSelectId: (String?) -> Unit,
    idProvider: (T) -> String,
    titleProvider: (T) -> String,
    modifier: UiModifier = Modifier,
    content: ColumnScope.(T) -> Unit
) {
    items.forEach { item ->
        val itemId = idProvider(item)
        val isOpen = selectedId == itemId
        shadcnCollapsible(
            id = itemId,
            title = titleProvider(item),
            expanded = isOpen,
            modifier = modifier,
            onExpandedChange = { open ->
                onSelectId(if (open) itemId else null)
            }
        ) {
            content(item)
        }
    }
}
