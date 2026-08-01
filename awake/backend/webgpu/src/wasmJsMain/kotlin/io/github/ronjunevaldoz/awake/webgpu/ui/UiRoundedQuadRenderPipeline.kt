// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.ui

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.ArrayBuffer
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
 * Rounded-corner counterpart to [UiRenderPipeline] -- WebGPU mirror of Vulkan's
 * `UiRoundedQuadRenderPipeline` (see that class's doc comment for the full rationale): same
 * alpha-blend setup as [UiRenderPipeline], but a distance-field fragment shader
 * (`ui_rounded_quad.wgsl`) instead of a flat fill, so
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.RoundedQuad] renders real rounded corners
 * outside an active convex-path clip instead of this backend's previous fallback (drawing it
 * as a flat [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Quad], dropping the radius).
 * Vertex layout is pos(vec2, screen-space) + localPos(vec2, pixels relative to the quad's own
 * center) + halfSize(vec2, pixels) + radius(float) + color(vec4) -- see
 * [DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX].
 */
class UiRoundedQuadRenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    shaderCode: ByteArray
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
                            arrayStride = (DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX * Float.SIZE_BYTES).toULong(),
                            attributes = listOf(
                                VertexAttribute(shaderLocation = 0u, offset = 0uL, format = GPUVertexFormat.Float32x2),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = (2 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x2
                                ),
                                VertexAttribute(
                                    shaderLocation = 2u,
                                    offset = (4 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x2
                                ),
                                VertexAttribute(
                                    shaderLocation = 3u,
                                    offset = (6 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32
                                ),
                                VertexAttribute(
                                    shaderLocation = 4u,
                                    offset = (7 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4
                                )
                            )
                        )
                    )
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
                                    dstFactor = GPUBlendFactor.OneMinusSrcAlpha
                                ),
                                alpha = BlendComponent(
                                    srcFactor = GPUBlendFactor.SrcAlpha,
                                    dstFactor = GPUBlendFactor.OneMinusSrcAlpha
                                )
                            )
                        )
                    )
                ),
                primitive = PrimitiveState(topology = GPUPrimitiveTopology.TriangleList)
            )
        )

        screenSizeBuffer = device.createBuffer(
            BufferDescriptor(
                size = (2 * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst
            )
        )
        screenSizeBindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = screenSizeBuffer))
                )
            )
        )
        val renderingContext = graphicsDevice.wgpuContext.renderingContext
        writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
    }

    /** Call once at construction and again whenever the canvas resizes. */
    fun writeScreenSize(width: Float, height: Float) {
        device.queue.writeBuffer(screenSizeBuffer, 0uL, ArrayBuffer.of(floatArrayOf(width, height)))
    }

    fun destroy() {
        screenSizeBuffer.close()
    }
}
