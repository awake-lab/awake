// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/** [boundingRadius] and [boundingCenter] over flat `x, y, z, x, y, z...` position arrays. */
class GeometryTest {

    // ---- boundingRadius ----

    @Test
    fun radiusOfAnEmptyArrayIsOneSoCallersCanDivideByIt() {
        assertEquals(1f, boundingRadius(FloatArray(0)), TOLERANCE)
    }

    @Test
    fun radiusOfAnAllZeroCloudIsOneRatherThanZero() {
        assertEquals(1f, boundingRadius(floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f)), TOLERANCE)
    }

    @Test
    fun radiusOfASinglePointIsItsDistanceFromTheOrigin() {
        assertEquals(5f, boundingRadius(floatArrayOf(3f, 4f, 0f)), TOLERANCE)
        assertEquals(13f, boundingRadius(floatArrayOf(3f, 4f, 12f)), TOLERANCE)
    }

    @Test
    fun radiusIgnoresSignSoAMirroredPointCountsTheSame() {
        assertEquals(5f, boundingRadius(floatArrayOf(-3f, -4f, 0f)), TOLERANCE)
    }

    @Test
    fun radiusTakesTheFarthestPointNotTheLastOne() {
        val positions = floatArrayOf(
            3f, 4f, 12f, // 13
            1f, 0f, 0f, // 1
            0f, 2f, 0f, // 2
        )

        assertEquals(13f, boundingRadius(positions), TOLERANCE)
    }

    @Test
    fun radiusIsMeasuredFromTheOriginNotFromTheCloudsOwnCentre() {
        // A tight cluster far from the origin: its own extent is 2, its distance from 0 is ~101.
        val positions = floatArrayOf(100f, 0f, 0f, 102f, 0f, 0f)

        assertEquals(102f, boundingRadius(positions), TOLERANCE)
    }

    @Test
    fun radiusOfAUnitCubeCornerIsTheBodyDiagonal() {
        val positions = floatArrayOf(
            -1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, -1f,
            -1f, -1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, -1f, 1f, 1f,
        )

        assertEquals(1.7320508f, boundingRadius(positions), TOLERANCE) // sqrt(3)
    }

    // DEFECT: boundingRadius (Geometry.kt:15) loops `while (i < positions.size)` and reads
    // positions[i + 1] / positions[i + 2], so a ragged array (length not a multiple of 3, e.g.
    // a truncated glTF accessor) reads past the end. boundingCenter guards the same case
    // correctly with `while (i + 2 < positions.size)`. Un-@Ignore once the two guards agree.
    @Ignore
    @Test
    fun radiusIgnoresATrailingPartialVertexTheWayBoundingCenterDoes() {
        val ragged = floatArrayOf(3f, 4f, 0f, 99f)

        assertEquals(5f, boundingRadius(ragged), TOLERANCE)
    }

    // ---- boundingCenter ----

    @Test
    fun centreOfAnEmptyArrayIsTheOrigin() {
        assertVec3(Vec3(0f, 0f, 0f), boundingCenter(FloatArray(0)))
    }

    @Test
    fun centreOfATooShortArrayIsTheOrigin() {
        assertVec3(Vec3(0f, 0f, 0f), boundingCenter(floatArrayOf(1f, 2f)))
    }

    @Test
    fun centreOfASinglePointIsThatPoint() {
        assertVec3(Vec3(3f, -4f, 12f), boundingCenter(floatArrayOf(3f, -4f, 12f)))
    }

    @Test
    fun centreOfASymmetricCloudIsTheOrigin() {
        val positions = floatArrayOf(
            -1f,
            -2f,
            -3f,
            1f,
            2f,
            3f,
        )

        assertVec3(Vec3(0f, 0f, 0f), boundingCenter(positions))
    }

    @Test
    fun centreOfAnAsymmetricCloudIsTheBoxMidpoint() {
        val positions = floatArrayOf(
            0f,
            0f,
            0f,
            10f,
            4f,
            -6f,
        )

        assertVec3(Vec3(5f, 2f, -3f), boundingCenter(positions))
    }

    @Test
    fun centreIsPerAxisSoOneStretchedAxisDoesNotMoveTheOthers() {
        val positions = floatArrayOf(
            -100f,
            1f,
            1f,
            100f,
            1f,
            1f,
        )

        assertVec3(Vec3(0f, 1f, 1f), boundingCenter(positions))
    }

    /**
     * The case [boundingCenter]'s doc comment exists for: a dense cluster on one side plus one
     * far vertex. The vertex *average* is dragged into the cluster (x = 1.0 here); the bounding
     * box midpoint -- what a camera should orbit -- stays at x = 5.
     */
    @Test
    fun centreIsTheBoxMidpointNotTheVertexAverage() {
        val positions = FloatArray(10 * 3)
        // Nine coincident vertices at x = 0 (a densely tessellated region) ...
        for (i in 0 until 9) {
            positions[i * 3] = 0f
        }
        // ... plus one lone vertex at x = 10.
        positions[9 * 3] = 10f

        val vertexAverage = positions.filterIndexed { index, _ -> index % 3 == 0 }.average().toFloat()

        assertEquals(1f, vertexAverage, TOLERANCE)
        assertVec3(Vec3(5f, 0f, 0f), boundingCenter(positions))
    }

    @Test
    fun centreHandlesACloudEntirelyOnOneSideOfTheOrigin() {
        val positions = floatArrayOf(
            5f,
            5f,
            5f,
            7f,
            9f,
            11f,
        )

        assertVec3(Vec3(6f, 7f, 8f), boundingCenter(positions))
    }

    @Test
    fun centreIgnoresATrailingPartialVertex() {
        val ragged = floatArrayOf(0f, 0f, 0f, 10f, 4f, -6f, 999f)

        assertVec3(Vec3(5f, 2f, -3f), boundingCenter(ragged))
    }

    private fun assertVec3(expected: Vec3, actual: Vec3) {
        assertEquals(expected.x, actual.x, TOLERANCE, "x")
        assertEquals(expected.y, actual.y, TOLERANCE, "y")
        assertEquals(expected.z, actual.z, TOLERANCE, "z")
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
