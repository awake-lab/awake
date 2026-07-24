// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets

fun UiModifier.width(dp: Dp): UiModifier = copy(width = Dimension.Fixed(dp))
fun UiModifier.height(dp: Dp): UiModifier = copy(height = Dimension.Fixed(dp))
fun UiModifier.width(dimension: Dimension): UiModifier = copy(width = dimension)
fun UiModifier.height(dimension: Dimension): UiModifier = copy(height = dimension)
fun UiModifier.size(width: Dp, height: Dp): UiModifier =
    copy(width = Dimension.Fixed(width), height = Dimension.Fixed(height))

fun UiModifier.fillMaxWidth(): UiModifier = copy(width = Dimension.FillMax)
fun UiModifier.fillMaxHeight(): UiModifier = copy(height = Dimension.FillMax)
fun UiModifier.fillMaxSize(): UiModifier =
    copy(width = Dimension.FillMax, height = Dimension.FillMax)

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
