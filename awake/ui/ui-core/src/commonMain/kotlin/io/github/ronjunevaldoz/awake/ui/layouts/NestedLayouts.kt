// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

/**
 * Nested layout entry points from an existing [UiPrimitiveScope].
 *
 * These keep nested authored code on the current receiver and avoid escaping back to an outer
 * runtime or needing `this@column` noise at call sites.
 */
fun UiPrimitiveScope.column(
    slot: UiBounds,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    block: ColumnScope.() -> Unit,
) {
    childColumn(
        slot = slot,
        verticalArrangement = verticalArrangement,
        modifier = modifier,
    ).block()
}

fun UiPrimitiveScope.row(
    slot: UiBounds,
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = Modifier,
    block: RowScope.() -> Unit,
) {
    childRow(
        slot = slot,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
    ).block()
}

fun UiPrimitiveScope.absolute(
    slot: UiBounds,
    modifier: UiModifier = Modifier,
    block: AbsoluteScope.() -> Unit,
) {
    childAbsolute(slot, modifier).block()
}

fun UiPrimitiveScope.box(
    slot: UiBounds,
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit,
) {
    childBox(slot, modifier, contentAlignment).block()
}
