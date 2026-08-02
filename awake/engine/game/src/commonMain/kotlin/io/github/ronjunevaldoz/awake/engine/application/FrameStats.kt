// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

class FrameStats(
    private val sampleWindowSeconds: Float = 1f,
    // Rolling window for percentile tracking (independent of sampleWindowSeconds's fps-publish
    // cadence) -- 120 frames is ~2s at 60fps, enough to catch a spike without unbounded memory.
    private val percentileWindowSize: Int = 120
) {
    var fps: Float = 0f
        private set

    var frameTimeMs: Float = 0f
        private set

    private var accumulator = 0f
    private var frameCount = 0

    private val frameTimesMs = ArrayDeque<Float>()

    fun update(deltaSeconds: Float): Boolean {
        frameTimeMs = deltaSeconds * 1000f
        accumulator += deltaSeconds
        frameCount += 1

        frameTimesMs.addLast(frameTimeMs)
        if (frameTimesMs.size > percentileWindowSize) {
            frameTimesMs.removeFirst()
        }

        if (accumulator < sampleWindowSeconds) {
            return false
        }

        fps = frameCount / accumulator
        accumulator = 0f
        frameCount = 0
        return true
    }

    /** Frame time, in ms, at [percentile] (0-100) over the last [percentileWindowSize] frames
     * update() has seen -- e.g. `percentileFrameTimeMs(95f)` is the p95 frame time. Returns 0 if
     * no frames have been recorded yet. */
    fun percentileFrameTimeMs(percentile: Float): Float {
        if (frameTimesMs.isEmpty()) return 0f
        val sorted = frameTimesMs.sorted()
        val index = ((percentile / 100f) * (sorted.size - 1)).toInt().coerceIn(0, sorted.size - 1)
        return sorted[index]
    }

    val p50FrameTimeMs: Float get() = percentileFrameTimeMs(50f)
    val p95FrameTimeMs: Float get() = percentileFrameTimeMs(95f)
    val p99FrameTimeMs: Float get() = percentileFrameTimeMs(99f)
}
