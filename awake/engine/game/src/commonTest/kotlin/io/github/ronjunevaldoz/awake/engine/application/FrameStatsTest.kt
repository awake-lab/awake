// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

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
}
