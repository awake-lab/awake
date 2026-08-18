// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Vec4
import io.github.ronjunevaldoz.awake.core.math.transformPosition
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SkyboxUniformsTest {
    private val camera = Camera(
        eye = Vec3(0f, 2f, 6f),
        center = Vec3.ZERO,
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (kotlin.math.PI / 4.0).toFloat(),
        near = 0.1f,
        far = 100f,
    )

    private fun mat4Of(values: FloatArray) = Mat4().also { values.copyInto(it.data) }

    @Test
    fun blockIsExactlyTheSizeBothPipelinesAllocate() {
        val floats = skyboxUniformFloats(
            camera.viewProjectionMatrix(16f / 9f, ClipSpace.Vulkan),
            camera.eye,
            Vec3(0.4f, 0.8f, 0.4f),
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(0f, 0f, 1f),
        )

        assertNotNull(floats)
        assertEquals(SkyboxUniformLayout.total, floats.size)
    }

    @Test
    fun cameraAndSunLandAtTheirDeclaredOffsets() {
        val sun = Vec3(0.4f, 0.8f, 0.4f)
        val floats = assertNotNull(
            skyboxUniformFloats(
                camera.viewProjectionMatrix(1f, ClipSpace.Vulkan),
                camera.eye,
                sun,
                floatArrayOf(1f, 0f, 0f),
                floatArrayOf(0f, 0f, 1f),
            ),
        )

        val eyeAt = SkyboxUniformLayout.offsetOf(SkyboxUniformLayout.fields[1])
        val sunAt = SkyboxUniformLayout.offsetOf(SkyboxUniformLayout.fields[2])
        assertEquals(camera.eye.x, floats[eyeAt])
        assertEquals(camera.eye.z, floats[eyeAt + 2])
        assertEquals(sun.y, floats[sunAt + 1])
    }

    /** The whole point of the block: the shader unprojects a far-plane NDC point with the
     * matrix in slot 0, so that matrix must actually invert the frame's view-projection. */
    @Test
    fun inverseMatrixRoundTripsAWorldPointThroughTheViewProjection() {
        val viewProjection = camera.viewProjectionMatrix(1f, ClipSpace.Vulkan)
        val floats = assertNotNull(
            skyboxUniformFloats(viewProjection, camera.eye, Vec3(0f, 1f, 0f), floatArrayOf(0f, 0f, 0f), floatArrayOf(0f, 0f, 0f)),
        )
        val inverse = mat4Of(floats.copyOf(16))

        val world = Vec4(1f, 0.5f, -2f, 1f)
        val clip = viewProjection.transformPosition(world)
        val back = inverse.transformPosition(clip)

        assertTrue(abs(back.x / back.w - world.x) < 0.001f, "x round-trip: ${back.x / back.w}")
        assertTrue(abs(back.y / back.w - world.y) < 0.001f, "y round-trip: ${back.y / back.w}")
        assertTrue(abs(back.z / back.w - world.z) < 0.001f, "z round-trip: ${back.z / back.w}")
    }

    @Test
    fun singularViewProjectionYieldsNoBlockRatherThanGarbage() {
        assertNull(
            skyboxUniformFloats(
                mat4Of(FloatArray(16)),
                camera.eye,
                Vec3(0f, 1f, 0f),
                floatArrayOf(0f, 0f, 0f),
                floatArrayOf(0f, 0f, 0f),
            ),
        )
    }
}
