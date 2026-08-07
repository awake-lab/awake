// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.ui

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUBindGroup
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
 * WebGPU mirror of Vulkan's `UiTextureRenderPipeline` (see that class's doc comment for the
 * full rationale) -- a fourth UI-overlay pipeline (alongside [UiRenderPipeline]'s colored
 * quads and [UiGlyphRenderPipeline]'s font glyphs) for
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Texture] primitives sampling a
 * `RenderTarget`-backed [Material]. Same vertex layout as [UiGlyphRenderPipeline] (pos+uv+color,
 * [DynamicMesh.GLYPH_FLOATS_PER_VERTEX]) -- reused rather than adding a new vertex format,
 * even though this pipeline's fragment shader ignores the vertex color.
 *
 * Unlike Vulkan (one shared descriptor set, rewritten per material -- legal there because
 * descriptor writes are host-side and this codebase fully serializes frames), WebGPU
 * `GPUBindGroup`s are immutable once created, so this pipeline instead caches one bind group
 * PER DISTINCT [Material] in [bindGroups], built lazily the first time each material is seen
 * in a frame's `Texture` primitives and reused every frame after that (a minimap/portal
 * material is typically created once and reused, not rebuilt per frame).
 */
class UiTextureRenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    shaderCode: ByteArray,
) {
    private val device = graphicsDevice.wgpuContext.device
    val pipeline: GPURenderPipeline
    private val screenSizeBuffer: GPUBuffer
    private val bindGroups = HashMap<Material, GPUBindGroup>()

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
                            arrayStride = (DynamicMesh.GLYPH_FLOATS_PER_VERTEX * Float.SIZE_BYTES).toULong(),
                            attributes = listOf(
                                VertexAttribute(shaderLocation = 0u, offset = 0uL, format = GPUVertexFormat.Float32x2),
                                VertexAttribute(
                                    shaderLocation = 1u,
                                    offset = (2 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x2,
                                ),
                                VertexAttribute(
                                    shaderLocation = 2u,
                                    offset = (4 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4,
                                ),
                                // scale(xy) + pivot(zw) -- see ui_texture.wgsl's `transform` field.
                                VertexAttribute(
                                    shaderLocation = 3u,
                                    offset = (8 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4,
                                ),
                            ),
                        ),
                    ),
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = "fragmentMain",
                    targets = listOf(ColorTargetState(format = swapchainManager.imageFormatWebGpu)),
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

        val renderingContext = graphicsDevice.wgpuContext.renderingContext
        writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
    }

    /** Call once at construction and again whenever the canvas resizes. */
    fun writeScreenSize(width: Float, height: Float) {
        device.queue.writeBuffer(screenSizeBuffer, 0uL, fastArrayBufferOf(floatArrayOf(width, height)))
    }

    /** Builds (once) and returns the bind group for [material]'s `previewTextureView`/
     * `previewSampler` -- see this class's doc comment for why bind groups are cached per
     * material instead of rewritten like Vulkan's descriptor set. */
    fun bindGroupFor(material: Material): GPUBindGroup = bindGroups.getOrPut(material) {
        device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = screenSizeBuffer)),
                    BindGroupEntry(
                        binding = 1u,
                        resource = requireNotNull(material.previewTextureView) {
                            "Material has no previewTextureView -- was it built via createMaterial(renderTarget = ...)?"
                        },
                    ),
                    BindGroupEntry(
                        binding = 2u,
                        resource = requireNotNull(material.previewSampler) {
                            "Material has no previewSampler -- was it built via createMaterial(renderTarget = ...)?"
                        },
                    ),
                ),
            ),
        )
    }

    fun destroy() {
        screenSizeBuffer.close()
    }
}
