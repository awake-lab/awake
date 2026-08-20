// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.times
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class MatrixTest {

    @Test
    fun setEulerTRSMatchesIndividualMatrixMultiplications() {
        val px = 3.5f
        val py = -2.1f
        val pz = 10.4f
        val rx = 0.45f
        val ry = -0.82f
        val rz = 1.23f
        val sx = 2.0f
        val sy = 0.5f
        val sz = 3.0f

        val chained = Mat4()
            .translate(px, py, pz)
            .rotateZ(rz)
            .rotateY(ry)
            .rotateX(rx)
            .scale(sx, sy, sz)

        val fast = Mat4().setEulerTRS(px, py, pz, rx, ry, rz, sx, sy, sz)

        for (i in 0 until 16) {
            assertTrue(
                abs(chained.data[i] - fast.data[i]) < 1e-4f,
                "Mismatch at data index $i: expected ${chained.data[i]}, got ${fast.data[i]}",
            )
        }
    }

    @Test
    fun multiplyInPlaceMatchesTimesOperator() {
        val a = Mat4().setEulerTRS(1f, 2f, 3f, 0.1f, 0.2f, 0.3f, 1f, 1f, 1f)
        val b = Mat4().setEulerTRS(4f, 5f, 6f, -0.4f, 0.5f, -0.6f, 2f, 2f, 2f)

        val expected = a * b
        val target = Mat4()
        Mat4.multiplyInPlace(a, b, target)

        for (i in 0 until 16) {
            assertTrue(
                abs(expected.data[i] - target.data[i]) < 1e-4f,
                "Mismatch at index $i: expected ${expected.data[i]}, got ${target.data[i]}",
            )
        }
    }
}
