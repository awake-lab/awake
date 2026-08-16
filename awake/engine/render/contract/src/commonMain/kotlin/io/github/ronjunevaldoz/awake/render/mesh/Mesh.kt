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
    /** The vertex layout this mesh's GPU buffer was built with -- lets a [Renderer]
     * implementation pick the correct pipeline for a [io.github.ronjunevaldoz.awake.render
     * .renderer.DrawCall] by [format] instead of assuming every mesh shares the one pipeline
     * the renderer happens to have bound. Set once at [Renderer.createMesh] time from the
     * [MeshGeometry.format] the mesh was created from. */
    val format: VertexFormat
    fun bind(commandBuffer: Long)
    fun draw(commandBuffer: Long)

    /** Draws [instanceCount] copies of this mesh in one GPU call, each reading its own model
     * matrix from an instance-rate vertex buffer a [Renderer] implementation bound alongside
     * this mesh's own vertex buffer -- see [io.github.ronjunevaldoz.awake.render.renderer
     * .DrawCall.instanceModels]'s doc comment for the caller-facing contract. Default throws:
     * only a backend that actually built an instanced pipeline for this mesh's [format]
     * overrides it; every test-only/Noop `Mesh` (this interface has several anonymous
     * implementations across test sources) never calls this, so they're free to inherit the
     * default rather than each restating a body they'd never exercise. */
    fun drawInstanced(commandBuffer: Long, instanceCount: Int) {
        error("drawInstanced is not supported by this Mesh implementation (format=$format).")
    }

    fun destroy()
}
