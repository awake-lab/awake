// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.layouts.ext

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
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
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * Root layout entry points from a [UiContext].
 *
 * This is the authored "start a page/frame here" surface. Public authoring should be
 * modifier-first; raw [UiSlot] overloads remain only as compatibility bridges while older
 * tests, previews, and helpers are migrated.
 */
fun UiContext.column(
    modifier: UiModifier = Modifier,
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

fun UiContext.row(
    modifier: UiModifier = Modifier,
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

fun UiContext.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    block: BoxScope.() -> Unit
) {
    createBox(
        slot = resolveRootSlot(modifier),
        contentAlignment = contentAlignment,
        testTag = modifier.testTag
    ).block()
}

fun UiContext.absolute(
    modifier: UiModifier = Modifier,
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

