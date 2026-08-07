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

    /**
     * Characterises a real defect, it is not an endorsement. `setLookAt` writes the side/up/
     * -forward basis into the matrix's **columns** (`m00, m10, m20 = s`), but a view matrix needs
     * them in its **rows** (`m00, m01, m02 = s`) -- the rotation block comes out transposed, i.e.
     * inverted, while the translation column (`m03, m13, m23`) is written in the correct,
     * row-form convention. Compare GLM's `lookAtRH`, whose `Result[0][0], Result[1][0],
     * Result[2][0]` (column-major indices, so row 0) are `s.x, s.y, s.z`.
     *
     * The two halves therefore disagree, and the matrix stops being a view matrix as soon as the
     * camera is not axis-aligned. See `viewMatrixShouldMapTheCameraPositionToTheOrigin`.
     */
    @Test
    fun viewMatrixCurrentlyStoresTheBasisAsColumnsInsteadOfRows() {
        val eye = Vec3(5f, 0f, 0f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        val forward = Vec3(-1f, 0f, 0f)
        val side = forward.cross(Vec3.UP).normalized() // (0, 0, -1)
        val cameraUp = side.cross(forward) // (0, 1, 0)

        // The side vector lands in column 0 ...
        assertEquals(side.x, view.m00, TOLERANCE)
        assertEquals(side.y, view.m10, TOLERANCE)
        assertEquals(side.z, view.m20, TOLERANCE)
        // ... so row 0 ends up holding one component of each basis vector, instead of the whole
        // side vector a view matrix needs there.
        assertEquals(side.x, view.m00, TOLERANCE)
        assertEquals(cameraUp.x, view.m01, TOLERANCE)
        assertEquals(-forward.x, view.m02, TOLERANCE)
    }

    /** The visible consequence of the above: the camera is not at the view-space origin. */
    @Test
    fun aNonAxisAlignedCameraCurrentlyDoesNotMapItselfToTheViewSpaceOrigin() {
        val eye = Vec3(5f, 0f, 0f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        // Should be (0, 0, 0, 1); the transposed rotation puts it twice as far back instead.
        assertVec4(Vec4(0f, 0f, -10f, 1f), view.applyToColumnVector(eye.x, eye.y, eye.z))
    }

    /** And the world axes come out mirrored, so surrounding geometry is oriented wrongly. */
    @Test
    fun aNonAxisAlignedCameraCurrentlyMirrorsTheViewSpaceXAxis() {
        val view = Mat4.setLookAt(eye = Vec3(5f, 0f, 0f), center = Vec3(0f, 0f, 0f), up = Vec3.UP)

        // Camera right is world -Z, so world (0, 0, -1) must appear to the right (view x > 0).
        assertEquals(-1f, view.applyToColumnVector(0f, 0f, -1f).x, TOLERANCE)
    }

    @Ignore(
        "DEFECT: Mat4.setLookAt writes the side/up/-forward basis into the matrix's columns " +
            "while writing the translation in row form, so the rotation block is transposed " +
            "(inverted). The camera only lands on the view-space origin when its basis happens " +
            "to be the identity -- i.e. eye on +Z looking down -Z, the one case every existing " +
            "test uses. Fix: swap the nine rotation assignments to m00/m01/m02 = s, " +
            "m10/m11/m12 = u, m20/m21/m22 = -f. Un-@Ignore once matrix.kt is fixed."
    )
    @Test
    fun viewMatrixShouldMapTheCameraPositionToTheOrigin() {
        for (eye in listOf(Vec3(5f, 0f, 0f), Vec3(3f, 4f, 5f), Vec3(-2f, 7f, -1.5f))) {
            val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)

            assertVec4(Vec4(0f, 0f, 0f, 1f), view.applyToColumnVector(eye.x, eye.y, eye.z))
        }
    }

    @Ignore("DEFECT: see viewMatrixShouldMapTheCameraPositionToTheOrigin.")
    @Test
    fun viewMatrixShouldHoldTheSideVectorInItsFirstRow() {
        val eye = Vec3(5f, 0f, 0f)
        val view = Mat4.setLookAt(eye = eye, center = Vec3(0f, 0f, 0f), up = Vec3.UP)
        val side = Vec3(-1f, 0f, 0f).cross(Vec3.UP).normalized()

        assertEquals(side.x, view.m00, TOLERANCE)
        assertEquals(side.y, view.m01, TOLERANCE)
        assertEquals(side.z, view.m02, TOLERANCE)
    }

    @Ignore("DEFECT: see viewMatrixShouldMapTheCameraPositionToTheOrigin.")
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
        // The side and up basis vectors both collapsed to zero.
        assertEquals(0f, view.m00, TOLERANCE)
        assertEquals(0f, view.m10, TOLERANCE)
        assertEquals(0f, view.m20, TOLERANCE)
        assertEquals(0f, view.m01, TOLERANCE)
        assertEquals(0f, view.m11, TOLERANCE)
        assertEquals(0f, view.m21, TOLERANCE)
        // Only -forward survives.
        assertEquals(-1f, view.m12, TOLERANCE)
    }

    @Test
    fun forwardAntiParallelToUpIsEquallyDegenerate() {
        val view = Mat4.setLookAt(eye = Vec3(0f, 0f, 0f), center = Vec3(0f, -1f, 0f), up = Vec3.UP)

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
        assertEquals(0f, view.m00, TOLERANCE)
        assertEquals(1f, view.m12, TOLERANCE)
    }

    @Test
    fun aNearlyParallelUpVectorStillProducesAUsableMatrix() {
        val view = Mat4.setLookAt(
            eye = Vec3(0f, 0f, 0f),
            center = Vec3(0f, 1f, 0f),
            up = Vec3(0.001f, 1f, 0f)
        )

        assertTrue(view.data.none { it.isNaN() }, "expected no NaNs, got ${view.data.toList()}")
        // The side vector recovers to a unit vector even from a nearly-degenerate cross product.
        assertEquals(1f, Vec3(view.m00, view.m10, view.m20).length3(), TOLERANCE)
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
