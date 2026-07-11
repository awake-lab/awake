// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

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
        far = 10f
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

    @Test
    fun edgesReferenceAllEightCornersExactlyThreeTimesEach() {
        val counts = IntArray(8)
        for ((a, b) in Frustum.EDGES) {
            counts[a]++
            counts[b]++
        }
        assertEquals(List(8) { 3 }, counts.toList())
    }

    private fun assertVec3Equals(expected: Vec3, actual: Vec3, epsilon: Float = 1e-4f) {
        assert(kotlin.math.abs(expected.x - actual.x) < epsilon) { "x: expected ${expected.x}, got ${actual.x}" }
        assert(kotlin.math.abs(expected.y - actual.y) < epsilon) { "y: expected ${expected.y}, got ${actual.y}" }
        assert(kotlin.math.abs(expected.z - actual.z) < epsilon) { "z: expected ${expected.z}, got ${actual.z}" }
    }
}
