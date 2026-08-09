// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.tan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What [Mat4]'s three projection builders actually put on screen, asserted through
 * [applyToColumnVector] (the product the shaders perform on these exact bytes) followed by the
 * perspective divide -- so every expectation here is a real NDC coordinate, not a matrix entry.
 *
 * **This file is where the clip-space depth range is pinned down.** All three builders take a
 * [ClipSpace] and emit either OpenGL's NDC z in `[-1, +1]` or the `[0, 1]` range Vulkan and
 * WebGPU clip against (`0 <= z <= w`). Tests naming a convention assert that convention; the
 * rest use [ClipSpace.Vulkan] as the representative of the two backends this engine has.
 */
class Mat4ProjectionTest {

    // ---- perspective: lateral (x/y) mapping, which is depth-convention independent ----

    @Test
    fun perspectiveScalesYByTheCotangentOfHalfTheFov() {
        assertEquals(1f / tan(HALF_PI / 2f), perspective(HALF_PI, 1f).m11, TOLERANCE)
        assertEquals(1f / tan(THIRD_PI / 2f), perspective(THIRD_PI, 1f).m11, TOLERANCE)
    }

    @Test
    fun narrowingTheFovMagnifiesBothAxes() {
        val wide = perspective(HALF_PI, 1f)
        val narrow = perspective(THIRD_PI, 1f)

        assertTrue(narrow.m11 > wide.m11, "expected ${narrow.m11} > ${wide.m11}")
        assertTrue(narrow.m00 > wide.m00, "expected ${narrow.m00} > ${wide.m00}")
        assertEquals(1.7320508f, narrow.m11, TOLERANCE) // 1 / tan(30 degrees)
    }

    @Test
    fun perspectiveDividesXByTheAspectRatioAndLeavesYAlone() {
        val square = perspective(HALF_PI, 1f)
        val wide = perspective(HALF_PI, 2f)

        assertEquals(square.m00 / 2f, wide.m00, TOLERANCE)
        assertEquals(square.m11, wide.m11, TOLERANCE)
    }

    @Test
    fun theNearPlaneEdgesLandOnTheNdcBoundary() {
        // fov 90 degrees, near 1 => the near plane's half-height is tan(45) * 1 = 1.
        val p = perspective(HALF_PI, 1f)

        assertEquals(1f, p.ndc(0f, 1f, -1f).y, TOLERANCE) // top edge
        assertEquals(-1f, p.ndc(0f, -1f, -1f).y, TOLERANCE) // bottom edge
        assertEquals(1f, p.ndc(1f, 0f, -1f).x, TOLERANCE) // right edge
        assertEquals(0f, p.ndc(0f, 0f, -1f).x, TOLERANCE) // centre
    }

    @Test
    fun aWiderAspectRatioFitsProportionallyMoreWorldAcrossTheSameScreenWidth() {
        val wide = perspective(HALF_PI, 2f)

        // Aspect 2 => the near plane's half-width is 2, so x = 2 is the right edge, not x = 1.
        assertEquals(1f, wide.ndc(2f, 0f, -1f).x, TOLERANCE)
        assertEquals(0.5f, wide.ndc(1f, 0f, -1f).x, TOLERANCE)
    }

    @Test
    fun perspectiveMakesDistantObjectsSmaller() {
        val p = perspective(HALF_PI, 1f)

        val near = p.ndc(1f, 0f, -1f).x
        val far = p.ndc(1f, 0f, -10f).x

        assertEquals(1f, near, TOLERANCE)
        assertEquals(0.1f, far, TOLERANCE)
    }

    // ---- perspective: the depth range, per convention ----

    /**
     * [ClipSpace.OpenGl]'s defining property: `m22 = (near + far) / (near - far)` +
     * `m23 = 2 * near * far / (near - far)`, the textbook GL projection, which lands the near
     * plane at NDC z = -1. This engine has no OpenGL backend today, but the enum value exists
     * and this is what it must keep emitting.
     */
    @Test
    fun perspectiveMapsTheNearPlaneToMinusOneForOpenGl() {
        val p = Mat4.perspective(HALF_PI, 1f, 1f, 100f, ClipSpace.OpenGl)

        assertEquals(-1f, p.ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, p.ndc(0f, 0f, -100f).z, TOLERANCE)
    }

    @Test
    fun perspectiveMapsTheNearPlaneToZeroForVulkanAndWebGpu() {
        assertEquals(0f, perspective(HALF_PI, 1f).ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, perspective(HALF_PI, 1f).ndc(0f, 0f, -100f).z, TOLERANCE)

        assertEquals(0f, Mat4.perspective(HALF_PI, 1f, 1f, 100f, ClipSpace.WebGpu).ndc(0f, 0f, -1f).z, TOLERANCE)
    }

    @Test
    fun nothingBetweenNearAndFarIsClippedByVulkanOrWebGpu() {
        val p = Mat4.perspective(THIRD_PI, 16f / 9f, SCENE_NEAR, SCENE_FAR, ClipSpace.Vulkan)

        for (viewZ in listOf(-SCENE_NEAR, -0.15f, -1f, -50f, -SCENE_FAR)) {
            val clip = p.applyToColumnVector(0f, 0f, viewZ)
            assertTrue(clip.z >= -TOLERANCE, "z_clip ${clip.z} < 0 at view z = $viewZ")
            assertTrue(clip.z <= clip.w + TOLERANCE, "z_clip ${clip.z} > w ${clip.w} at view z = $viewZ")
        }
    }

    @Test
    fun perspectiveDepthIsMonotonicSoTheDepthTestStillOrdersFragments() {
        val p = Mat4.perspective(THIRD_PI, 1f, SCENE_NEAR, SCENE_FAR, ClipSpace.Vulkan)

        var previous = Float.NEGATIVE_INFINITY
        for (viewZ in listOf(-0.2f, -0.5f, -1f, -5f, -25f, -100f)) {
            val z = p.ndc(0f, 0f, viewZ).z
            assertTrue(z > previous, "depth must increase with distance, got $z after $previous")
            previous = z
        }
    }

    // ---- orthographic ----

    @Test
    fun orthographicMapsTheViewVolumeCornersToTheOpenGlNdcCube() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.OpenGl)

        assertVec4(Vec4(1f, 1f, -1f, 1f), o.applyToColumnVector(1f, 1f, -1f))
        assertVec4(Vec4(-1f, -1f, 1f, 1f), o.applyToColumnVector(-1f, -1f, -100f))
    }

    @Test
    fun orthographicKeepsWAtOneSoThereIsNoPerspectiveDivide() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.Vulkan)

        assertEquals(1f, o.applyToColumnVector(0.5f, 0.5f, -50f).w, TOLERANCE)
    }

    @Test
    fun orthographicHandlesAnOffCentreVolumeLikeAPixelSpaceUiProjection() {
        val ui = Mat4.orthographic(0f, 800f, 0f, 600f, -1f, 1f, ClipSpace.Vulkan)

        // z = 0 sits halfway through a [-1, 1] view volume, so it lands mid-range: 0.5 in
        // [0, 1] depth (it was 0 back when this builder only emitted OpenGL's [-1, 1]).
        assertVec4(Vec4(-1f, -1f, 0.5f, 1f), ui.applyToColumnVector(0f, 0f, 0f))
        assertVec4(Vec4(1f, 1f, 0.5f, 1f), ui.applyToColumnVector(800f, 600f, 0f))
        assertVec4(Vec4(0f, 0f, 0.5f, 1f), ui.applyToColumnVector(400f, 300f, 0f))
    }

    /** The [ClipSpace.OpenGl] depth mapping, same convention `perspective` emits for it. */
    @Test
    fun orthographicMapsTheNearPlaneToMinusOneForOpenGl() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.OpenGl)

        assertEquals(-1f, o.applyToColumnVector(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, o.applyToColumnVector(0f, 0f, -100f).z, TOLERANCE)
    }

    @Test
    fun orthographicMapsTheNearPlaneToZeroForVulkanAndWebGpu() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.Vulkan)

        assertEquals(0f, o.applyToColumnVector(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, o.applyToColumnVector(0f, 0f, -100f).z, TOLERANCE)
    }

    // ---- frustum ----

    @Test
    fun aSymmetricFrustumIsExactlyTheEquivalentPerspectiveMatrix() {
        assertMat4(
            perspective(HALF_PI, 1f),
            Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.Vulkan),
        )
        assertMat4(
            Mat4.perspective(HALF_PI, 1f, 1f, 100f, ClipSpace.OpenGl),
            Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.OpenGl),
        )
    }

    @Test
    fun anOffCentreFrustumShearsSoItsOwnCentreIsStillTheNdcCentre() {
        val sheared = Mat4.frustum(0f, 2f, -1f, 1f, 1f, 100f, ClipSpace.Vulkan)

        assertEquals(1f, sheared.m02, TOLERANCE) // the shear term
        assertEquals(0f, sheared.ndc(1f, 0f, -1f).x, TOLERANCE) // x = 1 is the middle of [0, 2]
        assertEquals(-1f, sheared.ndc(0f, 0f, -1f).x, TOLERANCE)
        assertEquals(1f, sheared.ndc(2f, 0f, -1f).x, TOLERANCE)
    }

    @Test
    fun frustumSharesPerspectivesDepthConventionSoTheTwoStayInterchangeable() {
        val f = Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.Vulkan)

        assertEquals(0f, f.ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, f.ndc(0f, 0f, -100f).z, TOLERANCE)

        val gl = Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f, ClipSpace.OpenGl)
        assertEquals(-1f, gl.ndc(0f, 0f, -1f).z, TOLERANCE)
    }

    /** near = 1, far = 100 through this engine's own clip space -- the setup most tests here
     * share, spelled out once so the [ClipSpace] argument doesn't drown the assertion. */
    private fun perspective(fovY: Float, aspect: Float): Mat4 =
        Mat4.perspective(fovY, aspect, 1f, 100f, ClipSpace.Vulkan)

    /** Clip coordinates after the perspective divide -- i.e. the NDC position a fragment gets. */
    private fun Mat4.ndc(x: Float, y: Float, z: Float): Vec3 {
        val clip = applyToColumnVector(x, y, z)
        return Vec3(clip.x / clip.w, clip.y / clip.w, clip.z / clip.w)
    }

    private companion object {
        const val TOLERANCE = 0.0001f
        const val HALF_PI = (kotlin.math.PI / 2.0).toFloat()
        const val THIRD_PI = (kotlin.math.PI / 3.0).toFloat()

        /** `SceneCamera`'s own defaults (`scene/runtime/SceneDocument.kt:43-44`). */
        const val SCENE_NEAR = 0.1f
        const val SCENE_FAR = 100f
    }
}
