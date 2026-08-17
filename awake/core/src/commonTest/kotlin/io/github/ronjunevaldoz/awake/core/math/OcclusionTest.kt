// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OcclusionTest {

    /** Same identity-view setup `FrustumTest` uses (eye at origin, looking down -Z, up = +Y,
     * 90-degree fovY so `tan(fovY / 2) == 1`) -- corner projections can be hand-verified. */
    private fun identityViewCamera() = Camera(
        eye = Vec3(0f, 0f, 0f),
        center = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (kotlin.math.PI / 2.0).toFloat(),
        near = 1f,
        far = 10f,
    )

    private fun Camera.viewProjection(aspect: Float = 1f) = viewProjectionMatrix(aspect, ClipSpace.WebGpu)

    @Test
    fun screenBoundsReturnsNullWhenEveryCornerIsBehindTheCamera() {
        val camera = identityViewCamera()
        val boxBehind = Aabb(Vec3(-1f, -1f, 1f), Vec3(1f, 1f, 3f)) // +Z is behind an eye looking down -Z

        assertNull(camera.screenBounds(boxBehind, camera.viewProjection()))
    }

    @Test
    fun screenBoundsReturnsNullWhenABoxStraddlesTheNearPlane() {
        val camera = identityViewCamera()
        // near = 1, so z in [-1.5, -0.5] straddles it -- some corners in front, some behind.
        val straddling = Aabb(Vec3(-1f, -1f, -1.5f), Vec3(1f, 1f, -0.5f))

        assertNull(camera.screenBounds(straddling, camera.viewProjection()))
    }

    @Test
    fun screenBoundsNearestDistanceIsTheClosestSurfacePointToEye() {
        val camera = identityViewCamera()
        // x/y pinned to 0 (min == max) so the nearest-surface-point distance reduces to plain
        // |z|, isolating it from the box's lateral extent.
        val box = Aabb(Vec3(0f, 0f, -3f), Vec3(0f, 0f, -2f))

        val bounds = assertNotNull(camera.screenBounds(box, camera.viewProjection()))

        // Nearest face sits at z = -2 (distance 2 from the origin eye along -Z).
        assertEqualsWithEpsilon(2f, bounds.nearestDistance)
    }

    @Test
    fun screenBoundsNearestDistanceIsNotFooledByAWideBoxsFarCorners() {
        val camera = identityViewCamera()
        // A wide, thin wall centered on the view axis: its CORNERS are far off-axis (fooling a
        // corner-minimum distance into reporting the wall as "far"), but its actual nearest
        // surface point sits directly in front of the eye at z = -2.
        val wideWall = Aabb(Vec3(-50f, -50f, -3f), Vec3(50f, 50f, -2f))

        val bounds = assertNotNull(camera.screenBounds(wideWall, camera.viewProjection()))

        assertEqualsWithEpsilon(2f, bounds.nearestDistance)
    }

    @Test
    fun isOccludedByIsTrueWhenCandidateIsFullyInsideAFartherOccluder() {
        val occluder = ScreenBounds(minX = 0f, minY = 0f, maxX = 1f, maxY = 1f, nearestDistance = 2f)
        val candidate = ScreenBounds(minX = 0.4f, minY = 0.4f, maxX = 0.6f, maxY = 0.6f, nearestDistance = 5f)

        assertTrue(isOccludedBy(candidate, occluder))
    }

    @Test
    fun isOccludedByIsFalseWhenCandidateOnlyPartiallyOverlapsTheOccluderRect() {
        val occluder = ScreenBounds(minX = 0f, minY = 0f, maxX = 1f, maxY = 1f, nearestDistance = 2f)
        val candidate = ScreenBounds(minX = 0.5f, minY = 0.5f, maxX = 1.5f, maxY = 1.5f, nearestDistance = 5f)

        assertFalse(isOccludedBy(candidate, occluder))
    }

    @Test
    fun isOccludedByIsFalseWhenCandidateIsNearerThanTheOccluder() {
        val occluder = ScreenBounds(minX = 0f, minY = 0f, maxX = 1f, maxY = 1f, nearestDistance = 5f)
        val candidate = ScreenBounds(minX = 0.4f, minY = 0.4f, maxX = 0.6f, maxY = 0.6f, nearestDistance = 2f)

        assertFalse(isOccludedBy(candidate, occluder))
    }

    @Test
    fun isOccludedByIsFalseWhenRectsDoNotOverlapAtAll() {
        val occluder = ScreenBounds(minX = 0f, minY = 0f, maxX = 1f, maxY = 1f, nearestDistance = 2f)
        val candidate = ScreenBounds(minX = 2f, minY = 2f, maxX = 3f, maxY = 3f, nearestDistance = 5f)

        assertFalse(isOccludedBy(candidate, occluder))
    }

    private fun assertEqualsWithEpsilon(expected: Float, actual: Float, epsilon: Float = 1e-3f) {
        assertTrue(
            kotlin.math.abs(expected - actual) < epsilon,
            "Expected $expected but was $actual (epsilon $epsilon)",
        )
    }
}
