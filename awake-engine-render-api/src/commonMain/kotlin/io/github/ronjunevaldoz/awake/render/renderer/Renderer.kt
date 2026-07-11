// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the one real cross-backend entry
 * point `RenderSystem` calls. `awake-vulkan`'s `expect class Renderer` implements this
 * (`expect class Renderer(...) : io.github.ronjunevaldoz.awake.render.renderer.Renderer`) --
 * see [io.github.ronjunevaldoz.awake.render.mesh.Mesh]'s doc comment for why this doesn't
 * change `VulkanApplication.kt`'s construction pattern.
 */
interface Renderer {
    fun draw(camera: Camera, drawCalls: List<DrawCall>)
    fun destroy()
}
