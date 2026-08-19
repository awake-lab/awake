// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

internal class UiContextInteractionState {
    private var activeId: String? = null
    private var focusedId: String? = null
    private var pointerDownLastFrame = false
    private var pointerDownEdgeThisFrame = false
    private var focusClaimedThisFrame = false
    private var isOverScrollableThisFrame = false
    private var isScrollConsumedThisFrame = false
    private var requestedCursorThisFrame = UiCursor.Default

    private val activeOcclusionBounds = mutableListOf<UiBounds>()
    private val previousOcclusionBounds = mutableListOf<UiBounds>()
    private var modalActive = false
    private var modalActiveLastFrame = false

    fun beginFrame(inputState: UiInputState) {
        pointerDownEdgeThisFrame = inputState.pointerDown && !pointerDownLastFrame
        focusClaimedThisFrame = false
        isOverScrollableThisFrame = false
        isScrollConsumedThisFrame = false
        requestedCursorThisFrame = UiCursor.Default

        previousOcclusionBounds.clear()
        previousOcclusionBounds.addAll(activeOcclusionBounds)
        activeOcclusionBounds.clear()
        modalActiveLastFrame = modalActive
        modalActive = false
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

    fun registerOverlayOcclusion(bounds: UiBounds, isModal: Boolean = false) {
        activeOcclusionBounds.add(bounds)
        if (isModal) modalActive = true
    }

    /** Last call each frame wins, same "no priority, last writer settles it" shape
     * [setActive] already uses -- a widget only calls this while hovered/dragging, so the
     * common case is at most one call per frame anyway. */
    fun requestCursor(cursor: UiCursor) {
        requestedCursorThisFrame = cursor
    }

    fun requestedCursor(): UiCursor = requestedCursorThisFrame

    fun hitTest(slot: UiBounds, inputState: UiInputState, overlay: Boolean = false): Boolean {
        val inside = inputState.pointerX in slot.x..(slot.x + slot.width) &&
            inputState.pointerY in slot.y..(slot.y + slot.height)
        if (!inside) return false
        if (overlay) return true
        if (modalActiveLastFrame) return false
        val px = inputState.pointerX
        val py = inputState.pointerY
        for (i in 0 until previousOcclusionBounds.size) {
            val occ = previousOcclusionBounds[i]
            if (px in occ.x..(occ.x + occ.width) && py in occ.y..(occ.y + occ.height)) {
                return false
            }
        }
        for (i in 0 until activeOcclusionBounds.size) {
            val occ = activeOcclusionBounds[i]
            if (px in occ.x..(occ.x + occ.width) && py in occ.y..(occ.y + occ.height)) {
                return false
            }
        }
        return true
    }

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
