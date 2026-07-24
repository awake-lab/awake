// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.styling.UiInsets

val UiScope.inputState: UiInputState
    get() = context.inputState

fun UiScope.frameBounds(): UiSlot = context.frameBoundsInternal()

fun UiScope.frameDeltaSeconds(): Float = context.frameDeltaSecondsInternal()

fun UiScope.isMeasuring(): Boolean = context.isMeasuringInternal()

fun UiScope.pointerDownEdge(): Boolean = context.pointerDownEdgeInternal()

fun UiScope.pointerX(): Float = context.pointerXInternal()

fun UiScope.pointerY(): Float = context.pointerYInternal()

fun UiScope.pointerDown(): Boolean = context.pointerDownInternal()

fun UiScope.isFocused(id: String): Boolean = context.isFocusedInternal(id)

fun UiScope.requestFocus(id: String) = context.requestFocusInternal(id)

fun UiScope.clearFocusIfMatches(id: String) = context.clearFocusIfMatchesInternal(id)

fun UiScope.setActive(id: String?) = context.setActiveInternal(id)

fun UiScope.onOverScrollable() = context.onOverScrollableInternal()

fun UiScope.onScrollConsumed() = context.onScrollConsumedInternal()

fun UiScope.measureColumnContent(
    width: Float,
    gap: Float = UiSpacing.sm.toPx(),
    insets: UiInsets = UiInsets.Zero,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiMeasuredContent = context.measureColumnContentInternal(
    width = width,
    gap = gap,
    insets = insets,
    content = content
)

fun UiScope.measureRowContent(
    height: Float,
    gap: Float,
    insets: UiInsets = UiInsets.Zero,
    content: RowScope.(slot: UiSlot) -> Unit
): UiMeasuredContent = context.measureRowContentInternal(
    height = height,
    gap = gap,
    insets = insets,
    content = content
)
