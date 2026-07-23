// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * Nested layout entry points from an existing [UiScope].
 *
 * These keep nested authored code on the current receiver and avoid escaping back to an outer
 * runtime or needing `this@column` noise at call sites.
 */
fun UiScope.column(
    slot: UiSlot,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    block: ColumnScope.() -> Unit
) {
    childColumn(
        slot = slot,
        verticalArrangement = verticalArrangement,
        modifier = modifier
    ).block()
}

fun UiScope.row(
    slot: UiSlot,
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    block: RowScope.() -> Unit
) {
    childRow(
        slot = slot,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ).block()
}

fun UiScope.absolute(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    block: AbsoluteScope.() -> Unit
) {
    childAbsolute(slot, modifier).block()
}

fun UiScope.box(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    childBox(slot, modifier, contentAlignment).block()
}
