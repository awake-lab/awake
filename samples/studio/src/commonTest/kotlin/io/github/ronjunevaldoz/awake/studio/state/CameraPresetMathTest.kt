// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraPresetMathTest {

    private val target = Vec3(0f, 0f, 0f)
    private val distance = 5f

    /** A lens whose `eye` is [eye], so `applyPreset` writes the position under test into it. */
    private fun camera(eye: Vec3) = Camera.perspective(eye = eye)

    @Test
    fun frontPresetSitsOnTheZAxisAtDistance() {
        val eye = Vec3()
        val state = StudioContract.CameraState(mode = StudioContract.CameraPresetMode.Front, distance = distance)

        CameraPresetMath.applyPreset(state, target, camera(eye), Vec3())

        assertEquals(0f, eye.x, TOLERANCE)
        assertEquals(0f, eye.y, TOLERANCE)
        assertEquals(distance, abs(eye.z), TOLERANCE)
    }

    @Test
    fun topPresetSitsOnTheYAxisAtDistance() {
        val eye = Vec3()
        val state = StudioContract.CameraState(mode = StudioContract.CameraPresetMode.Top, distance = distance)

        CameraPresetMath.applyPreset(state, target, camera(eye), Vec3())

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

        CameraPresetMath.applyPreset(state, target, camera(eye), Vec3())

        assertEquals(distance, abs(eye.x), TOLERANCE)
        assertEquals(0f, eye.y, TOLERANCE)
        assertEquals(0f, eye.z, TOLERANCE)
    }

    @Test
    fun `projection state reaches the lens, sized to match the perspective framing`() {
        val lens = camera(Vec3())
        val perspective = StudioContract.CameraState(distance = distance)

        CameraPresetMath.applyPreset(perspective, target, lens, Vec3())
        assertEquals(Camera.Projection.Perspective, lens.projection)

        val ortho = perspective.copy(projection = StudioContract.Projection.Orthographic)
        CameraPresetMath.applyPreset(ortho, target, lens, Vec3())

        assertEquals(Camera.Projection.Orthographic, lens.projection)
        // Half-height = distance * tan(fovY / 2): the world extent the perspective lens
        // already covers at the orbit target, so toggling doesn't resize the subject.
        assertEquals(distance * tan(lens.fovYRadians / 2f), lens.orthoHalfHeight, TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-4f
    }
}
