// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOLERANCE = 1e-3f

class AabbTest {

    @Test
    fun aBoxIsFittedToPackedPositions() {
        val positions = floatArrayOf(
            -1f, 0f, 2f,
            3f, -4f, 0f,
            0f, 1f, -5f,
        )
        val box = assertNotNull(Aabb.fromPositions(positions))
        assertEquals(Vec3(-1f, -4f, -5f), box.min)
        assertEquals(Vec3(3f, 1f, 2f), box.max)
    }

    /** Interleaved vertex data (position + normal + colour) is the common case; reading it as
     * tight triples would treat normals as positions. */
    @Test
    fun aStrideSkipsInterleavedAttributes() {
        val positions = floatArrayOf(
            1f, 2f, 3f, 99f, 99f,
            -1f, -2f, -3f, 99f, 99f,
        )
        val box = assertNotNull(Aabb.fromPositions(positions, stride = 5))
        assertEquals(Vec3(-1f, -2f, -3f), box.min)
        assertEquals(Vec3(1f, 2f, 3f), box.max)
    }

    /** "No vertices" and "a point at the origin" are different facts, and a caller picking
     * against the second hit-tests something that isn't there. */
    @Test
    fun emptyInputHasNoBox() {
        assertNull(Aabb.fromPositions(FloatArray(0)))
    }

    @Test
    fun centreAndExtentsDescribeTheBox() {
        val box = Aabb(Vec3(-2f, 0f, -6f), Vec3(2f, 4f, 0f))
        assertEquals(Vec3(0f, 2f, -3f), box.center)
        assertEquals(Vec3(2f, 2f, 3f), box.extents)
        assertTrue(Vec3(0f, 1f, -1f) in box)
        assertFalse(Vec3(0f, 9f, -1f) in box)
    }

    @Test
    fun unionCoversBothBoxes() {
        val a = Aabb(Vec3(0f, 0f, 0f), Vec3(1f, 1f, 1f))
        val b = Aabb(Vec3(-5f, 2f, 0f), Vec3(-4f, 3f, 1f))
        val union = a.union(b)
        assertEquals(Vec3(-5f, 0f, 0f), union.min)
        assertEquals(Vec3(1f, 3f, 1f), union.max)
    }

    /**
     * Transforming only min and max is the classic bug: under rotation those corners stop being
     * the extremes, and the re-fitted box comes out too small exactly where geometry sticks out.
     * A unit cube rotated 45 degrees about Y must grow to sqrt(2) across X and Z.
     */
    @Test
    fun rotationRefitsAroundEveryCorner() {
        val box = Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f))
        val rotated = box.transformed(Mat4().rotateY((PI / 4).toFloat()))
        assertEquals(0.7071f, rotated.max.x, TOLERANCE)
        assertEquals(0.7071f, rotated.max.z, TOLERANCE)
        assertEquals(0.5f, rotated.max.y, TOLERANCE, "the untouched axis must not grow")
    }

    @Test
    fun translationMovesTheBox() {
        val box = Aabb(Vec3(-1f, -1f, -1f), Vec3(1f, 1f, 1f))
        val moved = box.transformed(Mat4().translate(10f, 0f, 0f))
        assertEquals(9f, moved.min.x, TOLERANCE)
        assertEquals(11f, moved.max.x, TOLERANCE)
    }

    @Test
    fun cornersAreEveryMinMaxCombination() {
        val box = Aabb(Vec3(-1f, -2f, -3f), Vec3(1f, 2f, 3f))

        val corners = box.corners()

        assertEquals(8, corners.size)
        assertEquals(Vec3(-1f, -2f, -3f), corners[0])
        assertEquals(Vec3(1f, 2f, 3f), corners[7])
        // Every coordinate is either the box's min or max on that axis.
        for (corner in corners) {
            assertTrue(corner.x == box.min.x || corner.x == box.max.x)
            assertTrue(corner.y == box.min.y || corner.y == box.max.y)
            assertTrue(corner.z == box.min.z || corner.z == box.max.z)
        }
    }

    @Test
    fun edgesReferenceAllEightCornersExactlyThreeTimesEach() {
        val counts = IntArray(8)
        for ((a, b) in Aabb.EDGES) {
            counts[a]++
            counts[b]++
        }
        assertEquals(List(8) { 3 }, counts.toList())
    }
}
