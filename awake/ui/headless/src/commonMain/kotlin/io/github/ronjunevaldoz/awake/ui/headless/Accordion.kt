// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

enum class AccordionType { Single, Multiple }

/** Accordion group context managing active open collapsible keys. */
class AccordionScope(
    val openKeys: Set<String>,
    val onToggleKey: (String) -> Unit,
    private val scope: UiScope,
) : UiScope by scope {
    fun collapsibleItem(
        key: String,
        title: String,
        modifier: Modifier = Modifier,
        content: ColumnScope.() -> Unit,
    ): Boolean {
        val isExpanded = openKeys.contains(key)
        return collapsible(
            id = key,
            expanded = isExpanded,
            onExpandedChange = { onToggleKey(key) },
            modifier = modifier,
            trigger = { _, _ ->
                button(id = "$key.trigger", label = title, modifier = Modifier.fillMaxWidth())
            },
            content = content,
        )
    }
}

/**
 * Unstyled Accordion primitive supporting Single-open or Multiple-open selection state across grouped collapsibles.
 */
fun UiScope.accordion(
    id: String,
    type: AccordionType = AccordionType.Single,
    openKeys: Set<String>,
    onOpenKeysChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    content: AccordionScope.() -> Unit,
): UiBounds = column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4f.dp),
) {
    val accordionScope = AccordionScope(
        openKeys = openKeys,
        onToggleKey = { key ->
            val nextKeys = when (type) {
                AccordionType.Single -> if (openKeys.contains(key)) emptySet() else setOf(key)
                AccordionType.Multiple -> if (openKeys.contains(key)) openKeys - key else openKeys + key
            }
            onOpenKeysChange(nextKeys)
        },
        scope = this,
    )
    accordionScope.content()
}
