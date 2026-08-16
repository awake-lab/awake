// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrustumTest {

    /** Same identity-view setup as `CameraTest`'s `identityViewCamera` (eye at origin,
     * looking down -Z, up = +Y) -- forward/right/up all line up with the world axes, so
     * corner positions can be hand-verified directly. 90-degree fovY makes
     * `tan(fovY / 2) == 1`, so half-width/height at each plane equals that plane's distance
     * exactly, keeping the expected numbers clean. */
    private fun identityViewCamera() = Camera(
        eye = Vec3(0f, 0f, 0f),
        center = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (kotlin.math.PI / 2.0).toFloat(),
        near = 1f,
        far = 10f,
    )

    @Test
    fun cornersMatchHandComputedPositionsForIdentityViewCamera() {
        val corners = Frustum.corners(identityViewCamera(), aspect = 1f)

        assertEquals(8, corners.size)
        assertVec3Equals(Vec3(-1f, -1f, -1f), corners[0]) // near bottom-left
        assertVec3Equals(Vec3(1f, -1f, -1f), corners[1]) // near bottom-right
        assertVec3Equals(Vec3(1f, 1f, -1f), corners[2]) // near top-right
        assertVec3Equals(Vec3(-1f, 1f, -1f), corners[3]) // near top-left
        assertVec3Equals(Vec3(-10f, -10f, -10f), corners[4]) // far bottom-left
        assertVec3Equals(Vec3(10f, -10f, -10f), corners[5]) // far bottom-right
        assertVec3Equals(Vec3(10f, 10f, -10f), corners[6]) // far top-right
        assertVec3Equals(Vec3(-10f, 10f, -10f), corners[7]) // far top-left
    }

    @Test
    fun wideAspectRatioWidensCornersButNotHeight() {
        val camera = identityViewCamera()

        val square = Frustum.corners(camera, aspect = 1f)
        val wide = Frustum.corners(camera, aspect = 2f)

        // Widening aspect only stretches X (half-width), Y (half-height) is untouched.
        assertEquals(square[1].x * 2f, wide[1].x)
        assertEquals(square[1].y, wide[1].y)
    }

    /** The identity-view case above cannot catch a swapped/negated basis vector, because its
     * right/up/forward already are the world axes. This one looks down +X instead, so every
     * basis vector is somewhere other than where it started. */
    @Test
    fun cornersFollowTheCameraBasisWhenItLooksDownPlusX() {
        val camera = Camera(
            eye = Vec3(0f, 0f, 0f),
            center = Vec3(1f, 0f, 0f),
            up = Vec3(0f, 1f, 0f),
            fovYRadians = (kotlin.math.PI / 2.0).toFloat(),
            near = 1f,
            far = 10f,
        )

        // forward = +X, right = forward x up = +Z, up = right x forward = +Y.
        val corners = Frustum.corners(camera, aspect = 1f)

        assertVec3Equals(Vec3(1f, -1f, -1f), corners[0]) // near bottom-left
        assertVec3Equals(Vec3(1f, -1f, 1f), corners[1]) // near bottom-right
        assertVec3Equals(Vec3(1f, 1f, 1f), corners[2]) // near top-right
        assertVec3Equals(Vec3(1f, 1f, -1f), corners[3]) // near top-left
        assertVec3Equals(Vec3(10f, -10f, -10f), corners[4])
        assertVec3Equals(Vec3(10f, 10f, 10f), corners[6])
    }

    @Test
    fun movingTheCameraTranslatesEveryCorner() {
        val atOrigin = Frustum.corners(identityViewCamera(), aspect = 1f)
        val offset = Vec3(3f, -4f, 5f)
        val moved = Frustum.corners(
            Camera(
                eye = offset,
                center = offset + Vec3(0f, 0f, -1f),
                up = Vec3(0f, 1f, 0f),
                fovYRadians = (kotlin.math.PI / 2.0).toFloat(),
                near = 1f,
                far = 10f,
            ),
            aspect = 1f,
        )

        for (index in atOrigin.indices) {
            assertVec3Equals(atOrigin[index] + offset, moved[index])
        }
    }

    @Test
    fun nearQuadHalfHeightMatchesTanOfHalfTheFov() {
        val corners = Frustum.corners(identityViewCamera(), aspect = 1f)

        // 90 degree fovY at near = 1 => half-height 1, so the near quad spans y in [-1, 1].
        assertEquals(2f, corners[2].y - corners[1].y, 1e-4f)
        assertEquals(2f, corners[1].x - corners[0].x, 1e-4f)
        assertEquals(20f, corners[6].y - corners[5].y, 1e-4f)
    }

    @Test
    fun edgesReferenceAllEightCornersExactlyThreeTimesEach() {
        val counts = IntArray(8)
        for ((a, b) in Frustum.EDGES) {
            counts[a]++
            counts[b]++
        }
        assertEquals(List(8) { 3 }, counts.toList())
    }

    @Test
    fun boxOnTheForwardAxisIntersects() {
        val camera = identityViewCamera()
        val box = Aabb(Vec3(-0.5f, -0.5f, -5.5f), Vec3(0.5f, 0.5f, -4.5f))

        assertTrue(Frustum.intersects(camera, aspect = 1f, box))
    }

    @Test
    fun boxBehindTheCameraDoesNotIntersect() {
        val camera = identityViewCamera()
        // Camera looks down -Z from the origin -- +Z is directly behind it.
        val box = Aabb(Vec3(-0.5f, -0.5f, 4.5f), Vec3(0.5f, 0.5f, 5.5f))

        assertTrue(!Frustum.intersects(camera, aspect = 1f, box))
    }

    @Test
    fun boxAboveTheTopPlaneDoesNotIntersect() {
        val camera = identityViewCamera()
        // 90-degree fovY, near quad half-height 1 at z=-1 -- well above the top plane at any
        // depth along the forward axis.
        val box = Aabb(Vec3(-0.5f, 500f, -5.5f), Vec3(0.5f, 501f, -4.5f))

        assertTrue(!Frustum.intersects(camera, aspect = 1f, box))
    }

    @Test
    fun boxBelowTheBottomPlaneDoesNotIntersect() {
        val camera = identityViewCamera()
        val box = Aabb(Vec3(-0.5f, -501f, -5.5f), Vec3(0.5f, -500f, -4.5f))

        assertTrue(!Frustum.intersects(camera, aspect = 1f, box))
    }

    @Test
    fun boxPastTheFarPlaneDoesNotIntersect() {
        val camera = identityViewCamera()
        val box = Aabb(Vec3(-0.5f, -0.5f, -1000f), Vec3(0.5f, 0.5f, -999f))

        assertTrue(!Frustum.intersects(camera, aspect = 1f, box))
    }

    @Test
    fun boxInFrontOfTheNearPlaneDoesNotIntersect() {
        val camera = identityViewCamera()
        // Near = 1, camera at origin looking down -Z -- z in (-1, 0] is closer than near.
        val box = Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0f))

        assertTrue(!Frustum.intersects(camera, aspect = 1f, box))
    }

    private fun assertVec3Equals(expected: Vec3, actual: Vec3, epsilon: Float = 1e-4f) {
        assertTrue(kotlin.math.abs(expected.x - actual.x) < epsilon, "x: expected ${expected.x}, got ${actual.x}")
        assertTrue(kotlin.math.abs(expected.y - actual.y) < epsilon, "y: expected ${expected.y}, got ${actual.y}")
        assertTrue(kotlin.math.abs(expected.z - actual.z) < epsilon, "z: expected ${expected.z}, got ${actual.z}")
    }
}
