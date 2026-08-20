// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.ygdrasil.webgpu.DepthStencilState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUCompareFunction
import io.ygdrasil.webgpu.GPUCullMode
import io.ygdrasil.webgpu.GPUFrontFace
import io.ygdrasil.webgpu.GPURenderPipeline
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
 * Depth-only graphics pipeline for the directional shadow pre-pass in the WebGPU backend.
 * Uses `shadow_depth.wgsl` to render meshes from the light's point of view into a `Depth32Float` texture.
 */
class ShadowRenderPipeline(
    graphicsDevice: GraphicsDevice,
    shaderCode: ByteArray,
    val vertexFormat: VertexFormat,
    vertexEntryPoint: String = "vertexMain",
    fragmentEntryPoint: String = "fragmentMain",
) {
    val pipeline: GPURenderPipeline
    val handle: WebGpuPipelineHandle

    init {
        val device = graphicsDevice.wgpuContext.device
        val wgslSource = shaderCode.decodeToString()
        val shaderModule = device.createShaderModule(ShaderModuleDescriptor(code = wgslSource))

        val vertexBuffers = listOf(
            VertexBufferLayout(
                arrayStride = vertexFormat.strideBytes.toULong(),
                attributes = vertexFormat.entries.map { (attribute, offsetBytes) ->
                    VertexAttribute(
                        shaderLocation = attribute.location.toUInt(),
                        offset = offsetBytes.toULong(),
                        format = attribute.format.toGpuVertexFormat(),
                    )
                },
            ),
        )

        pipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = shaderModule,
                    entryPoint = vertexEntryPoint,
                    buffers = vertexBuffers,
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = fragmentEntryPoint,
                    targets = emptyList(),
                ),
                primitive = PrimitiveState(
                    cullMode = GPUCullMode.None,
                    frontFace = GPUFrontFace.CW,
                ),
                depthStencil = DepthStencilState(
                    format = GPUTextureFormat.Depth32Float,
                    depthWriteEnabled = true,
                    depthCompare = GPUCompareFunction.LessEqual,
                    stencilFront = StencilFaceState(),
                    stencilBack = StencilFaceState(),
                ),
            ),
        )
        handle = WebGpuPipelineHandle(pipeline)
    }

    fun destroy() {
        // Garbage collected in JS runtime
    }
}

private fun GpuDataShape.toGpuVertexFormat(): GPUVertexFormat = when (this) {
    GpuDataShape.Float -> GPUVertexFormat.Float32
    GpuDataShape.Vec2 -> GPUVertexFormat.Float32x2
    GpuDataShape.Vec3 -> GPUVertexFormat.Float32x3
    GpuDataShape.Vec4 -> GPUVertexFormat.Float32x4
    GpuDataShape.UInt4 -> GPUVertexFormat.Uint32x4
    GpuDataShape.Mat4 -> error("Mat4 is not a valid vertex-attribute format.")
}
