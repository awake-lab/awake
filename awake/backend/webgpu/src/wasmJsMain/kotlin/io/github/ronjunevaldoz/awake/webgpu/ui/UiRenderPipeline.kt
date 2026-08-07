// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.ui

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BlendComponent
import io.ygdrasil.webgpu.BlendState
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBlendFactor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPUPrimitiveTopology
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUVertexFormat
import io.ygdrasil.webgpu.PrimitiveState
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.VertexAttribute
import io.ygdrasil.webgpu.VertexBufferLayout
import io.ygdrasil.webgpu.VertexState

/**
 * The UI overlay's own WebGPU pipeline -- mirrors Vulkan's `UiRenderPipeline` (own vertex
 * layout, `blend` enabled, no depth-stencil state at all -- WebGPU's way of saying "no
 * depth" is simply omitting that field, unlike Vulkan's explicit `depthTestEnable = false`).
 * Screen size goes through a small uniform buffer + bind group, the same mechanism the 3D
 * `Renderer` already uses for its MVP matrix -- WebGPU has no push-constant equivalent in
 * the stable API surface wgpu4k targets.
 */
class UiRenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    shaderCode: ByteArray,
) {
    private val device = graphicsDevice.wgpuContext.device
    val pipeline: GPURenderPipeline
    private val screenSizeBuffer: GPUBuffer
    val screenSizeBindGroup: GPUBindGroup

    init {
        val wgslSource = shaderCode.decodeToString()
        val shaderModule = device.createShaderModule(ShaderModuleDescriptor(code = wgslSource))

        pipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = shaderModule,
                    entryPoint = "vertexMain",
                    buffers = listOf(
                        VertexBufferLayout(
                            arrayStride = (DynamicMesh.FLOATS_PER_VERTEX * Float.SIZE_BYTES).toULong(),
                            attributes = listOf(
                                VertexAttribute(
                                    shaderLocation = 0u,
                                    offset = 0uL,
                                    format = GPUVertexFormat.Float32x2,
                                ),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = (2 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4,
                                ),
                                // scale(xy) + pivot(zw) -- see ui_quad.wgsl's `transform` field.
                                VertexAttribute(
                                    shaderLocation = 2u,
                                    offset = (6 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4,
                                ),
                            ),
                        ),
                    ),
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = "fragmentMain",
                    targets = listOf(
                        ColorTargetState(
                            format = swapchainManager.imageFormatWebGpu,
                            blend = BlendState(
                                color = BlendComponent(
                                    srcFactor = GPUBlendFactor.SrcAlpha,
                                    dstFactor = GPUBlendFactor.OneMinusSrcAlpha,
                                ),
                                alpha = BlendComponent(
                                    srcFactor = GPUBlendFactor.SrcAlpha,
                                    dstFactor = GPUBlendFactor.OneMinusSrcAlpha,
                                ),
                            ),
                        ),
                    ),
                ),
                primitive = PrimitiveState(topology = GPUPrimitiveTopology.TriangleList),
            ),
        )

        screenSizeBuffer = device.createBuffer(
            BufferDescriptor(
                size = (2 * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst,
            ),
        )
        screenSizeBindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = screenSizeBuffer)),
                ),
            ),
        )
        val renderingContext = graphicsDevice.wgpuContext.renderingContext
        writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
    }

    /** Call once at construction and again whenever the canvas resizes. */
    fun writeScreenSize(width: Float, height: Float) {
        device.queue.writeBuffer(screenSizeBuffer, 0uL, fastArrayBufferOf(floatArrayOf(width, height)))
    }

    fun destroy() {
        screenSizeBuffer.close()
    }
}
