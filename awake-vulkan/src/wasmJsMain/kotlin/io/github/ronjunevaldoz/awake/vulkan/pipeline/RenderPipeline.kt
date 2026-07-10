package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class RenderPipeline actual constructor(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    vertShaderCode: ByteArray,
    fragShaderCode: ByteArray,
    vertexStride: Int
) {
    actual var renderPass: Long = 0
    actual var pipelineLayout: Long = 0
    actual var pipelineCache: Long = 0
    actual var graphicsPipeline: LongArray = longArrayOf()

    actual fun bind(commandBuffer: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
