// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement as PrimitiveArrangement
import io.github.ronjunevaldoz.awake.ui.layouts.box as primitiveBox
import io.github.ronjunevaldoz.awake.ui.layouts.column as primitiveColumn
import io.github.ronjunevaldoz.awake.ui.layouts.row as primitiveRow
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier as primitiveModifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier as PrimitiveModifier
import io.github.ronjunevaldoz.awake.ui.modifier.align as primitiveAlign
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight as primitiveFillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize as primitiveFillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth as primitiveFillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height as primitiveHeight
import io.github.ronjunevaldoz.awake.ui.modifier.padding as primitivePadding
import io.github.ronjunevaldoz.awake.ui.modifier.weight as primitiveWeight
import io.github.ronjunevaldoz.awake.ui.modifier.width as primitiveWidth

/** Compose-style structural modifier for generic Headless layout behavior. */
interface Modifier {
    companion object : Modifier
}

private data class HeadlessModifier(val primitive: PrimitiveModifier) : Modifier

internal fun Modifier.asPrimitiveModifier(): PrimitiveModifier =
    (this as? HeadlessModifier)?.primitive ?: primitiveModifier

fun Modifier.width(width: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveWidth(width))

fun Modifier.width(width: Dimension): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveWidth(width))

fun Modifier.height(height: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveHeight(height))

fun Modifier.height(height: Dimension): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveHeight(height))

fun Modifier.fillMaxWidth(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxWidth())

fun Modifier.fillMaxHeight(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxHeight())

fun Modifier.fillMaxSize(): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveFillMaxSize())

fun Modifier.padding(all: Dp): Modifier = HeadlessModifier(asPrimitiveModifier().primitivePadding(all))

fun Modifier.padding(horizontal: Dp, vertical: Dp): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitivePadding(horizontal, vertical))

fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier =
    HeadlessModifier(asPrimitiveModifier().primitiveWeight(weight, fill))

fun Modifier.align(alignment: UiAlignment): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveAlign(alignment))

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

fun UiScope.box(
    modifier: Modifier = Modifier,
    contentAlignment: UiAlignment = UiAlignment.TopStart,
    content: BoxScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveBox(
    modifier = modifier.asPrimitiveModifier(),
    contentAlignment = contentAlignment,
) { slot -> content(asHeadlessScope(), slot) }
