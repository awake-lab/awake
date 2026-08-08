// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.scope

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.toPx

val UiScope.inputState: UiInputState
    get() = context.inputState

fun UiScope.frameBounds(): UiBounds = context.frameBoundsInternal()

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
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiMeasuredContent = context.measureColumnContentInternal(
    width = width,
    gap = gap,
    insets = insets,
    content = content,
)

fun UiScope.measureRowContent(
    height: Float,
    gap: Float,
    insets: UiInsets = UiInsets.Zero,
    content: RowScope.(slot: UiBounds) -> Unit,
): UiMeasuredContent = context.measureRowContentInternal(
    height = height,
    gap = gap,
    insets = insets,
    content = content,
)
