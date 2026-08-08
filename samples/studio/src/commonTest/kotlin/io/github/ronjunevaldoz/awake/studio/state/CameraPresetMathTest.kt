// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraPresetMathTest {

    private val target = Vec3(0f, 0f, 0f)
    private val distance = 5f

    @Test
    fun frontPresetSitsOnTheZAxisAtDistance() {
        val eye = Vec3()
        val state = StudioContract.CameraState(mode = StudioContract.CameraPresetMode.Front, distance = distance)

        CameraPresetMath.applyPreset(state, target, eye, Vec3(), Vec3())

        assertEquals(0f, eye.x, TOLERANCE)
        assertEquals(0f, eye.y, TOLERANCE)
        assertEquals(distance, abs(eye.z), TOLERANCE)
    }

    @Test
    fun topPresetSitsOnTheYAxisAtDistance() {
        val eye = Vec3()
        val state = StudioContract.CameraState(mode = StudioContract.CameraPresetMode.Top, distance = distance)

        CameraPresetMath.applyPreset(state, target, eye, Vec3(), Vec3())

        assertEquals(0f, eye.x, TOLERANCE)
        assertEquals(distance, eye.y, TOLERANCE)
        assertEquals(0f, eye.z, TOLERANCE)
    }

    @Test
    fun orbitAtNinetyDegreeYawSitsOnTheXAxis() {
        val eye = Vec3()
        val state = StudioContract.CameraState(
            mode = StudioContract.CameraPresetMode.Orbit,
            yaw = (PI / 2.0).toFloat(),
            pitch = 0f,
            distance = distance,
        )

        CameraPresetMath.applyPreset(state, target, eye, Vec3(), Vec3())

        assertEquals(distance, abs(eye.x), TOLERANCE)
        assertEquals(0f, eye.y, TOLERANCE)
        assertEquals(0f, eye.z, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-4f
    }
}
