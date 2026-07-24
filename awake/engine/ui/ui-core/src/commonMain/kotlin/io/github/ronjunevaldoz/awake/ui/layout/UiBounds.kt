// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layout

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot

/**
 * Frozen, public measured-bounds value -- the only rect type allowed to cross the `ui-core`
 * module boundary. `UiSlot` (`ui-core`-internal) is the layout engine's own mutable/measurement
 * type; downstream modules receive/construct [UiBounds] instead. See
 * docs/reference/ui-ownership.md Hard Rule 5 and docs/tasks/2026-07-24-uislot-narrowing.md.
 */
data class UiBounds(val x: Float, val y: Float, val width: Float, val height: Float)

fun UiSlot.toBounds(): UiBounds = UiBounds(x, y, width, height)

fun UiBounds.toSlot(): UiSlot = UiSlot(x, y, width, height)
