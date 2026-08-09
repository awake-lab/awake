// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.application

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FixedTimestepLoopTest {
    private val fixedDelta = 1f / 60f

    @Test
    fun rendersExactlyOncePerAdvanceCallRegardlessOfStepCount() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 5)
        var renderCalls = 0

        loop.advance(frameDelta = fixedDelta * 2.5f, fixedUpdate = {}, render = { renderCalls++ })

        assertEquals(1, renderCalls)
    }

    @Test
    fun accumulatesPartialFrameDeltaAcrossCalls() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 5)
        var steps = 0

        // Two half-steps should combine into exactly one fixed update, not zero or two.
        loop.advance(frameDelta = fixedDelta * 0.5f, fixedUpdate = { steps++ }, render = {})
        assertEquals(0, steps)
        loop.advance(frameDelta = fixedDelta * 0.5f, fixedUpdate = { steps++ }, render = {})
        assertEquals(1, steps)
    }

    @Test
    fun runsMultipleFixedStepsWhenFrameDeltaIsLarge() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 5)
        var steps = 0

        loop.advance(frameDelta = fixedDelta * 3.2f, fixedUpdate = { steps++ }, render = {})

        assertEquals(3, steps)
    }

    @Test
    fun everyFixedUpdateCallReceivesExactlyFixedDelta() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 5)
        val deltasSeen = mutableListOf<Float>()

        loop.advance(frameDelta = fixedDelta * 3f, fixedUpdate = { deltasSeen += it }, render = {})

        assertEquals(listOf(fixedDelta, fixedDelta, fixedDelta), deltasSeen)
    }

    @Test
    fun cannotExceedMaxStepsPerFrameAndDropsTheRemainder() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 3)
        var steps = 0

        // Ten pending steps' worth of delta in one frame -- without the cap this would run
        // 10 fixed updates in a single advance() call.
        loop.advance(frameDelta = fixedDelta * 10f, fixedUpdate = { steps++ }, render = {})

        assertEquals(3, steps)
    }

    @Test
    fun droppedRemainderDoesNotCarryOverToTheNextFrame() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 3)
        var steps = 0

        loop.advance(frameDelta = fixedDelta * 10f, fixedUpdate = { steps++ }, render = {})
        assertEquals(3, steps)

        // If the leftover 7 steps' worth of time had carried over, this call alone would
        // immediately hit the cap again with zero new frameDelta.
        loop.advance(frameDelta = 0f, fixedUpdate = { steps++ }, render = {})
        assertEquals(3, steps)
    }

    @Test
    fun renderAlphaReflectsFractionalProgressTowardTheNextStep() {
        val loop = FixedTimestepLoop(fixedDelta = fixedDelta, maxStepsPerFrame = 5)
        var alpha = -1f

        loop.advance(frameDelta = fixedDelta * 0.25f, fixedUpdate = {}, render = { alpha = it })

        assertTrue(alpha in 0f..1f)
        assertTrue(kotlin.math.abs(0.25f - alpha) < 1e-4f, "expected ~0.25, got $alpha")
    }
}
