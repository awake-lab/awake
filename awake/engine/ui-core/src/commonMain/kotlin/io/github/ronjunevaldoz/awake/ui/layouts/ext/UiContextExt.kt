// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.resolveRootSlot
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * Convenience extension for starting a root column directly from a [UiContext].
 * Authoring should be modifier-first; [UiSlot] remains an internal/resolved geometry type.
 */
fun UiContext.column(
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    block: ColumnScope.() -> Unit
) {
    createColumn(
        slot = resolveRootSlot(modifier),
        gap = verticalArrangement.baseSpacingPx(),
        verticalArrangement = verticalArrangement,
        testTag = modifier.testTag
    ).block()
}

@Deprecated(
    message = "Use column(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("column(modifier = modifier, verticalArrangement = verticalArrangement, block = block)")
)
fun UiContext.column(
    slot: io.github.ronjunevaldoz.awake.ui.UiSlot,
    modifier: UiModifier = UiModifier(),
    verticalArrangement: Arrangement = defaultArrangement(),
    block: ColumnScope.() -> Unit
) {
    createColumn(
        slot = slot,
        gap = verticalArrangement.baseSpacingPx(),
        insets = modifier.insets,
        verticalArrangement = verticalArrangement,
        testTag = modifier.testTag
    ).block()
}
