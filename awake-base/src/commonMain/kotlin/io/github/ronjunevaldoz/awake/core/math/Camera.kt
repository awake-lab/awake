/*
 * Awake
 * Awake.awake-core.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
}
