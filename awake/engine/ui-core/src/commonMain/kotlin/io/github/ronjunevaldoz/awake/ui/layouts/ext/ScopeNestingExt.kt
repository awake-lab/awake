// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.childAbsolute
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.childColumn
import io.github.ronjunevaldoz.awake.ui.childRow
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Opens a nested [ColumnScope] from an already-claimed [slot] without escaping back out to an
 * outer runtime receiver. This keeps nested UI composition on the same [UiScope] receiver and
 * avoids `this@...` call-site noise.
 */
fun UiScope.column(
    slot: UiSlot,
    gap: Float = UiSpacing.sm.toPx(),
    insets: UiInsets = UiInsets.Zero,
    block: ColumnScope.() -> Unit
) {
    childColumn(slot, gap, insets).block()
}

/** [slot]-based nested [RowScope] variant of [column]. */
fun UiScope.row(
    slot: UiSlot,
    gap: Float = UiSpacing.sm.toPx(),
    insets: UiInsets = UiInsets.Zero,
    block: RowScope.() -> Unit
) {
    childRow(slot, gap, insets).block()
}

/** [slot]-based nested [AbsoluteScope] variant of [column]. */
fun UiScope.absolute(
    slot: UiSlot,
    insets: UiInsets = UiInsets.Zero,
    block: AbsoluteScope.() -> Unit
) {
    childAbsolute(slot, insets).block()
}

/** [slot]-based nested [BoxScope] variant of [column]. */
fun UiScope.box(
    slot: UiSlot,
    insets: UiInsets = UiInsets.Zero,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    childBox(slot, insets, contentAlignment).block()
}
