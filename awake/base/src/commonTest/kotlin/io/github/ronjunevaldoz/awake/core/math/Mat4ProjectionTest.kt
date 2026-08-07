// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.tan
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * What [Mat4]'s three projection builders actually put on screen, asserted through
 * [applyToColumnVector] (the product the shaders perform on these exact bytes) followed by the
 * perspective divide -- so every expectation here is a real NDC coordinate, not a matrix entry.
 *
 * **This file is where the clip-space depth-range gap is pinned down.** All three builders emit
 * the OpenGL convention, NDC z in `[-1, +1]`. Vulkan and WebGPU both clip to `0 <= z <= w`, so
 * the entire near half of that range is discarded on the only two backends this engine has.
 * The `perspective...Current...` tests below characterise the defect; the `@Ignore`d
 * `...ShouldMap...` tests state the behaviour those backends need.
 */
class Mat4ProjectionTest {

    // ---- perspective: lateral (x/y) mapping, which is correct today ----

    @Test
    fun perspectiveScalesYByTheCotangentOfHalfTheFov() {
        assertEquals(1f / tan(HALF_PI / 2f), Mat4.perspective(HALF_PI, 1f, 1f, 100f).m11, TOLERANCE)
        assertEquals(1f / tan(THIRD_PI / 2f), Mat4.perspective(THIRD_PI, 1f, 1f, 100f).m11, TOLERANCE)
    }

    @Test
    fun narrowingTheFovMagnifiesBothAxes() {
        val wide = Mat4.perspective(HALF_PI, 1f, 1f, 100f)
        val narrow = Mat4.perspective(THIRD_PI, 1f, 1f, 100f)

        assertTrue(narrow.m11 > wide.m11, "expected ${narrow.m11} > ${wide.m11}")
        assertTrue(narrow.m00 > wide.m00, "expected ${narrow.m00} > ${wide.m00}")
        assertEquals(1.7320508f, narrow.m11, TOLERANCE) // 1 / tan(30 degrees)
    }

    @Test
    fun perspectiveDividesXByTheAspectRatioAndLeavesYAlone() {
        val square = Mat4.perspective(HALF_PI, 1f, 1f, 100f)
        val wide = Mat4.perspective(HALF_PI, 2f, 1f, 100f)

        assertEquals(square.m00 / 2f, wide.m00, TOLERANCE)
        assertEquals(square.m11, wide.m11, TOLERANCE)
    }

    @Test
    fun theNearPlaneEdgesLandOnTheNdcBoundary() {
        // fov 90 degrees, near 1 => the near plane's half-height is tan(45) * 1 = 1.
        val p = Mat4.perspective(HALF_PI, 1f, 1f, 100f)

        assertEquals(1f, p.ndc(0f, 1f, -1f).y, TOLERANCE) // top edge
        assertEquals(-1f, p.ndc(0f, -1f, -1f).y, TOLERANCE) // bottom edge
        assertEquals(1f, p.ndc(1f, 0f, -1f).x, TOLERANCE) // right edge
        assertEquals(0f, p.ndc(0f, 0f, -1f).x, TOLERANCE) // centre
    }

    @Test
    fun aWiderAspectRatioFitsProportionallyMoreWorldAcrossTheSameScreenWidth() {
        val wide = Mat4.perspective(HALF_PI, 2f, 1f, 100f)

        // Aspect 2 => the near plane's half-width is 2, so x = 2 is the right edge, not x = 1.
        assertEquals(1f, wide.ndc(2f, 0f, -1f).x, TOLERANCE)
        assertEquals(0.5f, wide.ndc(1f, 0f, -1f).x, TOLERANCE)
    }

    @Test
    fun perspectiveMakesDistantObjectsSmaller() {
        val p = Mat4.perspective(HALF_PI, 1f, 1f, 100f)

        val near = p.ndc(1f, 0f, -1f).x
        val far = p.ndc(1f, 0f, -10f).x

        assertEquals(1f, near, TOLERANCE)
        assertEquals(0.1f, far, TOLERANCE)
    }

    // ---- perspective: the depth-range defect ----

    /**
     * Characterises a real defect, it is not an endorsement. `Mat4.perspective`'s
     * `m22 = (near + far) / (near - far)` + `m23 = 2 * near * far / (near - far)` is the
     * textbook **OpenGL** projection: the near plane lands at NDC z = -1. Vulkan and WebGPU
     * both need it at 0. See `perspectiveShouldMapTheNearPlaneToZeroForVulkanAndWebGpu`.
     */
    @Test
    fun perspectiveCurrentlyMapsTheNearPlaneToMinusOneTheOpenGlConvention() {
        val p = Mat4.perspective(HALF_PI, 1f, 1f, 100f)

        assertEquals(-1f, p.ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, p.ndc(0f, 0f, -100f).z, TOLERANCE)
    }

    /**
     * The concrete cost of the above on the two backends that actually exist. Vulkan/WebGPU clip
     * to `0 <= z_clip <= w_clip`, so with the scene defaults (`SceneCamera.near = 0.1f`,
     * `far = 100f`) everything nearer than ~0.1998 -- almost exactly *twice* the requested near
     * plane -- is silently thrown away, and only half the depth buffer's range is ever written.
     */
    @Test
    fun perspectiveCurrentlyClipsAwayEverythingInFrontOfTwiceTheNearPlaneOnVulkanAndWebGpu() {
        val p = Mat4.perspective(THIRD_PI, 16f / 9f, SCENE_NEAR, SCENE_FAR)

        val justInsideNear = p.applyToColumnVector(0f, 0f, -0.15f)
        // OpenGL's clip test (-w <= z <= w) keeps it...
        assertTrue(justInsideNear.z >= -justInsideNear.w, "OpenGL would keep this fragment")
        // ...Vulkan's / WebGPU's (0 <= z <= w) throws it away.
        assertTrue(justInsideNear.z < 0f, "expected z_clip < 0, got ${justInsideNear.z}")

        // The crossover: z_clip = 0 at view-space z = -m23 / m22.
        assertEquals(-0.1998f, -p.m23 / p.m22, TOLERANCE)
    }

    @Ignore(
        "DEFECT: Mat4.perspective emits OpenGL's NDC z range [-1, +1], but every backend this " +
            "engine has (Vulkan, WebGPU) clips to [0, 1]. Half the depth range is discarded and " +
            "the effective near plane is ~2x the requested one. Camera.flipYForClipSpace only " +
            "corrects the Y axis; nothing corrects depth. Fix: emit m22 = far / (near - far), " +
            "m23 = near * far / (near - far) (drop the factor of 2 and the +near), or add a " +
            "depth-range parameter alongside flipYForClipSpace. Un-@Ignore once matrix.kt maps " +
            "near -> 0."
    )
    @Test
    fun perspectiveShouldMapTheNearPlaneToZeroForVulkanAndWebGpu() {
        val p = Mat4.perspective(HALF_PI, 1f, 1f, 100f)

        assertEquals(0f, p.ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, p.ndc(0f, 0f, -100f).z, TOLERANCE)
    }

    @Ignore("DEFECT: see perspectiveShouldMapTheNearPlaneToZeroForVulkanAndWebGpu.")
    @Test
    fun nothingBetweenNearAndFarShouldBeClippedByVulkanOrWebGpu() {
        val p = Mat4.perspective(THIRD_PI, 16f / 9f, SCENE_NEAR, SCENE_FAR)

        for (viewZ in listOf(-SCENE_NEAR, -0.15f, -1f, -50f, -SCENE_FAR)) {
            val clip = p.applyToColumnVector(0f, 0f, viewZ)
            assertTrue(clip.z >= -TOLERANCE, "z_clip ${clip.z} < 0 at view z = $viewZ")
            assertTrue(clip.z <= clip.w + TOLERANCE, "z_clip ${clip.z} > w ${clip.w} at view z = $viewZ")
        }
    }

    @Test
    fun perspectiveDepthIsMonotonicSoTheDepthTestStillOrdersFragments() {
        val p = Mat4.perspective(THIRD_PI, 1f, SCENE_NEAR, SCENE_FAR)

        var previous = Float.NEGATIVE_INFINITY
        for (viewZ in listOf(-0.2f, -0.5f, -1f, -5f, -25f, -100f)) {
            val z = p.ndc(0f, 0f, viewZ).z
            assertTrue(z > previous, "depth must increase with distance, got $z after $previous")
            previous = z
        }
    }

    // ---- orthographic ----

    @Test
    fun orthographicMapsTheViewVolumeCornersToTheNdcCube() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f)

        assertVec4(Vec4(1f, 1f, -1f, 1f), o.applyToColumnVector(1f, 1f, -1f))
        assertVec4(Vec4(-1f, -1f, 1f, 1f), o.applyToColumnVector(-1f, -1f, -100f))
    }

    @Test
    fun orthographicKeepsWAtOneSoThereIsNoPerspectiveDivide() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f)

        assertEquals(1f, o.applyToColumnVector(0.5f, 0.5f, -50f).w, TOLERANCE)
    }

    @Test
    fun orthographicHandlesAnOffCentreVolumeLikeAPixelSpaceUiProjection() {
        val ui = Mat4.orthographic(0f, 800f, 0f, 600f, -1f, 1f)

        assertVec4(Vec4(-1f, -1f, 0f, 1f), ui.applyToColumnVector(0f, 0f, 0f))
        assertVec4(Vec4(1f, 1f, 0f, 1f), ui.applyToColumnVector(800f, 600f, 0f))
        assertVec4(Vec4(0f, 0f, 0f, 1f), ui.applyToColumnVector(400f, 300f, 0f))
    }

    /** Characterises a real defect, it is not an endorsement -- same gap as `perspective`. */
    @Test
    fun orthographicCurrentlyMapsTheNearPlaneToMinusOneTheOpenGlConvention() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f)

        assertEquals(-1f, o.applyToColumnVector(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, o.applyToColumnVector(0f, 0f, -100f).z, TOLERANCE)
    }

    @Ignore("DEFECT: same [-1, 1] vs [0, 1] depth-range gap as Mat4.perspective -- see that test.")
    @Test
    fun orthographicShouldMapTheNearPlaneToZeroForVulkanAndWebGpu() {
        val o = Mat4.orthographic(-1f, 1f, -1f, 1f, 1f, 100f)

        assertEquals(0f, o.applyToColumnVector(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, o.applyToColumnVector(0f, 0f, -100f).z, TOLERANCE)
    }

    // ---- frustum ----

    @Test
    fun aSymmetricFrustumIsExactlyTheEquivalentPerspectiveMatrix() {
        assertMat4(
            Mat4.perspective(HALF_PI, 1f, 1f, 100f),
            Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f)
        )
    }

    @Test
    fun anOffCentreFrustumShearsSoItsOwnCentreIsStillTheNdcCentre() {
        val sheared = Mat4.frustum(0f, 2f, -1f, 1f, 1f, 100f)

        assertEquals(1f, sheared.m02, TOLERANCE) // the shear term
        assertEquals(0f, sheared.ndc(1f, 0f, -1f).x, TOLERANCE) // x = 1 is the middle of [0, 2]
        assertEquals(-1f, sheared.ndc(0f, 0f, -1f).x, TOLERANCE)
        assertEquals(1f, sheared.ndc(2f, 0f, -1f).x, TOLERANCE)
    }

    @Test
    fun frustumSharesPerspectivesDepthConventionSoTheTwoStayInterchangeable() {
        val f = Mat4.frustum(-1f, 1f, -1f, 1f, 1f, 100f)

        assertEquals(-1f, f.ndc(0f, 0f, -1f).z, TOLERANCE)
        assertEquals(1f, f.ndc(0f, 0f, -100f).z, TOLERANCE)
    }

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
