// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.theme

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
    content: ColumnScope.(T) -> Unit,
) {
    val shadcnTheme = theme.asShadcnTheme()
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
            },
        ) {
            // Real shadcn's AccordionContent (px-1 pb-4 pt-0), unlike the bare Collapsible this
            // delegates to -- Collapsible's own content is intentionally unpadded (matches
            // Radix), but Accordion's own reference always insets its body under the trigger,
            // and constrains width so long body text wraps instead of overflowing the panel.
            column(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = shadcnTheme.spacing.sm, top = 0f.dp, end = shadcnTheme.spacing.sm, bottom = shadcnTheme.spacing.sm),
            ) {
                content(item)
            }
        }
    }
}
