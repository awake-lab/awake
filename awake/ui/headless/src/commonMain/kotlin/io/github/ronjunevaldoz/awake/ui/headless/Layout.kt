// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement as PrimitiveArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box as primitiveBox
import io.github.ronjunevaldoz.awake.ui.layouts.column as primitiveColumn
import io.github.ronjunevaldoz.awake.ui.layouts.row as primitiveRow
import io.github.ronjunevaldoz.awake.ui.layouts.spacer as primitiveSpacer

/** Sizes a node to its measured content instead of the parent cross-axis extent. */
fun UiModifier.wrapContentWidth(): UiModifier = width(Dimension.WrapContent)

/** Sizes a node to its measured content only when the caller did not provide an explicit width. */
fun UiModifier.wrapContentWidthOrDefault(): UiModifier =
    if (widthDimension == null) wrapContentWidth() else this

/** Applies a component's intrinsic height only when the caller did not provide one. */
fun UiModifier.heightOrDefault(height: Dp): UiModifier =
    if (heightDimension == null) height(height) else this

/**
 * Fills the parent width only when the caller did not size the node itself -- the width
 * counterpart of [heightOrDefault], for block-level recipes whose CSS reference is `w-full`.
 * Without it a container defaults to wrap-content and its children collapse to their own
 * intrinsic width.
 */
fun UiModifier.fillMaxWidthOrDefault(): UiModifier =
    if (widthDimension == null) fillMaxWidth() else this

fun UiModifier.margin(
    start: Dp = Dp(0f),
    top: Dp = Dp(0f),
    end: Dp = Dp(0f),
    bottom: Dp = Dp(0f),
): UiModifier = offset(start, top) // Margin not yet in Core, approximating with offset

fun UiModifier.size(size: Dp): UiModifier = width(size).height(size)

fun ColumnScope.spacer(modifier: UiModifier = Modifier): Unit =
    primitive.primitiveSpacer(modifier)

fun RowScope.spacer(modifier: UiModifier = Modifier): Unit =
    primitive.primitiveSpacer(modifier)

fun UiScope.spacer(modifier: UiModifier = Modifier) {
    column(modifier = modifier) {}
}


/** Main-axis distribution for Headless [row] and [column] containers. */
sealed interface Arrangement {
    data object Start : Arrangement
    data object Center : Arrangement
    data object End : Arrangement
    data object SpaceBetween : Arrangement
    data object SpaceEvenly : Arrangement
    data object SpaceAround : Arrangement
    data class SpacedBy(val space: Dp) : Arrangement

    companion object {
        fun spacedBy(space: Dp): Arrangement = SpacedBy(space)
    }
}

internal fun Arrangement.asPrimitiveArrangement(): PrimitiveArrangement = when (this) {
    Arrangement.Start -> PrimitiveArrangement.Start
    Arrangement.Center -> PrimitiveArrangement.Center
    Arrangement.End -> PrimitiveArrangement.End
    Arrangement.SpaceBetween -> PrimitiveArrangement.SpaceBetween
    Arrangement.SpaceEvenly -> PrimitiveArrangement.SpaceEvenly
    Arrangement.SpaceAround -> PrimitiveArrangement.SpaceAround
    is Arrangement.SpacedBy -> PrimitiveArrangement.spacedBy(space)
}

fun UiScope.column(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun UiScope.column(
    id: String?,
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    // Opt-in cross-frame trial cache -- see UiPrimitiveScope.column()'s doc comment. Supply a
    // key that changes whenever this content's structure/size inputs change.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    id = id,
    modifier = modifier,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
    cacheKey = cacheKey,
) { slot -> content(asHeadlessScope(), slot) }

fun UiScope.row(
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    // Opt-in cross-frame trial cache -- see UiScope.column.
    id: String? = null,
    cacheKey: Any? = null,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier,
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
    id = id,
    cacheKey = cacheKey,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.column(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.column(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.column(
    modifier: UiModifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.row(
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier,
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.row(
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier,
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.row(
    modifier: UiModifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier,
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun UiScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier,
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier,
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier,
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.box(
    modifier: UiModifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier,
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }
