// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.resolveRootSlot
import io.github.ronjunevaldoz.awake.ui.layouts.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.baseSpacingPx
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement

/**
 * Root layout entry points from a [UiContext].
 *
 * This is the authored "start a page/frame here" surface. Public authoring should be
 * modifier-first; raw [UiSlot] overloads remain only as compatibility bridges while older
 * tests, previews, and helpers are migrated.
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
    slot: UiSlot,
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

fun UiContext.row(
    modifier: UiModifier = UiModifier(),
    horizontalArrangement: Arrangement = defaultArrangement(),
    block: RowScope.() -> Unit
) {
    createRow(
        slot = resolveRootSlot(modifier),
        gap = horizontalArrangement.baseSpacingPx(),
        horizontalArrangement = horizontalArrangement,
        testTag = modifier.testTag
    ).block()
}

@Deprecated(
    message = "Use row(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("row(modifier = modifier, horizontalArrangement = horizontalArrangement, block = block)")
)
fun UiContext.row(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    horizontalArrangement: Arrangement = defaultArrangement(),
    block: RowScope.() -> Unit
) {
    createRow(
        slot = slot,
        gap = horizontalArrangement.baseSpacingPx(),
        insets = modifier.insets,
        horizontalArrangement = horizontalArrangement,
        testTag = modifier.testTag
    ).block()
}

fun UiContext.box(
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    createBox(
        slot = resolveRootSlot(modifier),
        contentAlignment = contentAlignment,
        testTag = modifier.testTag
    ).block()
}

@Deprecated(
    message = "Use box(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("box(modifier = modifier, contentAlignment = contentAlignment, block = block)")
)
fun UiContext.box(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    createBox(
        slot = slot,
        insets = modifier.insets,
        contentAlignment = contentAlignment,
        testTag = modifier.testTag
    ).block()
}

fun UiContext.absolute(
    modifier: UiModifier = UiModifier(),
    block: AbsoluteScope.() -> Unit
) {
    createAbsolute(
        slot = resolveRootSlot(
            modifier = modifier,
            defaultWidth = Dimension.Fixed(0.dp),
            defaultHeight = Dimension.Fixed(0.dp)
        ),
        testTag = modifier.testTag
    ).block()
}

@Deprecated(
    message = "Use absolute(modifier = ...) so authored root layout comes from UiModifier, not UiSlot geometry.",
    replaceWith = ReplaceWith("absolute(modifier = modifier, block = block)")
)
fun UiContext.absolute(
    slot: UiSlot,
    modifier: UiModifier = UiModifier(),
    block: AbsoluteScope.() -> Unit
) {
    createAbsolute(
        slot = slot,
        insets = modifier.insets,
        testTag = modifier.testTag
    ).block()
}
