// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.pipeline

import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.DepthStencilState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUCompareFunction
import io.ygdrasil.webgpu.GPUCullMode
import io.ygdrasil.webgpu.GPUFrontFace
import io.ygdrasil.webgpu.GPUPrimitiveTopology
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUVertexFormat
import io.ygdrasil.webgpu.PrimitiveState
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.StencilFaceState
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
 *
 * [topology] defaults to `TriangleList`; a `LineList` companion pipeline (built with the same
 * shader/vertex layout, just this one field different) is how `Renderer.wireframe` is
 * implemented on this backend -- see `webgpu.renderer.Renderer`'s own doc comment for why
 * (WebGPU has no `VK_POLYGON_MODE_LINE` equivalent).
 */
class RenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    vertShaderCode: ByteArray,
    fragShaderCode: ByteArray,
    vertexStride: Int,
    vertexEntryPoint: String = DEFAULT_VERTEX_ENTRY_POINT,
    fragmentEntryPoint: String = DEFAULT_FRAGMENT_ENTRY_POINT,
    topology: GPUPrimitiveTopology = GPUPrimitiveTopology.TriangleList,
) {
    var renderPass: Long = 0
    var pipelineLayout: Long = 0
    var pipelineCache: Long = 0
    var graphicsPipeline: LongArray

    init {
        val device = graphicsDevice.wgpuContext.device
        val wgslSource = vertShaderCode.decodeToString()
        val shaderModule = device.createShaderModule(ShaderModuleDescriptor(code = wgslSource))

        val pipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = shaderModule,
                    entryPoint = vertexEntryPoint,
                    buffers = listOf(
                        VertexBufferLayout(
                            arrayStride = vertexStride.toULong(),
                            attributes = listOf(
                                VertexAttribute(
                                    shaderLocation = 0u,
                                    offset = 0uL,
                                    format = GPUVertexFormat.Float32x3,
                                ),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = (3 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x3,
                                ),
                                VertexAttribute(
                                    shaderLocation = 2u,
                                    offset = (6 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x2,
                                ),
                            ),
                        ),
                    ),
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = fragmentEntryPoint,
                    targets = listOf(
                        ColorTargetState(format = swapchainManager.imageFormatWebGpu),
                    ),
                ),
                primitive = PrimitiveState(
                    topology = topology,
                    cullMode = GPUCullMode.None,
                    frontFace = GPUFrontFace.CW,
                ),
                depthStencil = DepthStencilState(
                    format = GPUTextureFormat.Depth32Float,
                    depthWriteEnabled = true,
                    depthCompare = GPUCompareFunction.Less,
                    stencilFront = StencilFaceState(),
                    stencilBack = StencilFaceState(),
                ),
            ),
        )
        graphicsPipeline = longArrayOf(WebGpuHandles.register(pipeline))
    }

    fun bind(commandBuffer: Long) {
        TODO("WebGPU render-pass binding happens in Renderer.draw() directly, see docs/MVP_PLAN.md")
    }

    fun destroy() {
        WebGpuHandles.release(graphicsPipeline[0])
    }

    private companion object {
        const val DEFAULT_VERTEX_ENTRY_POINT = "vertexMain"
        const val DEFAULT_FRAGMENT_ENTRY_POINT = "fragmentMain"
    }
}
