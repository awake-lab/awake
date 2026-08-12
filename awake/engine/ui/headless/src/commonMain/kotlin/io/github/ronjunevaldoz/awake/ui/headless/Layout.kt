// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement as PrimitiveArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box as primitiveBox
import io.github.ronjunevaldoz.awake.ui.layouts.column as primitiveColumn
import io.github.ronjunevaldoz.awake.ui.layouts.row as primitiveRow
import io.github.ronjunevaldoz.awake.ui.layouts.spacer as primitiveSpacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier as primitiveModifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier as PrimitiveModifier
import io.github.ronjunevaldoz.awake.ui.modifier.align as primitiveAlign
import io.github.ronjunevaldoz.awake.ui.modifier.clickable as primitiveClickable
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight as primitiveFillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize as primitiveFillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth as primitiveFillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height as primitiveHeight
import io.github.ronjunevaldoz.awake.ui.modifier.heightIn as primitiveHeightIn
import io.github.ronjunevaldoz.awake.ui.modifier.offset as primitiveOffset
import io.github.ronjunevaldoz.awake.ui.modifier.padding as primitivePadding
import io.github.ronjunevaldoz.awake.ui.modifier.testTag as primitiveTestTag
import io.github.ronjunevaldoz.awake.ui.modifier.weight as primitiveWeight
import io.github.ronjunevaldoz.awake.ui.modifier.width as primitiveWidth

/** Compose-style structural modifier for generic Headless layout behavior. */
interface Modifier {
    companion object : Modifier
}

internal data class HeadlessModifier(val primitive: PrimitiveModifier) : Modifier

internal fun Modifier.asPrimitiveModifier(): PrimitiveModifier =
    (this as? HeadlessModifier)?.primitive ?: primitiveModifier

fun PrimitiveModifier.toHeadless(): Modifier = HeadlessModifier(this)

fun Modifier.width(width: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveWidth(width))

/** Sizes a node to its measured content instead of the parent cross-axis extent. */
fun Modifier.wrapContentWidth(): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveWidth(io.github.ronjunevaldoz.awake.ui.api.layout.Dimension.WrapContent))

fun Modifier.height(height: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveHeight(height))

/** Applies a component's intrinsic height only when the caller did not provide one. */
fun Modifier.heightOrDefault(height: Dp): Modifier =
    if ((this as? HeadlessModifier)?.primitive?.heightDimension == null) height(height) else this

fun Modifier.size(width: Dp, height: Dp): Modifier = this.width(width).height(height)
fun Modifier.size(size: Dp): Modifier = this.size(size, size)

/** Minimum/maximum height constraint that preserves an explicit caller height when present. */
fun Modifier.heightIn(min: Dp? = null, max: Dp? = null): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveHeightIn(min = min, max = max))

/** Attaches a stable click action to a Headless-owned widget or surface. */
fun Modifier.clickable(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveClickable(enabled = enabled, onClick = onClick))

fun Modifier.fillMaxWidth(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxWidth())

fun Modifier.fillMaxHeight(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxHeight())

fun Modifier.fillMaxSize(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxSize())

fun Modifier.padding(all: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitivePadding(all))

fun Modifier.padding(horizontal: Dp, vertical: Dp): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitivePadding(horizontal, vertical))

fun Modifier.padding(
    start: Dp = 0f.dp,
    top: Dp = 0f.dp,
    end: Dp = 0f.dp,
    bottom: Dp = 0f.dp
): Modifier = HeadlessModifier(
    asPrimitiveModifier().primitivePadding(start, top, end, bottom)
)

fun Modifier.margin(
    start: Dp = 0f.dp,
    top: Dp = 0f.dp,
    end: Dp = 0f.dp,
    bottom: Dp = 0f.dp
): Modifier = HeadlessModifier(
    asPrimitiveModifier().primitiveOffset(start, top) // Margin not yet in Core, approximating with offset
)

fun Modifier.offset(x: Dp = Dp(0f), y: Dp = Dp(0f)): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveOffset(x, y))

fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveWeight(weight, fill))

fun Modifier.align(alignment: UiAlignment): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveAlign(alignment))

/** Stable semantic/test identity for a Headless layout node. */
fun Modifier.testTag(tag: String): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveTestTag(tag))

fun ColumnScope.spacer(modifier: Modifier = Modifier): Unit =
    primitive.primitiveSpacer(modifier.asPrimitiveModifier())

fun RowScope.spacer(modifier: Modifier = Modifier): Unit =
    primitive.primitiveSpacer(modifier.asPrimitiveModifier())

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
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier.asPrimitiveModifier(),
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun UiScope.row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier.asPrimitiveModifier(),
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier.asPrimitiveModifier(),
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier.asPrimitiveModifier(),
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement = Arrangement.Start,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveColumn(
    modifier = modifier.asPrimitiveModifier(),
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    horizontalAlignment = horizontalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier.asPrimitiveModifier(),
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier.asPrimitiveModifier(),
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement = Arrangement.Start,
    verticalAlignment: UiAlignment.Vertical = UiAlignment.Vertical.Top,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveRow(
    modifier = modifier.asPrimitiveModifier(),
    horizontalArrangement = horizontalArrangement.asPrimitiveArrangement(),
    verticalAlignment = verticalAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun UiScope.box(
    modifier: Modifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier.asPrimitiveModifier(),
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun ColumnScope.box(
    modifier: Modifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier.asPrimitiveModifier(),
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun RowScope.box(
    modifier: Modifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier.asPrimitiveModifier(),
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }

fun BoxScope.box(
    modifier: Modifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier.asPrimitiveModifier(),
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }
