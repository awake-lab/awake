// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiSemanticNode
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.scope.intersect

internal class UiContextFrameState {
    private val renderCollector = UiRenderCollector()
    private val semanticCollector = UiSemanticCollector()
    private val clipStack = ArrayList<UiSlot>()

    var inputState: UiInputState = UiInputState()
        private set
    var fullFrameRect: UiSlot = UiSlot(0f, 0f, 0f, 0f)
        private set
    var frameDeltaSeconds: Float = 1f / 60f
        private set

    fun beginFrame(screenWidth: Float, screenHeight: Float, inputState: UiInputState, deltaSeconds: Float) {
        renderCollector.beginFrame()
        semanticCollector.beginFrame()
        clipStack.clear()
        fullFrameRect = UiSlot(0f, 0f, screenWidth, screenHeight)
        frameDeltaSeconds = deltaSeconds.coerceAtLeast(0f)
        this.inputState = inputState
    }

    fun endFrame(): List<UiDrawPrimitive> = renderCollector.endFrame()

    fun emit(primitive: UiDrawPrimitive) =
        renderCollector.emit(primitive)

    fun emitOverlay(primitive: UiDrawPrimitive) =
        renderCollector.emitOverlay(primitive)

    fun recordSemantic(node: UiSemanticNode) =
        semanticCollector.record(node)

    fun semanticNodes(): List<UiSemanticNode> = semanticCollector.snapshot()

    fun pushClip(rect: UiSlot): UiSlot {
        val current = clipStack.lastOrNull() ?: fullFrameRect
        val resolved = current.intersect(rect)
        clipStack += resolved
        return resolved
    }

    fun popClip(): UiSlot {
        if (clipStack.isNotEmpty()) clipStack.removeAt(clipStack.size - 1)
        return clipStack.lastOrNull() ?: fullFrameRect
    }
}
