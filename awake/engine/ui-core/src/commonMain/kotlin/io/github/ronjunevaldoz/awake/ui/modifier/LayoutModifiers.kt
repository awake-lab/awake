// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets

fun UiModifier.width(dp: Dp): UiModifier = copy(widthDimension = Dimension.Fixed(dp))
fun UiModifier.height(dp: Dp): UiModifier = copy(heightDimension = Dimension.Fixed(dp))
fun UiModifier.width(dimension: Dimension): UiModifier = copy(widthDimension = dimension)
fun UiModifier.height(dimension: Dimension): UiModifier = copy(heightDimension = dimension)
fun UiModifier.size(width: Dp, height: Dp): UiModifier =
    copy(widthDimension = Dimension.Fixed(width), heightDimension = Dimension.Fixed(height))

/** Fills in [fallbackWidth]/[fallbackHeight] only where this modifier didn't already
 * specify a size -- used by widgets to express their own natural default size while still
 * letting an authored modifier override it. */
fun UiModifier.withSizeFallback(fallbackWidth: Dimension, fallbackHeight: Dimension): UiModifier =
    width(widthDimension ?: fallbackWidth).height(heightDimension ?: fallbackHeight)

fun UiModifier.fillMaxWidth(): UiModifier = copy(widthDimension = Dimension.FillMax)
fun UiModifier.fillMaxHeight(): UiModifier = copy(heightDimension = Dimension.FillMax)
fun UiModifier.fillMaxSize(): UiModifier =
    copy(widthDimension = Dimension.FillMax, heightDimension = Dimension.FillMax)

fun UiModifier.align(alignment: UiAlignment): UiModifier = copy(alignment = alignment)
fun UiModifier.offset(x: Dp = UiShape.none, y: Dp = UiShape.none): UiModifier =
    copy(offsetX = x, offsetY = y)

fun UiModifier.padding(all: Dp): UiModifier = copy(insets = UiInsets(all))
fun UiModifier.paddingTop(top: Dp): UiModifier = padding(0.dp, top, 0.dp, 0.dp)
fun UiModifier.paddingBottom(bottom: Dp): UiModifier = padding(0.dp, 0.dp, 0.dp, bottom)
fun UiModifier.paddingStart(start: Dp): UiModifier = padding(start, 0.dp, 0.dp, 0.dp)
fun UiModifier.paddingEnd(end: Dp): UiModifier = padding(0.dp, 0.dp, end, 0.dp)
fun UiModifier.padding(horizontal: Dp, vertical: Dp): UiModifier = copy(
    insets = UiInsets(
        horizontal,
        vertical
    )
)

fun UiModifier.padding(start: Dp, top: Dp, end: Dp, bottom: Dp): UiModifier =
    copy(insets = UiInsets(start, top, end, bottom))
