// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the backend-neutral surface
 * `RenderSystem`/`DrawCall`/`Renderer.draw()` actually need across the module boundary --
 * deliberately narrow, not a 1:1 port of every Vulkan-backend `Mesh` member. Usage analysis
 * this session confirmed no caller outside `awake-vulkan` ever reads `vertexBuffer`/
 * `indexBuffer`/`indexCount`/etc. directly; only `bind()`/`draw()` are invoked generically
 * (by a `Renderer` implementation iterating `List<DrawCall>`). `awake-vulkan`'s real
 * `expect class Mesh` implements this interface (`expect class Mesh(...) :
 * io.github.ronjunevaldoz.awake.render.mesh.Mesh`) -- an `expect` class can implement an
 * interface declared in a different module, so `VulkanApplication.kt`'s existing
 * `Mesh(graphicsDevice, ...)` construction pattern needs no changes.
 */
interface Mesh {
    fun bind(commandBuffer: Long)
    fun draw(commandBuffer: Long)
    fun destroy()
}
