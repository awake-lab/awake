// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraTest {

    /** eye at the origin looking down -Z with up = +Y is the one configuration where
     * `Mat4.setLookAt` produces an identity view matrix (forward/side/up all line up with
     * the world axes) -- see `setLookAt`'s math. That makes `view * projection` collapse to
     * exactly `projection`, so this is the one setup where `Camera`'s output can be
     * hand-verified against `Mat4.perspective` directly, without needing a GPU. */
    private fun identityViewCamera() = Camera(
        eye = Vec3(0f, 0f, 0f),
        center = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (60.0 * kotlin.math.PI / 180.0).toFloat(),
        near = 0.1f,
        far = 100f
    )

    @Test
    fun `viewProjectionMatrix matches raw perspective matrix when view is identity`() {
        val aspect = 16f / 9f
        val camera = identityViewCamera()
        val expected = Mat4.perspective(
            fovY = camera.fovYRadians,
            aspect = aspect,
            near = camera.near,
            far = camera.far,
            clipSpace = ClipSpace.WebGpu
        )

        val actual = camera.viewProjectionMatrix(aspect, ClipSpace.WebGpu)

        assertEquals(expected.data.toList(), actual.data.toList())
    }

    @Test
    fun `viewProjectionMatrix flips Y for Vulkan clip space`() {
        val aspect = 16f / 9f
        val camera = identityViewCamera()
        val unflipped = Mat4.perspective(
            fovY = camera.fovYRadians,
            aspect = aspect,
            near = camera.near,
            far = camera.far,
            clipSpace = ClipSpace.Vulkan
        )

        val actual = camera.viewProjectionMatrix(aspect, ClipSpace.Vulkan)

        assertEquals(-unflipped.m11, actual.m11)
        // Every other entry is untouched by the flip.
        assertEquals(unflipped.m00, actual.m00)
        assertEquals(unflipped.m22, actual.m22)
        assertEquals(unflipped.m23, actual.m23)
        assertEquals(unflipped.m32, actual.m32)
    }

    @Test
    fun `viewProjectionMatrix widens m00 as aspect ratio narrows`() {
        val camera = identityViewCamera()

        val wide = camera.viewProjectionMatrix(aspect = 2f, clipSpace = ClipSpace.Vulkan)
        val narrow = camera.viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.Vulkan)

        // m00 = scaleY / aspect -- a smaller aspect ratio must produce a larger m00.
        assert(narrow.m00 > wide.m00) { "expected narrow.m00 (${narrow.m00}) > wide.m00 (${wide.m00})" }
    }

    /** The flip asserted where it is actually observable: a point above the centre of the frame
     * comes out above the NDC centre for a WebGPU/OpenGL backend and below it for Vulkan. */
    @Test
    fun `a point above the centre lands on opposite NDC Y sides depending on the renderer's ClipSpace`() {
        val webGpuStyle = identityViewCamera().viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.WebGpu)
        val vulkanStyle = identityViewCamera().viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.Vulkan)

        val webGpu = webGpuStyle.ndc(0f, 0.5f, -1f)
        val vulkan = vulkanStyle.ndc(0f, 0.5f, -1f)

        // 60 degree fovY => m11 = 1 / tan(30 degrees) = 1.7320508; 0.5 * that = 0.8660254.
        assertEquals(0.8660254f, webGpu.y, TOLERANCE)
        assertEquals(-0.8660254f, vulkan.y, TOLERANCE)
    }

    /** Vulkan and WebGPU differ only in the Y flip -- their depth range is the same `[0, 1]`,
     * so switching between them must move nothing but Y. */
    @Test
    fun `the Vulkan Y flip leaves X and depth untouched`() {
        val webGpu = identityViewCamera().viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.WebGpu)
            .ndc(0.5f, 0.5f, -1f)
        val vulkan = identityViewCamera().viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.Vulkan)
            .ndc(0.5f, 0.5f, -1f)

        assertEquals(webGpu.x, vulkan.x, TOLERANCE)
        assertEquals(webGpu.z, vulkan.z, TOLERANCE)
    }

    /** The view half of `view * projection` really is applied, not just the projection: a camera
     * pulled back to +Z must push world geometry further down view-space -Z. */
    @Test
    fun `viewProjectionMatrix applies the camera translation before projecting`() {
        val pulledBack = Camera(
            eye = Vec3(0f, 0f, 5f),
            center = Vec3(0f, 0f, 0f),
            up = Vec3(0f, 1f, 0f),
            fovYRadians = (60.0 * kotlin.math.PI / 180.0).toFloat(),
            near = 0.1f,
            far = 100f
        )

        val clip = pulledBack.viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.WebGpu)
            .applyToColumnVector(0f, 0f, 0f)

        // The world origin is 5 units in front of the camera, so w (which is -z_view) is 5.
        assertEquals(5f, clip.w, TOLERANCE)
    }

    private fun Mat4.ndc(x: Float, y: Float, z: Float): Vec3 {
        val clip = applyToColumnVector(x, y, z)
        return Vec3(clip.x / clip.w, clip.y / clip.w, clip.z / clip.w)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
