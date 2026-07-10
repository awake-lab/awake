package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.webgpu.WebGpuHandles
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUPrimitiveTopology
import io.ygdrasil.webgpu.GPUVertexFormat
import io.ygdrasil.webgpu.PrimitiveState
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.VertexAttribute
import io.ygdrasil.webgpu.VertexBufferLayout
import io.ygdrasil.webgpu.VertexState

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): real wgpu4k implementation.
 * [descriptorSetLayout] is unused -- WebGPU derives the bind group layout from the shader
 * itself ("auto" pipeline layout), unlike Vulkan's explicit `VkDescriptorSetLayout`.
 * [vertShaderCode] is decoded as UTF-8 WGSL source text (not SPIR-V bytecode) containing
 * both a `vertexMain` and `fragmentMain` entry point -- one shader module is used for both
 * pipeline stages, matching how WGSL is normally authored. [fragShaderCode] is unused (the
 * combined source already has both stages). [renderPass]/[pipelineCache] have no WebGPU
 * equivalent and stay 0.
 */
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
    actual var graphicsPipeline: LongArray

    init {
        val device = graphicsDevice.wgpuContext.device
        val wgslSource = vertShaderCode.decodeToString()
        val shaderModule = device.createShaderModule(ShaderModuleDescriptor(code = wgslSource))

        val pipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = shaderModule,
                    entryPoint = "vertexMain",
                    buffers = listOf(
                        VertexBufferLayout(
                            arrayStride = vertexStride.toULong(),
                            attributes = listOf(
                                VertexAttribute(
                                    shaderLocation = 0u,
                                    offset = 0uL,
                                    format = GPUVertexFormat.Float32x3
                                ),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = (3 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x3
                                )
                            )
                        )
                    )
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = "fragmentMain",
                    targets = listOf(
                        ColorTargetState(format = swapchainManager.imageFormatWebGpu)
                    )
                ),
                primitive = PrimitiveState(topology = GPUPrimitiveTopology.TriangleList)
            )
        )
        graphicsPipeline = longArrayOf(WebGpuHandles.register(pipeline))
    }

    actual fun bind(commandBuffer: Long) {
        TODO("WebGPU render-pass binding happens in Renderer.draw() directly, see docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        WebGpuHandles.release(graphicsPipeline[0])
    }
}
