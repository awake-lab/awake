// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Pins the inverse down by round-tripping through this codebase's own [transformPosition] rather
 * than asserting a matrix layout: `Mat4` stores column-major and multiplies row-vector style
 * (`A * B` computes the conventional `B * A`), so a test written against textbook indices would
 * pass or fail for reasons that have nothing to do with the inverse being right.
 */
class Mat4InverseTest {

    @Test
    fun aTransformedPointComesBackThroughTheInverse() {
        val transform = Mat4.fromTrs(
            translation = Vec3(3f, -2f, 5f),
            rotation = Quat.fromAxisAngle(Vec3(0f, 1f, 0f), 0.7f),
            scale = Vec3(2f, 2f, 2f),
        )
        val inverse = assertNotNull(transform.inverse())
        val original = Vec4(1.5f, -0.25f, 4f, 1f)

        val moved = transform.transformPosition(original)
        val returned = inverse.transformPosition(moved)

        assertEquals(original.x, returned.x, TOLERANCE)
        assertEquals(original.y, returned.y, TOLERANCE)
        assertEquals(original.z, returned.z, TOLERANCE)
        assertEquals(original.w, returned.w, TOLERANCE)
    }

    /** The projective case is the one unprojection actually needs: w is no longer 1 after a
     * perspective transform, and an inverse that only handles affine matrices silently drops it. */
    @Test
    fun aPerspectiveProjectionIsInvertible() {
        val camera = Camera(
            eye = Vec3(0f, 2f, 6f),
            center = Vec3(0f, 0f, 0f),
            fovYRadians = 1f,
            near = 0.1f,
            far = 100f,
        )
        val viewProjection = camera.viewProjectionMatrix(16f / 9f, ClipSpace.WebGpu)
        val inverse = assertNotNull(viewProjection.inverse())
        val world = Vec4(1f, 0.5f, -2f, 1f)

        val clip = viewProjection.transformPosition(world)
        val returned = inverse.transformPosition(clip)

        // Back through the perspective divide: the round trip lands on the same ray, scaled by w.
        assertEquals(world.x, returned.x / returned.w, TOLERANCE)
        assertEquals(world.y, returned.y / returned.w, TOLERANCE)
        assertEquals(world.z, returned.z / returned.w, TOLERANCE)
    }

    @Test
    fun composingWithTheInverseGivesTheIdentity() {
        val transform = Mat4.fromTrs(
            translation = Vec3(-1f, 4f, 2f),
            rotation = Quat.fromAxisAngle(Vec3(1f, 0f, 0f), -0.4f),
            scale = Vec3(1f, 3f, 0.5f),
        )
        val identity = transform * assertNotNull(transform.inverse())
        val expected = Mat4()
        expected.data.indices.forEach { index ->
            assertEquals(expected.data[index], identity.data[index], TOLERANCE, "element $index")
        }
    }

    /** A zero scale collapses an axis: there is no inverse, and returning identity or garbage
     * would put the error a long way from the call that caused it. */
    @Test
    fun aSingularMatrixHasNoInverse() {
        val collapsed = Mat4().scale(1f, 0f, 1f)
        assertNull(collapsed.inverse())
        assertEquals(0f, collapsed.determinant(), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-3f
    }
}
