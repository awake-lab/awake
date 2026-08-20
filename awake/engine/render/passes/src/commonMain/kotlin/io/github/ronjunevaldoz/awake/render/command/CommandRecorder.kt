// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.command

/**
 * Opaque per-backend draw-call recorder. Shared render-feature code holds one of these for the
 * duration of a pass and never touches the concrete Vulkan/WebGPU type underneath.
 *
 * Two boundaries this interface deliberately does *not* cross:
 * - **Pass lifetime.** Beginning/ending a render pass (`VkRenderPassBeginInfo` + framebuffer
 *   selection on Vulkan, `GPURenderPassDescriptor` on WebGPU) stays backend-owned. Each backend
 *   opens a pass, hands a feature a recorder, and closes the pass -- the same boundary the
 *   already-shipped `ShadowFeature` refactor established.
 * - **Resource creation.** Pipelines and materials are still built by each backend's own code,
 *   once, not per frame. Only *recording a draw* lives behind this interface.
 *
 * The implementing class is the only place in a backend allowed to make a real graphics-API
 * call on the recording path, and it holds no draw-order logic of its own -- each method is a
 * one-line translation into one real API call. A shared feature that needs a backend-specific
 * import to compile is a signal the missing capability belongs here as a new method, not as a
 * backend branch inside the shared class.
 */
interface CommandRecorder {
    fun bindPipeline(pipeline: PipelineHandle)

    /** [set] is the descriptor-set index on Vulkan and the bind-group index on WebGPU -- the same
     * slot number both APIs' shaders already declare, so it stays a plain [Int] here. */
    fun bindMaterial(set: Int, binding: MaterialBinding)

    fun bindVertexBuffer(binding: Int, buffer: BufferHandle)

    fun bindIndexBuffer(buffer: BufferHandle)

    /** Sets the hardware dynamic scissor rectangle for subsequent draws in the current pass. */
    fun setScissor(x: Int, y: Int, width: Int, height: Int) {
        // Default no-op for mock/recording test recorders that don't need scissor tracking
    }

    fun draw(vertexCount: Int, instanceCount: Int = 1)

    fun drawIndexed(indexCount: Int, instanceCount: Int = 1)
}

/**
 * Opaque, backend-defined. Vulkan's implementation wraps a `DescriptorSetHandle`, WebGPU's wraps
 * its bind-group equivalent. Shared code passes this through without inspecting it, which is what
 * lets the two genuinely different binding models stay behind one interface: materials are built
 * once, not per frame, so the shared layer only ever needs to re-present an artifact a backend
 * already produced.
 */
interface MaterialBinding

/** Opaque, backend-defined pipeline reference -- see [MaterialBinding] for the same reasoning. */
interface PipelineHandle

/**
 * Opaque, backend-defined vertex/index buffer reference.
 *
 * A marker interface rather than a shared `value class BufferHandle(val handle: Long)`, even
 * though both backends happen to have exactly that type today
 * (`awake.vulkan.handles.BufferHandle`, `awake.webgpu.handles.BufferHandle`): those two `Long`s
 * mean different things (a real `VkBuffer` vs. a WebGPU resource-table index), and a shared
 * concrete wrapper would bake "every backend's buffer is a `Long`" into the contract -- false the
 * moment a backend holds a JS `GPUBuffer` object directly. Keeping it opaque also keeps all three
 * handle types here consistent, so no reader has to remember which one is inspectable.
 *
 * Backend recorders implementing this will need an import alias -- both backends already own a
 * type of this name in their own `handles` package.
 */
interface BufferHandle
