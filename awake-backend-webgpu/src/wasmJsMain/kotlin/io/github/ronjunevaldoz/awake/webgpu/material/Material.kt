package io.github.ronjunevaldoz.awake.webgpu.material

import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.BufferHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorPoolHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.webgpu.texture.Texture

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
class Material(graphicsDevice: GraphicsDevice) : RenderMaterial {
    val descriptorSetLayout: DescriptorSetLayoutHandle = DescriptorSetLayoutHandle(0)
    var descriptorPool: DescriptorPoolHandle = DescriptorPoolHandle(0)
    var descriptorSet: DescriptorSetHandle = DescriptorSetHandle(0)
    var uniformBuffer: BufferHandle = BufferHandle(0)
    var uniformBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)

    fun createResources(texture: Texture) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    override fun updateUniformBuffer(mvp: FloatArray) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    override fun bind(commandBuffer: Long, pipelineLayout: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    override fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
