// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The conventional column-vector product `M * v` -- exactly what the GPU performs
 * (`gl_Position = ubo.mvp * vec4(inPosition, 1.0)` in `debug_line.vert`,
 * `uniforms.mvp * vec4f(inPosition, 1.0)` in `triangle.wgsl`) once [Mat4.data] is uploaded raw
 * into a `mat4` uniform, since `data[col * 4 + row]` is precisely GLSL/WGSL's column-major
 * layout and `mRC` reads as `m[row][col]`.
 *
 * This lives in the tests rather than being reused from production code on purpose: `Mat4` has
 * no such function. Both [Vec4.times] and [Mat4.transformPosition] compute the *transpose*
 * (`M^T * v`) instead -- see `Vec4Test.vec4TimesMat4ComputesTheTransposeNotWhatTheShaderDoes`.
 * Every clip-space assertion in this package goes through this helper so it asserts what the
 * GPU will actually see.
 */
internal fun Mat4.applyToColumnVector(x: Float, y: Float, z: Float, w: Float = 1f): Vec4 = Vec4(
    m00 * x + m01 * y + m02 * z + m03 * w,
    m10 * x + m11 * y + m12 * z + m13 * w,
    m20 * x + m21 * y + m22 * z + m23 * w,
    m30 * x + m31 * y + m32 * z + m33 * w,
)

internal fun assertVec4(expected: Vec4, actual: Vec4, tolerance: Float = 0.0001f) {
    assertEquals(expected.x, actual.x, tolerance, "x")
    assertEquals(expected.y, actual.y, tolerance, "y")
    assertEquals(expected.z, actual.z, tolerance, "z")
    assertEquals(expected.w, actual.w, tolerance, "w")
}

internal fun assertMat4(expected: Mat4, actual: Mat4, tolerance: Float = 0.0001f) {
    for (index in 0 until 16) {
        assertEquals(expected.data[index], actual.data[index], tolerance, "data[$index]")
    }
}

/** [Mat4]'s algebra: storage layout, the elementary transforms, and the composition order. */
class Mat4Test {

    @Test
    fun freshMat4IsTheIdentity() {
        assertEquals(IDENTITY_DATA, Mat4().data.toList())
    }

    @Test
    fun identityResetsAPreviouslyMutatedMatrix() {
        val m = Mat4()
        m.m03 = 7f
        m.m21 = -3f

        m.identity()

        assertEquals(IDENTITY_DATA, m.data.toList())
    }

    @Test
    fun identityLeavesAColumnVectorUntouched() {
        assertVec4(Vec4(3f, -4f, 5f, 1f), Mat4().applyToColumnVector(3f, -4f, 5f))
    }

    /**
     * The load-bearing storage fact: `mRC` is `m[row][col]`, stored at `data[col * 4 + row]`.
     * That is GLSL/WGSL's column-major `mat4` layout, which is why [Mat4.data] can be memcpy'd
     * straight into a uniform buffer. Everything else in this file depends on this holding.
     */
    @Test
    fun mRcAccessorsAreRowColumnStoredColumnMajor() {
        val m = Mat4()
        m.m03 = 42f // row 0, column 3 -- the X translation slot.

        assertEquals(42f, m.data[3 * 4 + 0], TOLERANCE)
        assertEquals(42f, m.applyToColumnVector(0f, 0f, 0f).x, TOLERANCE)
    }

    @Test
    fun setCopiesEveryComponentWithoutAliasingTheSource() {
        val source = Mat4().translate(1f, 2f, 3f)
        val copy = Mat4().set(source)

        source.m03 = 99f

        assertEquals(1f, copy.m03, TOLERANCE)
        assertEquals(2f, copy.m13, TOLERANCE)
        assertEquals(3f, copy.m23, TOLERANCE)
    }

    @Test
    fun translateMovesTheOrigin() {
        val t = Mat4().translate(5f, -2f, 7f)

        assertVec4(Vec4(5f, -2f, 7f, 1f), t.applyToColumnVector(0f, 0f, 0f))
        assertVec4(Vec4(6f, -2f, 7f, 1f), t.applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun translateLeavesDirectionsAloneBecauseTheirWIsZero() {
        val t = Mat4().translate(5f, -2f, 7f)

        assertVec4(Vec4(1f, 0f, 0f, 0f), t.applyToColumnVector(1f, 0f, 0f, w = 0f))
    }

    @Test
    fun uniformScaleMultipliesEveryAxis() {
        val s = Mat4().scale(3f)

        assertVec4(Vec4(3f, 6f, 9f, 1f), s.applyToColumnVector(1f, 2f, 3f))
    }

    @Test
    fun nonUniformScaleAppliesPerAxisFactors() {
        val s = Mat4().scale(2f, -1f, 0.5f)

        assertVec4(Vec4(2f, -2f, 1.5f, 1f), s.applyToColumnVector(1f, 2f, 3f))
    }

    @Test
    fun rotateXByNinetyDegreesMapsPlusYToPlusZ() {
        val r = Mat4().rotateX(HALF_PI)

        assertVec4(Vec4(0f, 0f, 1f, 1f), r.applyToColumnVector(0f, 1f, 0f))
        assertVec4(Vec4(0f, -1f, 0f, 1f), r.applyToColumnVector(0f, 0f, 1f))
        assertVec4(Vec4(1f, 0f, 0f, 1f), r.applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun rotateYByNinetyDegreesMapsPlusXToMinusZ() {
        val r = Mat4().rotateY(HALF_PI)

        assertVec4(Vec4(0f, 0f, -1f, 1f), r.applyToColumnVector(1f, 0f, 0f))
        assertVec4(Vec4(1f, 0f, 0f, 1f), r.applyToColumnVector(0f, 0f, 1f))
        assertVec4(Vec4(0f, 1f, 0f, 1f), r.applyToColumnVector(0f, 1f, 0f))
    }

    @Test
    fun rotateZByNinetyDegreesMapsPlusXToPlusY() {
        val r = Mat4().rotateZ(HALF_PI)

        assertVec4(Vec4(0f, 1f, 0f, 1f), r.applyToColumnVector(1f, 0f, 0f))
        assertVec4(Vec4(-1f, 0f, 0f, 1f), r.applyToColumnVector(0f, 1f, 0f))
        assertVec4(Vec4(0f, 0f, 1f, 1f), r.applyToColumnVector(0f, 0f, 1f))
    }

    @Test
    fun rotationsPreserveLength() {
        val v = Vec3(1f, 2f, 3f)
        val rotated = Mat4().rotateX(0.4f).rotateY(-1.1f).rotateZ(2.3f).applyToColumnVector(v.x, v.y, v.z)

        assertEquals(v.length3(), rotated.length3(), TOLERANCE)
    }

    @Test
    fun rotateAboutAnArbitraryAxisNormalisesThatAxis() {
        val unit = Mat4().rotate(0.9f, Vec3(0f, 0f, 1f))
        val scaled = Mat4().rotate(0.9f, Vec3(0f, 0f, 25f))

        assertMat4(unit, scaled)
    }

    @Test
    fun rotateAboutZeroAngleIsTheIdentity() {
        assertMat4(Mat4(), Mat4().rotate(0f, Vec3(1f, 2f, 3f)))
    }

    /**
     * The convention this repo documents on [Mat4.times] and [Camera.viewProjectionMatrix]:
     * Kotlin's `A * B` computes the *conventional* `B * A`. Asserted concretely rather than
     * taken on trust, because every MVP composition in both render backends depends on it
     * (`drawCall.model * viewProjection` is meant to be `projection * view * model`).
     */
    @Test
    fun timesOperatorComputesTheConventionalReversedProduct() {
        val scale = Mat4().scale(2f)
        val translate = Mat4().translate(5f, 0f, 0f)

        // Kotlin `scale * translate` == conventional `translate * scale`: scale first (1 -> 2),
        // then translate (2 -> 7). The other order would give (1 -> 6 -> 12).
        assertVec4(Vec4(7f, 0f, 0f, 1f), (scale * translate).applyToColumnVector(1f, 0f, 0f))
        assertVec4(Vec4(12f, 0f, 0f, 1f), (translate * scale).applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun timesWithTheIdentityIsANoOpFromEitherSide() {
        val m = Mat4().translate(1f, 2f, 3f).rotateY(0.5f)

        assertMat4(m, m * Mat4())
        assertMat4(m, Mat4() * m)
    }

    @Test
    fun multiplyColumnMajorIsTheConventionalNonReversedProduct() {
        val scale = Mat4().scale(2f)
        val translate = Mat4().translate(5f, 0f, 0f)

        // multiplyColumnMajor(a, b) applies b first -- the mirror image of the `times` operator.
        assertMat4(scale * translate, Mat4.multiplyColumnMajor(translate, scale))
        assertMat4(translate * scale, Mat4.multiplyColumnMajor(scale, translate))
    }

    @Test
    fun chainedBuilderCallsApplyInReverseOfWritingOrder() {
        // `.translate(...).scale(...)` == conventional T * S, i.e. scale runs first.
        val chained = Mat4().translate(5f, 0f, 0f).scale(2f)

        assertVec4(Vec4(7f, 0f, 0f, 1f), chained.applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun fromTrsComposesTranslationRotationAndScale() {
        val trs = Mat4.fromTrs(
            translation = Vec3(10f, 0f, 0f),
            rotation = Quat(0f, 0f, sin(HALF_PI / 2f), cos(HALF_PI / 2f)), // 90 degrees about +Z
            scale = Vec3(2f, 2f, 2f),
        )

        // (1, 0, 0) -> scale -> (2, 0, 0) -> rotate 90 about Z -> (0, 2, 0) -> translate -> (10, 2, 0).
        assertVec4(Vec4(10f, 2f, 0f, 1f), trs.applyToColumnVector(1f, 0f, 0f))
    }

    @Test
    fun fromTrsWithIdentityRotationAndUnitScaleIsAPureTranslation() {
        val trs = Mat4.fromTrs(Vec3(1f, 2f, 3f), Quat.IDENTITY, Vec3(1f, 1f, 1f))

        assertMat4(Mat4().translate(1f, 2f, 3f), trs)
    }

    @Test
    fun rotateZAgreesWithTheEquivalentQuaternion() {
        val angle = 0.7f
        val fromQuat = Quat(0f, 0f, sin(angle / 2f), cos(angle / 2f)).toMat4()

        assertMat4(fromQuat, Mat4().rotateZ(angle))
    }

    /** Every rotation producer in this package builds the same, right-handed rotation. rotateX,
     * rotateY and the general rotate used to build the transpose (they rotated by `-angle`)
     * while rotateZ and Quat.toMat4 did not. */
    @Test
    fun rotateXAndRotateYMatchTheEquivalentQuaternion() {
        val angle = 0.7f

        assertMat4(Quat(sin(angle / 2f), 0f, 0f, cos(angle / 2f)).toMat4(), Mat4().rotateX(angle))
        assertMat4(Quat(0f, sin(angle / 2f), 0f, cos(angle / 2f)).toMat4(), Mat4().rotateY(angle))
    }

    /** Two public APIs for "rotate about +Z" must agree. */
    @Test
    fun rotateAboutAnAxisShouldMatchTheDedicatedPerAxisHelper() {
        assertMat4(Mat4().rotateX(0.7f), Mat4().rotate(0.7f, Vec3(1f, 0f, 0f)))
        assertMat4(Mat4().rotateY(0.7f), Mat4().rotate(0.7f, Vec3(0f, 1f, 0f)))
        assertMat4(Mat4().rotateZ(0.7f), Mat4().rotate(0.7f, Vec3(0f, 0f, 1f)))
    }

    /** Characterises a real defect, it is not an endorsement -- see the @Ignore'd test below. */
    @Test
    fun plusCurrentlyProducesNonsenseInsteadOfAnElementwiseSum() {
        val sum = Mat4() + Mat4()

        // An elementwise sum of two identities has 0 off the diagonal; this has 2 everywhere.
        assertEquals(2f, sum.m00, TOLERANCE)
        assertTrue(sum.data.all { it == 2f }, "expected every entry to be the buggy 2f, got ${sum.data.toList()}")
    }

    // DEFECT: Mat4.plus (matrix.kt:441) is a matrix-*multiply* loop with '+' substituted for
    // '*' -- it sums a[i,k] + b[k,j] over k instead of adding matching entries. Dead code today
    // (no callers). Un-@Ignore once it is fixed or deleted.
    @Ignore
    @Test
    fun plusShouldAddMatchingEntries() {
        val sum = Mat4().translate(1f, 2f, 3f) + Mat4().translate(10f, 20f, 30f)

        assertEquals(2f, sum.m00, TOLERANCE)
        assertEquals(0f, sum.m01, TOLERANCE)
        assertEquals(11f, sum.m03, TOLERANCE)
        assertEquals(22f, sum.m13, TOLERANCE)
        assertEquals(33f, sum.m23, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
        const val HALF_PI = (kotlin.math.PI / 2.0).toFloat()
        val IDENTITY_DATA = listOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
        )
    }
}
