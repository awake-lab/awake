// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.WidgetState

internal class UiRuntimeCoordinator(
    private val interaction: UiContextInteractionState = UiContextInteractionState(),
    private val frameState: UiContextFrameState = UiContextFrameState(),
    private val stateStore: UiStateStore = UiStateStore()
) {
    private var finalizedFrameOutput: UiFrameOutput? = null

    val inputState: UiInputState get() = frameState.inputState
    val frameDeltaSeconds: Float get() = frameState.frameDeltaSeconds
    val fullFrameRect: UiSlot get() = frameState.fullFrameRect

    fun beginFrame(
        screenWidth: Float,
        screenHeight: Float,
        inputState: UiInputState,
        deltaSeconds: Float
    ) {
        finalizedFrameOutput = null
        frameState.beginFrame(screenWidth, screenHeight, inputState, deltaSeconds)
        interaction.beginFrame(inputState)
    }

    fun inputResult(): UiInputResult = finalizedFrameOutput?.ownership?.toInputResult() ?: interaction.inputResult()

    fun endFrame(): List<UiDrawPrimitive> = finishFrame().primitives

    fun finishFrame(): UiFrameOutput = finalizedFrameOutput ?: finalizeFrame().also {
        finalizedFrameOutput = it
    }

    private fun finalizeFrame(): UiFrameOutput {
        interaction.endFrame(frameState.inputState)
        val ownership = interaction.inputResult().toOwnership()
        return UiFrameOutput(
            primitives = frameState.endFrame(),
            semantics = frameState.semanticNodes(),
            ownership = ownership,
            effects = UiPlatformEffects(requestKeyboard = ownership.isTextInputFocused)
        )
    }

    fun onOverScrollable(measuring: Boolean) {
        if (!measuring) interaction.onOverScrollable()
    }

    fun onScrollConsumed(measuring: Boolean) {
        if (!measuring) interaction.onScrollConsumed()
    }

    fun semanticNodes(): List<UiSemanticNode> = finalizedFrameOutput?.semantics ?: frameState.semanticNodes()

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

    fun widgetState(id: String): WidgetState = stateStore.widgetState(id)

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

private fun UiInputResult.toOwnership(): UiInputOwnership = UiInputOwnership(
    isCaptured = isCaptured,
    isOverScrollable = isOverScrollable,
    isScrollConsumed = isScrollConsumed,
    isTextInputFocused = isTextInputFocused
)

private fun UiInputOwnership.toInputResult(): UiInputResult = UiInputResult(
    isCaptured = isCaptured,
    isOverScrollable = isOverScrollable,
    isScrollConsumed = isScrollConsumed,
    isTextInputFocused = isTextInputFocused
)
