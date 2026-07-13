// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PixelBaselineTest {

    @Test
    fun identicalBuffersMatch() {
        val pixels = byteArrayOf(10, 20, 30, -1, 40, 50, 60, -1)
        val result = comparePixels(pixels, pixels.copyOf())
        assertTrue(result.matches)
        assertEquals(0, result.diffPixelCount)
        assertEquals(0, result.maxChannelDiff)
    }

    @Test
    fun diffWithinToleranceStillMatches() {
        val actual = byteArrayOf(10, 20, 30, -1)
        val expected = byteArrayOf(11, 20, 31, -1)
        val result = comparePixels(actual, expected, toleranceParChannel = 2)
        assertTrue(result.matches, "a 1-value-per-channel diff must pass a tolerance of 2")
        assertEquals(0, result.diffPixelCount)
        assertEquals(1, result.maxChannelDiff)
    }

    @Test
    fun diffBeyondToleranceFails() {
        val actual = byteArrayOf(10, 20, 30, -1, 100, 100, 100, -1)
        val expected = byteArrayOf(10, 20, 30, -1, 0, 100, 100, -1)
        val result = comparePixels(actual, expected, toleranceParChannel = 2)
        assertFalse(result.matches)
        assertEquals(1, result.diffPixelCount, "only the second pixel should count as mismatched")
        assertEquals(100, result.maxChannelDiff)
    }

    @Test
    fun sizeMismatchThrows() {
        assertFailsWith<IllegalArgumentException> {
            comparePixels(byteArrayOf(1, 2, 3, 4), byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        }
    }
}
