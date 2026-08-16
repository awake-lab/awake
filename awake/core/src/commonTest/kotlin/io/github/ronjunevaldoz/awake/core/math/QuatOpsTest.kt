// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val TOLERANCE = 1e-3f
private val QUARTER = (PI / 2.0).toFloat()

class QuatOpsTest {

    @Test
    fun rotatingAPointMatchesTheEquivalentMatrix() {
        val rotation = Quat.fromAxisAngle(Vec3(0f, 1f, 0f), QUARTER)
        val point = Vec3(1f, 0f, 0f)

        val rotated = rotation.rotate(point)
        val viaMatrix = rotation.toMat4().transformPosition(Vec4(point.x, point.y, point.z, 1f))

        assertEquals(viaMatrix.x, rotated.x, TOLERANCE)
        assertEquals(viaMatrix.y, rotated.y, TOLERANCE)
        assertEquals(viaMatrix.z, rotated.z, TOLERANCE)
    }

    /** Composition order is the only thing that can be wrong, so it is asserted against the
     * matrices rather than trusted. */
    @Test
    fun composingTwoRotationsAgreesWithComposingTheirMatrices() {
        val first = Quat.fromAxisAngle(Vec3(0f, 1f, 0f), 0.7f)
        val second = Quat.fromAxisAngle(Vec3(1f, 0f, 0f), -0.3f)
        val point = Vec4(0.3f, 1.2f, -0.8f, 1f)

        val viaQuat = (first * second).toMat4().transformPosition(point)
        val viaMatrices = (first.toMat4() * second.toMat4()).transformPosition(point)

        assertEquals(viaMatrices.x, viaQuat.x, TOLERANCE)
        assertEquals(viaMatrices.y, viaQuat.y, TOLERANCE)
        assertEquals(viaMatrices.z, viaQuat.z, TOLERANCE)
    }

    @Test
    fun aRotationFollowedByItsInverseIsANoOp() {
        val rotation = Quat.fromAxisAngle(Vec3(0.3f, 1f, -0.2f), 1.1f)
        val undone = rotation * assertNotNull(rotation.inverse())
        val point = Vec3(2f, -1f, 0.5f)
        val result = undone.rotate(point)

        assertEquals(point.x, result.x, TOLERANCE)
        assertEquals(point.y, result.y, TOLERANCE)
        assertEquals(point.z, result.z, TOLERANCE)
    }

    @Test
    fun aZeroQuaternionHasNoInverse() {
        assertNull(Quat(0f, 0f, 0f, 0f).inverse())
    }

    @Test
    fun eulerAnglesRoundTrip() {
        val euler = Vec3(0.4f, -0.9f, 0.25f)
        val returned = Quat.fromEuler(euler).toEuler()

        assertEquals(euler.x, returned.x, TOLERANCE)
        assertEquals(euler.y, returned.y, TOLERANCE)
        assertEquals(euler.z, returned.z, TOLERANCE)
    }

    /**
     * fromEuler must compose in the order `Transform` composes its own matrix, or a rotation
     * authored in the inspector and the same rotation as a quaternion would disagree.
     */
    @Test
    fun fromEulerMatchesTheTransformMatrixOrder() {
        val euler = Vec3(0.3f, 0.8f, -0.5f)
        val point = Vec4(1f, 2f, -3f, 1f)

        val viaQuat = Quat.fromEuler(euler).toMat4().transformPosition(point)
        val viaTransformOrder = Mat4().rotateZ(euler.z).rotateY(euler.y).rotateX(euler.x)
            .transformPosition(point)

        assertEquals(viaTransformOrder.x, viaQuat.x, TOLERANCE)
        assertEquals(viaTransformOrder.y, viaQuat.y, TOLERANCE)
        assertEquals(viaTransformOrder.z, viaQuat.z, TOLERANCE)
    }

    /** At the pole, yaw and roll describe the same axis; the round trip may pick a different
     * triple, but it must describe the same rotation. */
    @Test
    fun gimbalLockStillDescribesTheSameRotation() {
        val locked = Quat.fromEuler(Vec3(QUARTER, 0.6f, 0f))
        val rebuilt = Quat.fromEuler(locked.toEuler())
        val point = Vec3(1f, 0f, 0f)

        val original = locked.rotate(point)
        val returned = rebuilt.rotate(point)
        assertEquals(original.x, returned.x, TOLERANCE)
        assertEquals(original.y, returned.y, TOLERANCE)
        assertEquals(original.z, returned.z, TOLERANCE)
    }
}
