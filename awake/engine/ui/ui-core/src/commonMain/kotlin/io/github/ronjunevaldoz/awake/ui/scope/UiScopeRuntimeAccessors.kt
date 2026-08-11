// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.scope

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.context.UiMeasuredContent
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.toPx

val UiPrimitiveScope.inputState: UiInputState
    get() = context.inputState

fun UiPrimitiveScope.frameBounds(): UiBounds = context.frameBoundsInternal()

fun UiPrimitiveScope.frameDeltaSeconds(): Float = context.frameDeltaSecondsInternal()

fun UiPrimitiveScope.isMeasuring(): Boolean = context.isMeasuringInternal()

fun UiPrimitiveScope.pointerDownEdge(): Boolean = context.pointerDownEdgeInternal()

fun UiPrimitiveScope.pointerX(): Float = context.pointerXInternal()

fun UiPrimitiveScope.pointerY(): Float = context.pointerYInternal()

fun UiPrimitiveScope.pointerDown(): Boolean = context.pointerDownInternal()

fun UiPrimitiveScope.isFocused(id: String): Boolean = context.isFocusedInternal(id)

fun UiPrimitiveScope.requestFocus(id: String) = context.requestFocusInternal(id)

fun UiPrimitiveScope.clearFocusIfMatches(id: String) = context.clearFocusIfMatchesInternal(id)

fun UiPrimitiveScope.setActive(id: String?) = context.setActiveInternal(id)

fun UiPrimitiveScope.onOverScrollable() = context.onOverScrollableInternal()

fun UiPrimitiveScope.onScrollConsumed() = context.onScrollConsumedInternal()

/** Requests [cursor] as this frame's platform pointer shape -- see [UiCursor]'s doc comment.
 * Call while hovered/dragging; last call in the frame wins. */
fun UiPrimitiveScope.requestCursor(cursor: UiCursor) = context.requestCursorInternal(cursor)

fun UiPrimitiveScope.measureColumnContent(
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

fun UiPrimitiveScope.measureRowContent(
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
