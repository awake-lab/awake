// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.input.InputSnapshot

/** Builds a one-off [InputSnapshot] for a test frame -- [Input] is a per-session instance
 * now (no longer a global object), so tests construct their own throwaway one instead of
 * writing into shared static state. */
fun testSnapshot(x: Float = -100f, y: Float = -100f, down: Boolean = false, scrollDeltaY: Float = 0f): InputSnapshot {
    val input = Input()
    input.setPointer(down, x, y)
    input.scrollDeltaY = scrollDeltaY
    return input.updateSnapshot()
}
