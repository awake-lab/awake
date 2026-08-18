// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier as PrimitiveModifier
import io.github.ronjunevaldoz.awake.ui.modifier.clickable as primitiveClickable
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight as primitiveFillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize as primitiveFillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth as primitiveFillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height as primitiveHeight
import io.github.ronjunevaldoz.awake.ui.modifier.offset as primitiveOffset
import io.github.ronjunevaldoz.awake.ui.modifier.padding as primitivePadding
import io.github.ronjunevaldoz.awake.ui.modifier.styleable as primitiveStyleable
import io.github.ronjunevaldoz.awake.ui.modifier.weight as primitiveWeight
import io.github.ronjunevaldoz.awake.ui.modifier.width as primitiveWidth
import io.github.ronjunevaldoz.awake.ui.modifier.widthIn as primitiveWidthIn
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Headless's re-export of Core's `UiModifier` builder surface. Design System and samples must
 * not import `io.github.ronjunevaldoz.awake.ui.modifier` directly (see
 * docs/reference/ui-ownership.md's consumer rule) -- this file is the licensed door through.
 */
typealias UiModifier = io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

val Modifier: UiModifier get() = PrimitiveModifier

fun UiModifier.clickable(enabled: Boolean = true, onClick: () -> Unit): UiModifier =
    primitiveClickable(enabled, onClick)

fun UiModifier.fillMaxHeight(): UiModifier = primitiveFillMaxHeight()
fun UiModifier.fillMaxSize(): UiModifier = primitiveFillMaxSize()
fun UiModifier.fillMaxWidth(): UiModifier = primitiveFillMaxWidth()
fun UiModifier.height(dp: Dp): UiModifier = primitiveHeight(dp)
fun UiModifier.width(dp: Dp): UiModifier = primitiveWidth(dp)
fun UiModifier.widthIn(min: Dp? = null, max: Dp? = null): UiModifier = primitiveWidthIn(min, max)

fun UiModifier.offset(x: Dp = UiShape.none, y: Dp = UiShape.none): UiModifier =
    primitiveOffset(x, y)

fun UiModifier.padding(all: Dp): UiModifier = primitivePadding(all)
fun UiModifier.padding(horizontal: Dp, vertical: Dp): UiModifier = primitivePadding(horizontal, vertical)
fun UiModifier.padding(
    start: Dp = 0f.dp,
    top: Dp = 0f.dp,
    end: Dp = 0f.dp,
    bottom: Dp = 0f.dp,
): UiModifier = primitivePadding(start, top, end, bottom)

fun UiModifier.styleable(style: Style): UiModifier = primitiveStyleable(style)
fun UiModifier.weight(weight: Float, fill: Boolean = true): UiModifier = primitiveWeight(weight, fill)
