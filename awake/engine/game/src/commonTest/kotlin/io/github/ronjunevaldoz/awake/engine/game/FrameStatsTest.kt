// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FrameStatsTest {

    @Test
    fun updateTracksFrameTimeAndPublishesFpsWhenWindowElapses() {
        val stats = FrameStats(sampleWindowSeconds = 0.5f)

        assertFalse(stats.update(0.2f))
        assertEquals(200f, stats.frameTimeMs)
        assertEquals(0f, stats.fps)

        assertFalse(stats.update(0.2f))
        assertEquals(200f, stats.frameTimeMs)
        assertEquals(0f, stats.fps)

        assertTrue(stats.update(0.2f))
        assertEquals(200f, stats.frameTimeMs)
        assertEquals(5f, stats.fps)
    }

    @Test
    fun updateStartsANewSamplingWindowAfterPublishing() {
        val stats = FrameStats(sampleWindowSeconds = 0.25f)

        assertTrue(stats.update(0.25f))
        assertEquals(4f, stats.fps)

        assertFalse(stats.update(0.1f))
        assertEquals(100f, stats.frameTimeMs)
        assertEquals(4f, stats.fps)

        assertTrue(stats.update(0.15f))
        assertEquals(8f, stats.fps)
    }

    @Test
    fun percentileFrameTimeMsReflectsRollingWindow() {
        val stats = FrameStats(sampleWindowSeconds = 100f, percentileWindowSize = 4)

        assertEquals(0f, stats.percentileFrameTimeMs(50f))

        stats.update(0.010f) // 10ms
        stats.update(0.020f) // 20ms
        stats.update(0.030f) // 30ms
        stats.update(0.100f) // 100ms spike

        // Nearest-rank on sorted {10,20,30,100}, index = floor(percentile/100 * (size-1)).
        assertEquals(20f, stats.p50FrameTimeMs) // floor(0.50*3)=1 -> 20
        assertEquals(30f, stats.p95FrameTimeMs) // floor(0.95*3)=2 -> 30
        assertEquals(30f, stats.p99FrameTimeMs) // floor(0.99*3)=2 -> 30

        // Window size 4: oldest frame (10ms) rolls off once a 5th frame lands.
        stats.update(0.025f)
        assertEquals(20f, stats.percentileFrameTimeMs(0f)) // min of {20,30,100,25}
    }
}
