// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

import kotlin.math.tan

/**
 * A view + projection pair -- extracted out of `VulkanApplication`'s
 * `updateUniformBuffer` (previously computed inline, mixed in with the demo's own
 * cube-spin animation). Pure math, no GPU/backend dependency, so it lives in `awake-core`
 * rather than `awake-vulkan`: any future backend (WebGPU, Metal) needs the same view/
 * projection math, and testing it doesn't require a GPU.
 *
 * A `Camera` describes a **lens** -- eye, target, up, field of view, near/far -- and nothing
 * else. It deliberately stores no clip-space convention: the active
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer] owns that (it owns the API), and
 * passes its [ClipSpace] to [viewProjectionMatrix] at the moment the matrix is built. That is
 * why a scene, a demo or a test cannot bake in the wrong convention -- it never supplies one.
 */
class Camera(
    var eye: Vec3,
    var center: Vec3,
    var up: Vec3 = Vec3(0f, 1f, 0f),
    var fovYRadians: Float,
    var near: Float,
    var far: Float,
) {
    /** Which lens shape [viewProjectionMatrix] builds. Perspective unless a caller opts out,
     * so every camera built before this existed keeps the matrix it always had. */
    var projection: Projection = Projection.Perspective

    /** Half the vertical world extent an orthographic view covers; left/right follow from it
     * and the aspect ratio. Read only while [projection] is [Projection.Orthographic].
     *
     * Ortho has no perspective divide, so nothing else sets on-screen scale. A caller
     * toggling projection at runtime wants `distance * tan(fovYRadians / 2)`, which frames
     * exactly what the perspective lens frames at that distance. */
    var orthoHalfHeight: Float = DEFAULT_ORTHO_HALF_HEIGHT

    /** An enum plus a separate [orthoHalfHeight] rather than a sealed type carrying the
     * half-height: camera-driver systems reassign this every frame, and a variant with a
     * payload would allocate there (core-math skill Rule 2). */
    enum class Projection { Perspective, Orthographic }

    /** Returns `view * projection` in Mat4's own (Kotlin-operator) multiplication order,
     * which -- per [Mat4.times]'s convention (`A * B` computes the conventional `B * A`) --
     * is the conventional `projection * view`. A caller building a full MVP matrix combines
     * this with a model matrix the same way `VulkanApplication` already did:
     * `model * camera.viewProjectionMatrix(aspect, clipSpace)` (Kotlin order) gives the
     * conventional `projection * view * model`, the standard clip-space transform order.
     *
     * [clipSpace] has no default on purpose: it comes from the renderer that is about to
     * consume this matrix, and a default is exactly how a camera ends up silently baking in
     * some other backend's convention. */
    fun viewProjectionMatrix(aspect: Float, clipSpace: ClipSpace): Mat4 {
        val view = Mat4.setLookAt(eye = eye, center = center, up = up)
        val projectionMatrix = when (projection) {
            Projection.Perspective -> Mat4.perspective(
                fovY = fovYRadians,
                aspect = aspect,
                near = near,
                far = far,
                clipSpace = clipSpace,
            )

            // Same [clipSpace] through the same builder family, so ortho picks up the depth
            // convention (and the flipY below) exactly the way perspective does.
            Projection.Orthographic -> {
                val halfWidth = orthoHalfHeight * aspect
                Mat4.orthographic(
                    left = -halfWidth,
                    right = halfWidth,
                    bottom = -orthoHalfHeight,
                    top = orthoHalfHeight,
                    near = near,
                    far = far,
                    clipSpace = clipSpace,
                )
            }
        }
        if (clipSpace.flipY) {
            projectionMatrix.m11 *= -1f
        }
        return view * projectionMatrix
    }

    companion object {
        /**
         * The standard perspective lens, in the units people actually think in: field of view
         * in **degrees**, not radians.
         *
         * Exists because every call site was hand-writing `45f * (PI / 180.0).toFloat()` and
         * repeating the same near/far pair. Prefer this over the raw constructor.
         */
        fun perspective(
            eye: Vec3 = Vec3(0f, 0f, DEFAULT_EYE_DISTANCE),
            center: Vec3 = Vec3.ZERO,
            up: Vec3 = Vec3.UP,
            fovYDegrees: Float = DEFAULT_FOV_DEGREES,
            near: Float = DEFAULT_NEAR,
            far: Float = DEFAULT_FAR,
        ): Camera = Camera(
            eye = eye,
            center = center,
            up = up,
            fovYRadians = fovYDegrees.angleRad,
            near = near,
            far = far,
        )

        const val DEFAULT_FOV_DEGREES = 45f
        const val DEFAULT_NEAR = 0.1f
        const val DEFAULT_FAR = 100f

        /** What the default perspective lens frames at [DEFAULT_EYE_DISTANCE], so an untouched
         * camera switched to ortho keeps its subject roughly the same size. */
        val DEFAULT_ORTHO_HALF_HEIGHT = DEFAULT_EYE_DISTANCE * tan(DEFAULT_FOV_DEGREES.angleRad / 2f)

        private const val DEFAULT_EYE_DISTANCE = 5f
    }
}
