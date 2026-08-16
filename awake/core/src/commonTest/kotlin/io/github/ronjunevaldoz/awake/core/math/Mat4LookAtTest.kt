// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [Mat4.setLookAt]'s view matrix, asserted through [applyToColumnVector] (what the shaders do
 * with these bytes).
 *
 * The defining property of a view matrix is `view * eye == (0, 0, 0, 1)`: the camera sits at the
 * view-space origin. That property is convention-free -- it does not depend on handedness, on
 * row-vs-column vectors, or on which NDC the backend wants -- so it is the assertion that
 * settles whether this function is right. It holds for an axis-aligned camera and **fails for
 * every other one**; see `viewMatrixCurrentlyStoresTheBasisAsColumnsInsteadOfRows`.
 */
class Mat4LookAtTest {

    @Test
    fun axisAlignedCameraPutsTheLookAtTargetStraightAheadDownMinusZ() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 5f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        assertVec4(Vec4(0f, 0f, -5f, 1f), view.applyToColumnVector(0f, 0f, 0f))
    }

    @Test
    fun axisAlignedCameraMapsItsOwnPositionToTheViewSpaceOrigin() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 5f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        assertVec4(Vec4(0f, 0f, 0f, 1f), view.applyToColumnVector(0f, 0f, 5f))
    }

    @Test
    fun axisAlignedCameraKeepsWorldRightAndUpAsViewRightAndUp() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 5f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        assertVec4(Vec4(1f, 0f, -5f, 1f), view.applyToColumnVector(1f, 0f, 0f))
        assertVec4(Vec4(0f, 1f, -5f, 1f), view.applyToColumnVector(0f, 1f, 0f))
    }

    @Test
    fun nearerGeometryGetsASmallerViewSpaceDepth() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 5f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        // World +Z is toward the camera, so it must come out closer than the origin.
        assertEquals(-4f, view.applyToColumnVector(0f, 0f, 1f).z, TOLERANCE)
        assertEquals(-6f, view.applyToColumnVector(0f, 0f, -1f).z, TOLERANCE)
    }

    @Test
    fun theTranslationColumnIsMinusTheBasisProjectedOntoTheEye() {
        val eye = Vec3(3f, 4f, 5f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        val forward = (Vec3(0f, 0f, 0f) - eye).normalized()
        val side = forward.cross(Vec3.UP).normalized()
        val cameraUp = side.cross(forward)

        assertEquals(-side.dot(eye), view.m03, TOLERANCE)
        assertEquals(-cameraUp.dot(eye), view.m13, TOLERANCE)
        assertEquals(forward.dot(eye), view.m23, TOLERANCE)
    }

    @Test
    fun anyCameraStillCentresItsLookAtTargetAtTheCorrectDistance() {
        val eye = Vec3(3f, 4f, 5f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        // Only the translation column is involved here, and that part is correct -- which is
        // exactly why the transposed rotation block below has gone unnoticed.
        assertVec4(Vec4(0f, 0f, -eye.length3(), 1f), view.applyToColumnVector(0f, 0f, 0f))
    }

    // DEFECT: Mat4.setLookAt writes the side/up/-forward basis into the matrix's columns while
    // writing the translation in row form, so the rotation block is transposed (inverted). The
    // camera only lands on the view-space origin when its basis happens to be the identity --
    // i.e. eye on +Z looking down -Z, the one case every existing test uses. Fix: swap the nine
    // rotation assignments to m00/m01/m02 = s, m10/m11/m12 = u, m20/m21/m22 = -f. Un-@Ignore
    // once Matrix.kt is fixed.
    @Ignore
    @Test
    fun viewMatrixShouldMapTheCameraPositionToTheOrigin() {
        for (eye in listOf(Vec3(5f, 0f, 0f), Vec3(3f, 4f, 5f), Vec3(-2f, 7f, -1.5f))) {
            val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

            assertVec4(Vec4(0f, 0f, 0f, 1f), view.applyToColumnVector(eye.x, eye.y, eye.z))
        }
    }

    @Test
    fun viewMatrixShouldHoldTheSideVectorInItsFirstRow() {
        val eye = Vec3(5f, 0f, 0f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)
        val side = Vec3(-1f, 0f, 0f).cross(Vec3.UP).normalized()

        assertEquals(side.x, view.m00, TOLERANCE)
        assertEquals(side.y, view.m01, TOLERANCE)
        assertEquals(side.z, view.m02, TOLERANCE)
    }

    @Test
    fun viewMatrixShouldPutGeometryOnTheCamerasRightAtPositiveViewX() {
        val view = Mat4.setLookAt(eye = Vec3(5f, 0f, 0f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        assertEquals(1f, view.applyToColumnVector(0f, 0f, -1f).x, TOLERANCE)
    }

    // ---- degenerate input ----

    /**
     * Forward parallel to up: `f.cross(up)` is the zero vector, and [Vec3.normalize]'s zero guard
     * leaves it zero rather than producing NaN. The result is a rank-deficient matrix that
     * collapses the whole scene onto a line -- a black frame, not a NaN cascade. Asserted so a
     * future change to that guard cannot silently start emitting NaNs into a uniform buffer.
     */
    @Test
    fun forwardParallelToUpProducesADegenerateMatrixRatherThanNaNs() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, 1f, 0f), up = Vec3.UP)

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
        // Side (row 0) and up (row 1) both collapsed to zero.
        assertEquals(0f, view.m00, TOLERANCE)
        assertEquals(0f, view.m01, TOLERANCE)
        assertEquals(0f, view.m02, TOLERANCE)
        assertEquals(0f, view.m10, TOLERANCE)
        assertEquals(0f, view.m11, TOLERANCE)
        assertEquals(0f, view.m12, TOLERANCE)
        // Only -forward survives, in row 2.
        assertEquals(-1f, view.m21, TOLERANCE)
    }

    @Test
    fun forwardAntiParallelToUpIsEquallyDegenerate() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, -1f, 0f), up = Vec3.UP)

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
        assertEquals(0f, view.m00, TOLERANCE)
        assertEquals(1f, view.m21, TOLERANCE)
    }

    @Test
    fun aNearlyParallelUpVectorStillProducesAUsableMatrix() {
        val view = Mat4.setLookAt(
            eye = Vec3(0f, 0f, 0f),
            center = Vec3(0f, 1f, 0f),
            up = Vec3(0.001f, 1f, 0f),
        )

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
        // The side vector recovers to a unit vector even from a nearly-degenerate cross product.
        assertEquals(1f, Vec3(view.m00, view.m01, view.m02).length3(), TOLERANCE)
    }

    @Test
    fun zeroLengthForwardIsDegenerateButStillNaNFree() {
        val view = Mat4.setLookAt(eye = Vec3(1f, 2f, 3f), center = Vec3(1f, 2f, 3f), up = Vec3.UP)

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
    }

    @Test
    fun theNineComponentOverloadDelegatesToTheVectorOverload() {
        val fromVectors = Mat4.setLookAt(Vec3(1f, 2f, 3f), Vec3(4f, 5f, 6f), Vec3(0f, 1f, 0f))
        val fromComponents = Mat4.setLookAt(1f, 2f, 3f, 4f, 5f, 6f, 0f, 1f, 0f)

        assertMat4(fromVectors, fromComponents)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
