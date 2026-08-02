// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.ui

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFontSamplingMode
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
import io.ygdrasil.webgpu.Extent3D
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUAddressMode
import io.ygdrasil.webgpu.GPUBlendFactor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPUFilterMode
import io.ygdrasil.webgpu.GPUMipmapFilterMode
import io.ygdrasil.webgpu.GPUPrimitiveTopology
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUTexture
import io.ygdrasil.webgpu.GPUTextureDimension
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.GPUTextureUsage
import io.ygdrasil.webgpu.GPUVertexFormat
import io.ygdrasil.webgpu.PrimitiveState
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.SamplerDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.TexelCopyBufferLayout
import io.ygdrasil.webgpu.TexelCopyTextureInfo
import io.ygdrasil.webgpu.TextureDescriptor
import io.ygdrasil.webgpu.TextureViewDescriptor
import io.ygdrasil.webgpu.VertexAttribute
import io.ygdrasil.webgpu.VertexBufferLayout
import io.ygdrasil.webgpu.VertexState

/**
 * WebGPU mirror of Vulkan's `UiGlyphRenderPipeline` (see that class's doc comment for the
 * full rationale): a second UI-overlay pipeline drawn in the same command encoder as
 * [UiRenderPipeline]'s colored quads, sampling a [UiFont] atlas. Unlike Vulkan, this
 * backend's own `Texture` class (`webgpu/texture/Texture.kt`) is an unimplemented stub (no
 * upload path exists yet for either the 3D pass's `Material` or here) -- so the font atlas
 * `GPUTexture` is created and uploaded directly in this class rather than reusing that stub,
 * scoped narrowly to just this glyph-rendering feature.
 */
class UiGlyphRenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    shaderCode: ByteArray,
    private val font: UiFont
) {
    private val device = graphicsDevice.wgpuContext.device
    val pipeline: GPURenderPipeline
    private val screenSizeBuffer: GPUBuffer
    private val fontTexture: GPUTexture
    val bindGroup: GPUBindGroup

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
                                    format = GPUVertexFormat.Float32x2
                                ),
                                VertexAttribute(
                                    shaderLocation = 2u,
                                    offset = (4 * Float.SIZE_BYTES).toULong(),
                                    format = GPUVertexFormat.Float32x4
                                ),
                                // scale(xy) + pivot(zw) -- see ui_glyph.wgsl's `transform` field.
                                VertexAttribute(
                                    shaderLocation = 3u,
                                    offset = (8 * Float.SIZE_BYTES).toULong(),
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
                size = (4 * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst
            )
        )

        // Deliberately NOT swapchainManager.imageFormatWebGpu: this is a sampled-only texture
        // (TextureBinding, never a ColorTargetState/render-pass attachment), uploaded directly
        // from font.atlasPixelsRgba's literal RGBA byte layout -- it never needs to match the
        // browser's preferred *presentation* format, and forcing it to would reinterpret the
        // same bytes with R/B swapped on a BGRA-preferred device.
        fontTexture = device.createTexture(
            TextureDescriptor(
                size = Extent3D(width = font.atlasWidth.toUInt(), height = font.atlasHeight.toUInt()),
                format = GPUTextureFormat.RGBA8Unorm,
                usage = GPUTextureUsage.TextureBinding or GPUTextureUsage.CopyDst,
                dimension = GPUTextureDimension.TwoD
            )
        )
        device.queue.writeTexture(
            destination = TexelCopyTextureInfo(texture = fontTexture),
            data = ArrayBuffer.of(font.atlasPixelsRgba),
            dataLayout = TexelCopyBufferLayout(bytesPerRow = (font.atlasWidth * 4).toUInt()),
            size = Extent3D(width = font.atlasWidth.toUInt(), height = font.atlasHeight.toUInt())
        )
        val fontTextureView = fontTexture.createView(TextureViewDescriptor())
        val fontSampler = device.createSampler(
            SamplerDescriptor(
                addressModeU = GPUAddressMode.ClampToEdge,
                addressModeV = GPUAddressMode.ClampToEdge,
                addressModeW = GPUAddressMode.ClampToEdge,
                magFilter = GPUFilterMode.Linear,
                minFilter = GPUFilterMode.Linear,
                mipmapFilter = GPUMipmapFilterMode.Linear
            )
        )

        bindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = screenSizeBuffer)),
                    BindGroupEntry(binding = 1u, resource = fontTextureView),
                    BindGroupEntry(binding = 2u, resource = fontSampler)
                )
            )
        )

        val renderingContext = graphicsDevice.wgpuContext.renderingContext
        writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
    }

    /** Call once at construction and again whenever the canvas resizes. */
    fun writeScreenSize(width: Float, height: Float) {
        device.queue.writeBuffer(
            screenSizeBuffer,
            0uL,
            ArrayBuffer.of(
                floatArrayOf(
                    width,
                    height,
                    if (font.samplingMode == UiFontSamplingMode.DistanceField) 1f else 0f,
                    font.distanceFieldRangePx
                )
            )
        )
    }

    fun destroy() {
        screenSizeBuffer.close()
        fontTexture.close()
    }
}
