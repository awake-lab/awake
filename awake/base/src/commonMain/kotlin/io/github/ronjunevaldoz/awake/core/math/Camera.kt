// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.math

/**
 * A view + perspective-projection pair -- extracted out of `VulkanApplication`'s
 * `updateUniformBuffer` (previously computed inline, mixed in with the demo's own
 * cube-spin animation). Pure math, no GPU/backend dependency, so it lives in `awake-core`
 * rather than `awake-vulkan`: any future backend (WebGPU, Metal) needs the same view/
 * projection math, and testing it doesn't require a GPU.
 *
 * [flipYForClipSpace] defaults to `true` because [Mat4.perspective] follows the OpenGL
 * convention (NDC +Y up); Vulkan's NDC has +Y down, so a backend targeting Vulkan needs the
 * projection's Y scale flipped or the scene renders upside down. A backend using OpenGL's
 * own NDC convention would construct this with `false`.
 */
class Camera(
    var eye: Vec3,
    var center: Vec3,
    var up: Vec3 = Vec3(0f, 1f, 0f),
    var fovYRadians: Float,
    var near: Float,
    var far: Float,
    private val flipYForClipSpace: Boolean = true
) {
    /** Returns `view * projection` in Mat4's own (Kotlin-operator) multiplication order,
     * which -- per [Mat4.times]'s convention (`A * B` computes the conventional `B * A`) --
     * is the conventional `projection * view`. A caller building a full MVP matrix combines
     * this with a model matrix the same way `VulkanApplication` already did:
     * `model * camera.viewProjectionMatrix(aspect)` (Kotlin order) gives the conventional
     * `projection * view * model`, the standard clip-space transform order. */
    fun viewProjectionMatrix(aspect: Float): Mat4 {
        val view = Mat4.setLookAt(eye = eye, center = center, up = up)
        val projection = Mat4.perspective(fovY = fovYRadians, aspect = aspect, near = near, far = far)
        if (flipYForClipSpace) {
            projection.m11 *= -1f
        }
        return view * projection
    }

    companion object {
        /**
         * The standard perspective lens, in the units people actually think in: field of view
         * in **degrees**, not radians.
         *
         * Exists because every call site was hand-writing `45f * (PI / 180.0).toFloat()` and
         * repeating the same near/far pair. Prefer this over the raw constructor unless you
         * genuinely need a non-default clip-space convention.
         */
        fun perspective(
            eye: Vec3 = Vec3(0f, 0f, DEFAULT_EYE_DISTANCE),
            center: Vec3 = Vec3.ZERO,
            up: Vec3 = Vec3.UP,
            fovYDegrees: Float = DEFAULT_FOV_DEGREES,
            near: Float = DEFAULT_NEAR,
            far: Float = DEFAULT_FAR,
            flipYForClipSpace: Boolean = true
        ): Camera = Camera(
            eye = eye,
            center = center,
            up = up,
            fovYRadians = fovYDegrees.angleRad,
            near = near,
            far = far,
            flipYForClipSpace = flipYForClipSpace
        )

        const val DEFAULT_FOV_DEGREES = 45f
        const val DEFAULT_NEAR = 0.1f
        const val DEFAULT_FAR = 100f
        private const val DEFAULT_EYE_DISTANCE = 5f
    }
}
