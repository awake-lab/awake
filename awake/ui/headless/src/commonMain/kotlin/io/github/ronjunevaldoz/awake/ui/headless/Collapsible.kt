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
    // Keyed on the disclosure's CURRENT animated height (published by animatedHeight), not on
    // [expanded]: the height animates over several frames while the boolean holds one value, so
    // an expanded-derived key returns stale mid-animation sizes (caught by
    // ShowcaseMeasureCacheConsistencyTest when tried). The animated value is the exact driver of
    // this subtree's measured size -- distinct every animation frame, constant once settled.
    val animatedHeightKey = primitive.widgetState("$id.content").get("currentHeight", -1f)
    column(id = "$id.shell", cacheKey = expanded to animatedHeightKey, modifier = modifier) {
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
