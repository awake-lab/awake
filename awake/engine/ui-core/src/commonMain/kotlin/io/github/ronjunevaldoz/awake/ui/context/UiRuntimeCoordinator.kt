// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.WidgetState

internal class UiRuntimeCoordinator(
    private val interaction: UiContextInteractionState = UiContextInteractionState(),
    private val frameState: UiContextFrameState = UiContextFrameState()
) {
    val inputState: UiInputState get() = frameState.inputState
    val frameDeltaSeconds: Float get() = frameState.frameDeltaSeconds
    val fullFrameRect: UiSlot get() = frameState.fullFrameRect

    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputState: UiInputState,
        deltaSeconds: Float
    ) {
        frameState.beginFrame(screenWidth, screenHeight, inputState, deltaSeconds)
        interaction.beginFrame(inputState)
    }

    fun inputResult(): UiInputResult = interaction.inputResult()

    fun endFrame(): List<UiDrawPrimitive> {
        interaction.endFrame(frameState.inputState)
        return frameState.endFrame()
    }

    fun onOverScrollable(measuring: Boolean) {
        if (!measuring) interaction.onOverScrollable()
    }

    fun onScrollConsumed(measuring: Boolean) {
        if (!measuring) interaction.onScrollConsumed()
    }

    fun semanticNodes(): List<UiSemanticNode> = frameState.semanticNodes()

    fun hitTest(slot: UiSlot, measuring: Boolean): Boolean =
        interaction.hitTest(slot, frameState.inputState, measuring)

    fun isActive(id: String): Boolean = interaction.isActive(id)

    fun tryClaimActive(id: String, hovered: Boolean, measuring: Boolean) {
        interaction.tryClaimActive(id, hovered, frameState.inputState, measuring)
    }

    fun releaseActiveIfMatches(id: String, measuring: Boolean) {
        interaction.releaseActiveIfMatches(id, frameState.inputState, measuring)
    }

    fun isFocused(id: String): Boolean = interaction.isFocused(id)

    fun requestFocus(id: String, measuring: Boolean) {
        interaction.requestFocus(id, measuring)
    }

    fun clearFocusIfMatches(id: String, measuring: Boolean) {
        interaction.clearFocusIfMatches(id, measuring)
    }

    fun emit(primitive: UiDrawPrimitive, measuring: Boolean) {
        frameState.emit(primitive, measuring)
    }

    fun emitOverlay(primitive: UiDrawPrimitive, measuring: Boolean) {
        frameState.emitOverlay(primitive, measuring)
    }

    fun widgetState(id: String): WidgetState = interaction.widgetState(id)

    fun recordSemantic(node: UiSemanticNode, measuring: Boolean) {
        frameState.recordSemantic(node, measuring)
    }

    fun pushClip(rect: UiSlot): UiSlot = frameState.pushClip(rect)

    fun popClip(): UiSlot = frameState.popClip()

    fun pointerDownEdge(): Boolean = interaction.pointerDownEdge()

    fun setActive(id: String?) {
        interaction.setActive(id)
    }
}
