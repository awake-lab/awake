package io.github.ronjunevaldoz.awake.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraTest {

    /** eye at the origin looking down -Z with up = +Y is the one configuration where
     * `Mat4.setLookAt` produces an identity view matrix (forward/side/up all line up with
     * the world axes) -- see `setLookAt`'s math. That makes `view * projection` collapse to
     * exactly `projection`, so this is the one setup where `Camera`'s output can be
     * hand-verified against `Mat4.perspective` directly, without needing a GPU. */
    private fun identityViewCamera(flipYForClipSpace: Boolean = true) = Camera(
        eye = Vec3(0f, 0f, 0f),
        center = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (60.0 * kotlin.math.PI / 180.0).toFloat(),
        near = 0.1f,
        far = 100f,
        flipYForClipSpace = flipYForClipSpace
    )

    @Test
    fun `viewProjectionMatrix matches raw perspective matrix when view is identity`() {
        val aspect = 16f / 9f
        val camera = identityViewCamera(flipYForClipSpace = false)
        val expected = Mat4.perspective(fovY = camera.fovYRadians, aspect = aspect, near = camera.near, far = camera.far)

        val actual = camera.viewProjectionMatrix(aspect)

        assertEquals(expected.data.toList(), actual.data.toList())
    }

    @Test
    fun `viewProjectionMatrix flips Y for Vulkan clip space by default`() {
        val aspect = 16f / 9f
        val camera = identityViewCamera(flipYForClipSpace = true)
        val unflipped = Mat4.perspective(fovY = camera.fovYRadians, aspect = aspect, near = camera.near, far = camera.far)

        val actual = camera.viewProjectionMatrix(aspect)

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

        val wide = camera.viewProjectionMatrix(aspect = 2f)
        val narrow = camera.viewProjectionMatrix(aspect = 1f)

        // m00 = scaleY / aspect -- a smaller aspect ratio must produce a larger m00.
        assert(narrow.m00 > wide.m00) { "expected narrow.m00 (${narrow.m00}) > wide.m00 (${wide.m00})" }
    }
}
