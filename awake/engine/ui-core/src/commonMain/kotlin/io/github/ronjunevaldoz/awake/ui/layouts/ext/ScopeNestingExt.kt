// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * Opens a nested [ColumnScope] from an already-claimed [slot] without escaping back out to an
 * outer runtime receiver. This keeps nested UI composition on the same [UiScope] receiver and
 * avoids `this@...` call-site noise.
 */
fun UiScope.column(
    slot: UiSlot,
    verticalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    block: ColumnScope.() -> Unit
) {
    childColumn(
        slot = slot,
        gap = verticalArrangement.baseSpacingPx(),
        verticalArrangement = verticalArrangement,
        insets = modifier.insets,
        testTag = modifier.testTag
    ).block()
}

/** [slot]-based nested [RowScope] variant of [column]. */
fun UiScope.row(
    slot: UiSlot,
    horizontalArrangement: Arrangement = defaultArrangement(),
    modifier: UiModifier = UiModifier(),
    block: RowScope.() -> Unit
) {
    childRow(
        slot = slot,
        gap = horizontalArrangement.baseSpacingPx(),
        horizontalArrangement = horizontalArrangement,
        insets = modifier.insets,
        testTag = modifier.testTag
    ).block()
}

/** [slot]-based nested [AbsoluteScope] variant of [column]. */
fun UiScope.absolute(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    block: AbsoluteScope.() -> Unit
) {
    childAbsolute(slot, modifier.insets, testTag = modifier.testTag).block()
}

/** [slot]-based nested [BoxScope] variant of [column]. */
fun UiScope.box(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    childBox(slot, modifier.insets, contentAlignment, testTag = modifier.testTag).block()
}
