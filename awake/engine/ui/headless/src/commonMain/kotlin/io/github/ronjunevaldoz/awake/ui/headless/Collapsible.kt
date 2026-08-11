// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.unstyled.components.collapsible as primitiveCollapsible

/** Generic disclosure behavior. The trigger owns its visual and click affordance. */
fun ColumnScope.collapsible(
    id: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(isOpen: Boolean, toggle: () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean = primitive.primitiveCollapsible(
    id = id,
    expanded = expanded,
    modifier = modifier.asPrimitiveModifier(),
    onExpandedChange = onExpandedChange,
    trigger = { open, toggle -> trigger(asHeadlessScope(), open, toggle) },
    content = { content(asHeadlessScope()) },
)
