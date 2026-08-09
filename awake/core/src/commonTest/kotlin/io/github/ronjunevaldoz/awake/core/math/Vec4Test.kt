// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class Vec4Test {

    @Test
    fun dotProductOfKnownVectors() {
        assertEquals(70f, Vec4(1f, 2f, 3f, 4f).dot(Vec4(5f, 6f, 7f, 8f)), TOLERANCE)
    }

    @Test
    fun dotProductIncludesTheWComponent() {
        // Identical xyz, differing only in w -- proves w is not silently dropped.
        assertEquals(0f, Vec4(0f, 0f, 0f, 1f).dot(Vec4(0f, 0f, 0f, 0f)), TOLERANCE)
        assertEquals(1f, Vec4(0f, 0f, 0f, 1f).dot(Vec4(0f, 0f, 0f, 1f)), TOLERANCE)
    }

    @Test
    fun plusAndMinusAreComponentwise() {
        val a = Vec4(1f, 2f, 3f, 4f)
        val b = Vec4(0.5f, -1f, 10f, 0f)

        assertVec4(Vec4(1.5f, 1f, 13f, 4f), a + b)
        assertVec4(Vec4(0.5f, 3f, -7f, 4f), a - b)
        assertVec4(Vec4(0f, 0f, 0f, 0f), a - a)
    }

    @Test
    fun timesScalarScalesEveryComponentIncludingW() {
        assertVec4(Vec4(2f, 4f, 6f, 8f), Vec4(1f, 2f, 3f, 4f) * 2f)
        assertVec4(Vec4(0f, 0f, 0f, 0f), Vec4(1f, 2f, 3f, 4f) * 0f)
    }

    @Test
    fun length3IgnoresW() {
        assertEquals(5f, Vec4(3f, 4f, 0f, 999f).length3(), TOLERANCE)
        assertEquals(13f, Vec4(3f, 4f, 12f, 0f).length3(), TOLERANCE)
    }

    @Test
    fun setOperatorReplacesEveryComponent() {
        val v = Vec4(1f, 1f, 1f, 1f)

        v[2f, 3f, 4f] = 5f

        assertVec4(Vec4(2f, 3f, 4f, 5f), v)
    }

    @Test
    fun timesMat4WithTheIdentityIsANoOp() {
        assertVec4(Vec4(1f, 2f, 3f, 4f), Vec4(1f, 2f, 3f, 4f) * Mat4())
    }

    /**
     * Characterises a real defect, it is not an endorsement. [Vec4.times] (and its twin
     * [Mat4.transformPosition]) contract `m[row][col]` against the *row* index of the vector,
     * i.e. they compute `M^T * v`, not the `M * v` the shaders perform on the very same
     * [Mat4.data] bytes (see [applyToColumnVector]'s doc comment). The visible consequence: a
     * translation matrix does not translate -- its translation column leaks into `w` instead.
     */
    @Test
    fun timesMat4ComputesTheTransposeNotWhatTheShaderDoes() {
        val translate = Mat4().translate(5f, 0f, 0f)

        // What the GPU does with these exact 16 floats:
        assertVec4(Vec4(5f, 0f, 0f, 1f), translate.applyToColumnVector(0f, 0f, 0f))
        // What Vec4.times does with them: the origin never moves...
        assertVec4(Vec4(0f, 0f, 0f, 1f), Vec4(0f, 0f, 0f, 1f) * translate)
        // ...and the translation turns up in w instead.
        assertVec4(Vec4(1f, 0f, 0f, 5f), Vec4(1f, 0f, 0f, 0f) * translate)
    }

    @Test
    fun transformPositionIsTheSameFormulaAsTheTimesOperator() {
        val m = Mat4().translate(1f, 2f, 3f).rotateY(0.6f).scale(1.5f)
        val v = Vec4(0.25f, -3f, 7f, 1f)

        assertVec4(v * m, m.transformPosition(v))
    }

    @Test
    fun transformToPositionWritesTheResultBackIntoItsArgument() {
        val m = Mat4().translate(1f, 2f, 3f)
        val v = Vec4(0.25f, -3f, 7f, 1f)
        val expected = v * m

        m.transformToPosition(v)

        assertVec4(expected, v)
    }

    /**
     * Characterises a real defect, it is not an endorsement -- see the @Ignore'd test below.
     * Because [Mat4.times] is the reversed/transposed product while [Vec4.times] applies the
     * transpose too, the two do not compose: transforming through a pre-multiplied matrix gives
     * a different answer than transforming through its factors one at a time.
     */
    @Test
    fun timesMat4IsCurrentlyNotAssociativeWithMat4Times() {
        val translate = Mat4().translate(5f, 0f, 0f)
        val scale = Mat4().scale(2f)
        val p = Vec4(1f, 0f, 0f, 1f)

        assertVec4(Vec4(2f, 0f, 0f, 6f), (p * translate) * scale)
        assertVec4(Vec4(2f, 0f, 0f, 11f), p * (translate * scale))
    }

    // DEFECT: Vec4.times(Mat4)/Mat4.transformPosition compute M^T * v while Mat4.times composes
    // matrices for the M * v convention the shaders use, so (p * A) * B != p * (A * B). Any
    // CPU-side projection through a composed MVP is wrong today. Un-@Ignore once
    // vector.kt/matrix.kt agree on one convention.
    @Ignore
    @Test
    fun timesMat4ShouldAssociateWithMat4Times() {
        val translate = Mat4().translate(5f, 0f, 0f)
        val scale = Mat4().scale(2f)
        val p = Vec4(1f, 0f, 0f, 1f)

        assertVec4((p * translate) * scale, p * (translate * scale))
    }

    @Test
    fun pixelCoordsMapsTheNdcCentreToTheScreenCentre() {
        val centre = Vec4(0f, 0f, 0f, 1f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(400f, centre.x, TOLERANCE)
        assertEquals(300f, centre.y, TOLERANCE)
    }

    @Test
    fun pixelCoordsIsYDownSoNdcTopLeftBecomesPixelZeroZero() {
        val topLeft = Vec4(-1f, 1f, 0f, 1f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)
        val bottomRight = Vec4(1f, -1f, 0f, 1f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(0f, topLeft.x, TOLERANCE)
        assertEquals(0f, topLeft.y, TOLERANCE)
        assertEquals(800f, bottomRight.x, TOLERANCE)
        assertEquals(600f, bottomRight.y, TOLERANCE)
    }

    @Test
    fun pixelCoordsPerformsThePerspectiveDivide() {
        // Clip (2, 2, _, 2) is the same NDC point as (1, 1, _, 1) -- the far corner.
        val divided = Vec4(2f, 2f, 0f, 2f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(800f, divided.x, TOLERANCE)
        assertEquals(0f, divided.y, TOLERANCE)
    }

    @Test
    fun pixelCoordsTruncatesTowardZeroBecauseItReturnsIntPixels() {
        // NDC x = -0.9995 -> 0.5 * 0.0005 * 800 = 0.2 px, truncated to 0.
        val nearLeftEdge = Vec4(-0.9995f, 0f, 0f, 1f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(0f, nearLeftEdge.x, TOLERANCE)
    }

    @Test
    fun makePixelCoordsMutatesInPlaceAndMapsTheNdcCentreToTheViewportCentre() {
        val v = Vec4(0f, 0f, 0f, 1f)

        v.makePixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(400f, v.x, TOLERANCE)
        assertEquals(300f, v.y, TOLERANCE)
        assertEquals(0.5f, v.z, TOLERANCE) // GL-style depth remap: [-1, 1] -> [0, 1].
        assertEquals(1f, v.w, TOLERANCE)
    }

    /**
     * [Vec4.makePixelCoords] is Y-*up* (`y = 0.5 + 0.5 * y`, no flip) while [Vec4.pixelCoords]
     * is Y-*down* (`0.5 * (1 - ndcY)`). Both are defensible alone; together they are a trap, so
     * the difference is asserted rather than left for a caller to discover.
     */
    @Test
    fun makePixelCoordsIsYUpUnlikePixelCoords() {
        val top = Vec4(0f, 1f, 0f, 1f)
        top.makePixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(600f, top.y, TOLERANCE)
        assertEquals(0f, Vec4(0f, 1f, 0f, 1f).pixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT).y, TOLERANCE)
    }

    @Test
    fun makePixelCoordsPerformsThePerspectiveDivideFirst() {
        val v = Vec4(2f, 0f, 2f, 2f)

        v.makePixelCoords(SCREEN_WIDTH, SCREEN_HEIGHT)

        assertEquals(800f, v.x, TOLERANCE) // ndc x = 1 -> full width
        assertEquals(300f, v.y, TOLERANCE)
        assertEquals(1f, v.z, TOLERANCE) // ndc z = 1 -> far end of [0, 1]
    }

    private companion object {
        const val TOLERANCE = 0.0001f
        const val SCREEN_WIDTH = 800
        const val SCREEN_HEIGHT = 600
    }
}
