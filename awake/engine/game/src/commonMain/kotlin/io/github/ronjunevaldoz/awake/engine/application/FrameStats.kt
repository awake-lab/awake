// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

class FrameStats(
    private val sampleWindowSeconds: Float = 1f
) {
    var fps: Float = 0f
        private set

    var frameTimeMs: Float = 0f
        private set

    private var accumulator = 0f
    private var frameCount = 0

    fun update(deltaSeconds: Float): Boolean {
        frameTimeMs = deltaSeconds * 1000f
        accumulator += deltaSeconds
        frameCount += 1
        if (accumulator < sampleWindowSeconds) {
            return false
        }

        fps = frameCount / accumulator
        accumulator = 0f
        frameCount = 0
        return true
    }
}
