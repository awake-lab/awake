// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

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
     * [Vec4.times] must do to these 16 floats exactly what the GPU does with them.
     *
     * This once characterised the opposite: the operator contracted `m[row][col]` against the
     * vector's ROW index, i.e. computed `M^T * v`, so a translation matrix did not translate --
     * its translation column leaked into `w`. Nothing on the render path noticed, because the
     * CPU never transforms a point there; it surfaced when a bounding box and a gizmo handle
     * needed transforming on the CPU.
     */
    @Test
    fun timesMat4MatchesWhatTheShaderDoesWithTheSameBytes() {
        val translate = Mat4().translate(5f, 0f, 0f)

        assertVec4(translate.applyToColumnVector(0f, 0f, 0f), Vec4(0f, 0f, 0f, 1f) * translate)
        assertVec4(Vec4(5f, 0f, 0f, 1f), Vec4(0f, 0f, 0f, 1f) * translate)
        assertVec4(Vec4(6f, 0f, 0f, 1f), Vec4(1f, 0f, 0f, 1f) * translate)
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

    /** The defect this once characterised is fixed: vector.kt and matrix.kt now agree on the
     * `M * v` convention the shaders use, so transforming through a composed matrix and through
     * its factors give the same answer. */
    @Test
    fun timesMat4AssociatesWithMat4Times() {
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
