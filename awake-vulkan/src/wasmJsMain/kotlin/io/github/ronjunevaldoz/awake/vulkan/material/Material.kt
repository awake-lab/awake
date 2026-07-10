package io.github.ronjunevaldoz.awake.vulkan.material

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorPoolHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class Material actual constructor(graphicsDevice: GraphicsDevice) {
    actual val descriptorSetLayout: DescriptorSetLayoutHandle = DescriptorSetLayoutHandle(0)
    actual var descriptorPool: DescriptorPoolHandle = DescriptorPoolHandle(0)
    actual var descriptorSet: DescriptorSetHandle = DescriptorSetHandle(0)
    actual var uniformBuffer: BufferHandle = BufferHandle(0)
    actual var uniformBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)

    actual fun createResources(texture: Texture) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun updateUniformBuffer(mvp: FloatArray) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun bind(commandBuffer: Long, pipelineLayout: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
