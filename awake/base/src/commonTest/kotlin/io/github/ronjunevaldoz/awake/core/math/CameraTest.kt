// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        far = 100f,
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
            clipSpace = ClipSpace.WebGpu,
        )

        val actual = camera.viewProjectionMatrix(aspect, ClipSpace.WebGpu)

        // The default lens is still perspective, entry for entry -- adding the orthographic
        // branch must not change what any existing camera projects.
        assertEquals(Camera.Projection.Perspective, camera.projection)
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
            clipSpace = ClipSpace.Vulkan,
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
        assertTrue(narrow.m00 > wide.m00, "expected narrow.m00 (${narrow.m00}) > wide.m00 (${wide.m00})")
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
            far = 100f,
        )

        val clip = pulledBack.viewProjectionMatrix(aspect = 1f, clipSpace = ClipSpace.WebGpu)
            .applyToColumnVector(0f, 0f, 0f)

        // The world origin is 5 units in front of the camera, so w (which is -z_view) is 5.
        assertEquals(5f, clip.w, TOLERANCE)
    }

    /** The defining property of an orthographic lens: depth does not scale anything. Two
     * points on the same line of sight from the camera -- one at the target plane, one far
     * behind it -- must land on the same NDC XY, where perspective would shrink the far one. */
    @Test
    fun `orthographic projects the same XY at any depth where perspective shrinks`() {
        val ortho = identityViewCamera().apply {
            projection = Camera.Projection.Orthographic
            orthoHalfHeight = 2f
        }.viewProjectionMatrix(aspect = 2f, clipSpace = ClipSpace.WebGpu)

        val atTarget = ortho.ndc(1f, 1f, -5f)
        val farBehind = ortho.ndc(1f, 1f, -50f)

        assertEquals(atTarget.x, farBehind.x, TOLERANCE)
        assertEquals(atTarget.y, farBehind.y, TOLERANCE)
        // half-height 2 at aspect 2 => half-width 4, so x = 1 is a quarter across and y = 1
        // is half way up -- absolute placement, not just "the two agree".
        assertEquals(0.25f, atTarget.x, TOLERANCE)
        assertEquals(0.5f, atTarget.y, TOLERANCE)
        // Depth still increases with distance, so the depth test keeps ordering fragments.
        assertTrue(farBehind.z > atTarget.z, "expected ${farBehind.z} > ${atTarget.z}")

        // The contrast: the same two points under the default perspective lens do not agree.
        val perspective = identityViewCamera().viewProjectionMatrix(aspect = 2f, clipSpace = ClipSpace.WebGpu)
        assertTrue(
            perspective.ndc(1f, 1f, -50f).x < perspective.ndc(1f, 1f, -5f).x,
            "perspective must shrink with depth",
        )
    }

    /** Ortho goes through the same [ClipSpace] mechanism perspective does -- same flipY, same
     * `[0, 1]` depth range -- so a backend cannot end up with one convention per projection. */
    @Test
    fun `orthographic honours the renderer's ClipSpace exactly like perspective`() {
        fun ortho(clipSpace: ClipSpace) = identityViewCamera().apply {
            projection = Camera.Projection.Orthographic
            orthoHalfHeight = 2f
            near = 1f
            far = 100f
        }.viewProjectionMatrix(aspect = 1f, clipSpace = clipSpace)

        val webGpu = ortho(ClipSpace.WebGpu).ndc(0.5f, 0.5f, -1f)
        val vulkan = ortho(ClipSpace.Vulkan).ndc(0.5f, 0.5f, -1f)

        assertEquals(-webGpu.y, vulkan.y, TOLERANCE) // flipY, and only Y
        assertEquals(webGpu.x, vulkan.x, TOLERANCE)
        assertEquals(webGpu.z, vulkan.z, TOLERANCE)

        // depthZeroToOne: the near plane is 0 and the far plane is 1 for both.
        assertEquals(0f, vulkan.z, TOLERANCE)
        assertEquals(1f, ortho(ClipSpace.Vulkan).ndc(0f, 0f, -100f).z, TOLERANCE)
        // ...and OpenGL's -1 .. 1 instead, from the same call.
        assertEquals(-1f, ortho(ClipSpace.OpenGl).ndc(0f, 0f, -1f).z, TOLERANCE)
    }

    private fun Mat4.ndc(x: Float, y: Float, z: Float): Vec3 {
        val clip = applyToColumnVector(x, y, z)
        return Vec3(clip.x / clip.w, clip.y / clip.w, clip.z / clip.w)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
    }
}
