// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuatTest {
    @Test
    fun identityQuatProducesIdentityMatrix() {
        val identity = Quat.IDENTITY.toMat4()

        assertEquals(
            listOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f,
            ),
            identity.data.toList(),
        )
    }

    @Test
    fun ninetyDegreeYRotationMatchesKnownMatrix() {
        // 90 degrees about Y: x=0, y=sin(45), z=0, w=cos(45).
        val half = sqrt(2f) / 2f
        val rotation = Quat(0f, half, 0f, half).toMat4()

        assertApprox(0f, rotation.m00)
        assertApprox(-1f, rotation.m20)
        assertApprox(1f, rotation.m11)
        assertApprox(1f, rotation.m02)
        assertApprox(0f, rotation.m22)
    }

    /** Read through the conventional column-vector product the shaders perform, so these assert
     * where a vertex actually ends up rather than which matrix entry holds which sign. */
    @Test
    fun ninetyDegreeRotationsAboutEachAxisMoveTheBasisVectorsRightHanded() {
        val half = sqrt(2f) / 2f

        assertVec4(Vec4(0f, 0f, 1f, 1f), Quat(half, 0f, 0f, half).toMat4().applyToColumnVector(0f, 1f, 0f))
        assertVec4(Vec4(1f, 0f, 0f, 1f), Quat(0f, half, 0f, half).toMat4().applyToColumnVector(0f, 0f, 1f))
        assertVec4(Vec4(0f, 1f, 0f, 1f), Quat(0f, 0f, half, half).toMat4().applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun toMat4LeavesTheRotationAxisItselfUnmoved() {
        val half = sqrt(2f) / 2f

        assertVec4(Vec4(0f, 1f, 0f, 1f), Quat(0f, half, 0f, half).toMat4().applyToColumnVector(0f, 1f, 0f))
    }

    @Test
    fun toMat4ProducesAnOrthonormalRotationThatPreservesLength() {
        val q = Quat(0.2f, -0.4f, 0.5f, 0.7416198f) // already unit length
        val rotated = q.toMat4().applyToColumnVector(1f, 2f, 3f)

        assertApprox(Vec3(1f, 2f, 3f).length3(), rotated.length3())
        assertApprox(1f, rotated.w)
    }

    @Test
    fun toMat4NeverTouchesTheTranslationColumn() {
        val rotation = Quat(0.2f, -0.4f, 0.5f, 0.7416198f).toMat4()

        assertApprox(0f, rotation.m03)
        assertApprox(0f, rotation.m13)
        assertApprox(0f, rotation.m23)
        assertApprox(1f, rotation.m33)
    }

    @Test
    fun nlerpAtZeroReturnsFirstQuaternion() {
        val a = Quat(0f, 0.6f, 0f, 0.8f)
        val b = Quat(0.6f, 0f, 0f, 0.8f)

        val result = Quat.nlerp(a, b, 0f)

        assertApprox(a.x, result.x)
        assertApprox(a.y, result.y)
        assertApprox(a.z, result.z)
        assertApprox(a.w, result.w)
    }

    @Test
    fun nlerpAtOneReturnsSecondQuaternion() {
        val a = Quat(0f, 0.6f, 0f, 0.8f)
        val b = Quat(0.6f, 0f, 0f, 0.8f)

        val result = Quat.nlerp(a, b, 1f)

        assertApprox(b.x, result.x)
        assertApprox(b.y, result.y)
        assertApprox(b.z, result.z)
        assertApprox(b.w, result.w)
    }

    @Test
    fun nlerpResultIsAlwaysNormalized() {
        val a = Quat(0f, 0f, 0f, 1f)
        val b = Quat(1f, 0f, 0f, 0f)

        val result = Quat.nlerp(a, b, 0.3f)
        val length = sqrt(result.x * result.x + result.y * result.y + result.z * result.z + result.w * result.w)

        assertApprox(1f, length)
    }

    @Test
    fun nlerpPicksShorterArcWhenDotProductIsNegative() {
        val a = Quat(0f, 0f, 0f, 1f)
        // -a, geometrically the same rotation but opposite sign -- naive lerp toward this
        // would spin the long way around; nlerp must flip it back first.
        val negatedA = Quat(0f, 0f, 0f, -1f)

        val result = Quat.nlerp(a, negatedA, 0.5f)

        assertApprox(1f, result.w)
    }

    private fun assertApprox(expected: Float, actual: Float, epsilon: Float = 1e-4f) {
        assertTrue(kotlin.math.abs(expected - actual) < epsilon, "expected $expected, got $actual")
    }
}
