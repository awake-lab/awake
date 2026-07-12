// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class GridTest {

    @Test
    fun returnsTwoTimesDivisionsPlusOneLines() {
        val lines = Grid.lines(size = 10f, divisions = 4)
        assertEquals(2 * (4 + 1), lines.size)
    }

    /** `size = 2f`, `divisions = 2` -- step is `1f`, so lines fall on the clean coordinates
     * `-1, 0, 1` on both axes. Hand-computed expected lines below. */
    @Test
    fun smallGridMatchesHandComputedLines() {
        val lines = Grid.lines(size = 2f, divisions = 2, y = 0.5f)

        assertEquals(6, lines.size)

        // Lines parallel to X, one per Z in {-1, 0, 1}.
        assertVec3PairEquals(Vec3(-1f, 0.5f, -1f), Vec3(1f, 0.5f, -1f), lines[0])
        assertVec3PairEquals(Vec3(-1f, 0.5f, 0f), Vec3(1f, 0.5f, 0f), lines[1])
        assertVec3PairEquals(Vec3(-1f, 0.5f, 1f), Vec3(1f, 0.5f, 1f), lines[2])

        // Lines parallel to Z, one per X in {-1, 0, 1}.
        assertVec3PairEquals(Vec3(-1f, 0.5f, -1f), Vec3(-1f, 0.5f, 1f), lines[3])
        assertVec3PairEquals(Vec3(0f, 0.5f, -1f), Vec3(0f, 0.5f, 1f), lines[4])
        assertVec3PairEquals(Vec3(1f, 0.5f, -1f), Vec3(1f, 0.5f, 1f), lines[5])
    }

    @Test
    fun defaultsToYZero() {
        val lines = Grid.lines(size = 4f, divisions = 1)
        for ((start, end) in lines) {
            assertEquals(0f, start.y)
            assertEquals(0f, end.y)
        }
    }

    private fun assertVec3PairEquals(expectedStart: Vec3, expectedEnd: Vec3, actual: Pair<Vec3, Vec3>, epsilon: Float = 1e-4f) {
        assertVec3Equals(expectedStart, actual.first, epsilon)
        assertVec3Equals(expectedEnd, actual.second, epsilon)
    }

    private fun assertVec3Equals(expected: Vec3, actual: Vec3, epsilon: Float = 1e-4f) {
        assert(kotlin.math.abs(expected.x - actual.x) < epsilon) { "x: expected ${expected.x}, got ${actual.x}" }
        assert(kotlin.math.abs(expected.y - actual.y) < epsilon) { "y: expected ${expected.y}, got ${actual.y}" }
        assert(kotlin.math.abs(expected.z - actual.z) < epsilon) { "z: expected ${expected.z}, got ${actual.z}" }
    }
}
