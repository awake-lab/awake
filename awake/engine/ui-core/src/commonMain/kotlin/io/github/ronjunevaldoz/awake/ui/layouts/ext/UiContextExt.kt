// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Convenience extension for starting a root column directly from a [UiContext].
 */
fun UiContext.column(
    x: Float,
    y: Float,
    width: Float,
    gap: Float = UiSpacing.sm.toPx(),
    block: ColumnScope.() -> Unit
) {
    createColumn(
        x = x,
        y = y,
        width = width,
        gap = gap
    ).block()
}
