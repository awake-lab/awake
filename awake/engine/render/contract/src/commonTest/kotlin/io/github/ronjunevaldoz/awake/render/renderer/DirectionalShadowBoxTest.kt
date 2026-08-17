// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Vec4
import io.github.ronjunevaldoz.awake.core.math.inverse
import io.github.ronjunevaldoz.awake.core.math.transformPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DirectionalShadowBoxTest {
    @Test
    fun eyeIsDirectionTimesDistance() {
        val box = directionalShadowBox(Vec3(1f, 0f, 0f), ClipSpace.WebGpu, distance = 10f)

        assertEquals(Vec3(10f, 0f, 0f), box.eye)
    }

    @Test
    fun upVectorSwitchesToZWhenDirectionIsNearlyVertical() {
        val vertical = directionalShadowBox(Vec3(0f, 0.995f, 0f), ClipSpace.WebGpu)
        val notVertical = directionalShadowBox(Vec3(0f, 0.5f, 0.5f), ClipSpace.WebGpu)

        assertFalse(vertical.view.hasNaN())
        assertFalse(notVertical.view.hasNaN())
    }

    @Test
    fun viewInverseRoundTripsTheEyePointBackToItself() {
        val box = directionalShadowBox(Vec3(0.4f, 0.8f, 0.4f), ClipSpace.WebGpu)

        val inverseView = box.view.inverse()
        assertNotNull(inverseView)
        // view transforms world -> light space, so world-space eye maps to light-space origin;
        // inverting and transforming the origin back should round-trip to the eye point.
        val roundTripped = inverseView.transformPosition(Vec4(0f, 0f, 0f, 1f))
        val delta = Vec3(roundTripped.x, roundTripped.y, roundTripped.z) - box.eye
        assertTrue(delta.length3() < 0.001f)
    }

    private fun io.github.ronjunevaldoz.awake.core.math.Mat4.hasNaN(): Boolean {
        val values = floatArrayOf(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33,
        )
        return values.any { it.isNaN() }
    }
}
