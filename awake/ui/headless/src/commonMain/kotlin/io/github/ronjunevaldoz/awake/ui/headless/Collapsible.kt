// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.collapsible as primitiveCollapsible

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

/**
 * [UiScope] overload — wraps into a [column] to obtain a [ColumnScope] for the disclosure
 * primitive. Use when the call site is not already inside a column layout.
 */
fun UiScope.collapsible(
    id: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(isOpen: Boolean, toggle: () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean {
    var resolved = expanded
    column(modifier = modifier) {
        resolved = collapsible(
            id = id,
            expanded = expanded,
            modifier = Modifier,
            onExpandedChange = onExpandedChange,
            trigger = trigger,
            content = content,
        )
    }
    return resolved
}
