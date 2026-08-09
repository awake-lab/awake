// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds

internal class UiContextInteractionState {
    private var activeId: String? = null
    private var focusedId: String? = null
    private var pointerDownLastFrame = false
    private var pointerDownEdgeThisFrame = false
    private var focusClaimedThisFrame = false
    private var isOverScrollableThisFrame = false
    private var isScrollConsumedThisFrame = false
    private var requestedCursorThisFrame = UiCursor.Default

    fun beginFrame(inputState: UiInputState) {
        pointerDownEdgeThisFrame = inputState.pointerDown && !pointerDownLastFrame
        focusClaimedThisFrame = false
        isOverScrollableThisFrame = false
        isScrollConsumedThisFrame = false
        requestedCursorThisFrame = UiCursor.Default
    }

    fun endFrame(inputState: UiInputState) {
        if (pointerDownEdgeThisFrame && !focusClaimedThisFrame) {
            focusedId = null
        }
        pointerDownLastFrame = inputState.pointerDown
    }

    fun inputResult(): UiInputResult = UiInputResult(
        isCaptured = activeId != null,
        isOverScrollable = isOverScrollableThisFrame,
        isScrollConsumed = isScrollConsumedThisFrame,
        isTextInputFocused = focusedId != null,
    )

    fun onOverScrollable() {
        isOverScrollableThisFrame = true
    }

    fun onScrollConsumed() {
        isScrollConsumedThisFrame = true
    }

    /** Last call each frame wins, same "no priority, last writer settles it" shape
     * [setActive] already uses -- a widget only calls this while hovered/dragging, so the
     * common case is at most one call per frame anyway. */
    fun requestCursor(cursor: UiCursor) {
        requestedCursorThisFrame = cursor
    }

    fun requestedCursor(): UiCursor = requestedCursorThisFrame

    fun hitTest(slot: UiBounds, inputState: UiInputState): Boolean =
        inputState.pointerX in slot.x..(slot.x + slot.width) &&
            inputState.pointerY in slot.y..(slot.y + slot.height)

    fun isActive(id: String): Boolean = activeId == id

    fun tryClaimActive(id: String, hovered: Boolean, inputState: UiInputState) {
        if (hovered && inputState.pointerDown && activeId == null) activeId = id
    }

    fun releaseActiveIfMatches(id: String, inputState: UiInputState) {
        if (!inputState.pointerDown && activeId == id) activeId = null
    }

    fun isFocused(id: String): Boolean = focusedId == id

    fun requestFocus(id: String) {
        focusedId = id
        focusClaimedThisFrame = true
    }

    fun clearFocusIfMatches(id: String) {
        if (focusedId == id) focusedId = null
    }

    fun pointerDownEdge(): Boolean = pointerDownEdgeThisFrame

    fun setActive(id: String?) {
        activeId = id
    }
}
