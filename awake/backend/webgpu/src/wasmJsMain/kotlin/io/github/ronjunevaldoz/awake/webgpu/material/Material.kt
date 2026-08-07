// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.material

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.BufferHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorPoolHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.webgpu.texture.Texture
import io.ygdrasil.webgpu.GPUSampler
import io.ygdrasil.webgpu.GPUTextureView
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md. `previewTextureView`/`previewSampler` are the one exception: set by
// [createResourcesFromRenderTarget], scoped narrowly to the UI-compositing use case
// (`Renderer.createMaterial(renderTarget = ...)` -> `UiTextureRenderPipeline`) -- the
// general 3D `DrawCall.material` texture-binding path (`createResources`/`bind`) remains
// unimplemented, see that method's own doc comment for why.
class Material(graphicsDevice: GraphicsDevice, private val uniformFloatCount: Int = 16) : RenderMaterial {
    val descriptorSetLayout: DescriptorSetLayoutHandle = DescriptorSetLayoutHandle(0)
    var descriptorPool: DescriptorPoolHandle = DescriptorPoolHandle(0)
    var descriptorSet: DescriptorSetHandle = DescriptorSetHandle(0)
    var uniformBuffer: BufferHandle = BufferHandle(0)
    var uniformBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)

    var previewTextureView: GPUTextureView? = null
        private set
    var previewSampler: GPUSampler? = null
        private set

    /** Scoped to the UI-compositing case -- see this class's own doc comment. Not `createResources`'s
     * general-purpose sibling: this backend's `DrawCall.material` texture binding
     * (world-space quads sampling a render target) is explicitly deferred, since it'd also
     * require the shared 3D shader to declare a second bind group -- real scope creep beyond
     * offscreen rendering itself. */
    fun createResourcesFromRenderTarget(textureView: GPUTextureView, sampler: GPUSampler) {
        previewTextureView = textureView
        previewSampler = sampler
    }

    /** Only ever called for the 3D `DrawCall.material` path -- see this class's own doc
     * comment for why that's still out of scope on this backend. */
    fun createResources(texture: Texture) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    override fun updateUniformBuffer(mvp: FloatArray) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    override fun bind(commandBuffer: Long, pipelineLayout: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    /** Unlike Vulkan, WebGPU objects (`GPUSampler`/`GPUTextureView`) are garbage-collected by
     * the JS runtime -- there's no native handle to explicitly release here. This must be
     * safe to call unconditionally (every [io.github.ronjunevaldoz.awake.engine.application.Game.dispose]
     * calls its own `Material.destroy()` regardless of whether the general 3D texture-binding
     * path above was ever used), so it just drops the references rather than throwing the
     * same `TODO()` as the genuinely-unimplemented methods above. */
    override fun destroy() {
        previewTextureView = null
        previewSampler = null
    }
}
