// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * Convenience extension for starting a root column directly from a [UiContext].
 */
fun UiContext.column(
    x: Float,
    y: Float,
    width: Float,
    verticalArrangement: Arrangement = defaultArrangement(),
    block: ColumnScope.() -> Unit
) {
    createColumn(
        x = x,
        y = y,
        width = width,
        gap = verticalArrangement.baseSpacingPx(),
        verticalArrangement = verticalArrangement
    ).block()
}
