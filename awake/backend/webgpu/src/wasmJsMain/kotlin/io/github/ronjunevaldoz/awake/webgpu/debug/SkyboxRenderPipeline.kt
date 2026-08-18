// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.debug

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.ColorTargetState
import io.ygdrasil.webgpu.DepthStencilState
import io.ygdrasil.webgpu.FragmentState
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPUCompareFunction
import io.ygdrasil.webgpu.GPUCullMode
import io.ygdrasil.webgpu.GPUPrimitiveTopology
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUTextureFormat
import io.ygdrasil.webgpu.PrimitiveState
import io.ygdrasil.webgpu.RenderPipelineDescriptor
import io.ygdrasil.webgpu.ShaderModuleDescriptor
import io.ygdrasil.webgpu.StencilFaceState
import io.ygdrasil.webgpu.VertexState

/**
 * WebGPU mirror of Vulkan's `debug.SkyboxRenderPipeline` -- the procedural sky (`skybox.wgsl`),
 * drawn as the FIRST call inside the same 3D render pass as the existing `drawCalls` loop.
 *
 * `vertex.buffers` is empty and the draw takes no vertex buffer: the fragment covers the
 * viewport via a full-screen triangle generated from `vertex_index`. `depthCompare = Always` +
 * `depthWriteEnabled = false` is this backend's "depth test and write both off" -- and the
 * `depthStencil` block itself is mandatory, not optional, because the pass has a real
 * `Depth32Float` attachment and WebGPU rejects the whole command buffer for a pipeline whose
 * attachment state doesn't match (see [LineRenderPipeline]'s doc comment).
 */
class SkyboxRenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    shaderCode: ByteArray,
) {
    private val device = graphicsDevice.wgpuContext.device
    val pipeline: GPURenderPipeline
    private val uniformBuffer: GPUBuffer
    val bindGroup: GPUBindGroup

    init {
        val shaderModule = device.createShaderModule(ShaderModuleDescriptor(code = shaderCode.decodeToString()))

        pipeline = device.createRenderPipeline(
            RenderPipelineDescriptor(
                vertex = VertexState(
                    module = shaderModule,
                    entryPoint = "vertexMain",
                    buffers = emptyList(),
                ),
                fragment = FragmentState(
                    module = shaderModule,
                    entryPoint = "fragmentMain",
                    targets = listOf(ColorTargetState(format = swapchainManager.imageFormatWebGpu)),
                ),
                // No culling: a single screen-covering primitive has nothing to cull, and its
                // winding depends on the clip space's Y direction.
                primitive = PrimitiveState(
                    topology = GPUPrimitiveTopology.TriangleList,
                    cullMode = GPUCullMode.None,
                ),
                depthStencil = DepthStencilState(
                    format = GPUTextureFormat.Depth32Float,
                    depthWriteEnabled = false,
                    depthCompare = GPUCompareFunction.Always,
                    stencilFront = StencilFaceState(),
                    stencilBack = StencilFaceState(),
                ),
            ),
        )

        uniformBuffer = device.createBuffer(
            BufferDescriptor(
                size = (UNIFORM_FLOATS * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst,
            ),
        )
        bindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = uniformBuffer))),
            ),
        )
    }

    /** [uniforms] must be exactly [UNIFORM_FLOATS] long, in `skybox.wgsl`'s field order. */
    fun writeUniforms(uniforms: FloatArray) {
        require(uniforms.size == UNIFORM_FLOATS) {
            "Skybox uniform block must be $UNIFORM_FLOATS floats, got ${uniforms.size}."
        }
        device.queue.writeBuffer(uniformBuffer, 0uL, fastArrayBufferOf(uniforms))
    }

    fun destroy() {
        uniformBuffer.close()
    }

    companion object {
        /** inverseViewProjection(16) + cameraEye(4) + sunDirection(4) + horizon/zenith/sun/moon
         * colors(4 each) -- `skybox.wgsl`'s Uniforms, in order. */
        const val UNIFORM_FLOATS = 40

        /** Vertex count of the generated full-screen triangle. */
        const val FULLSCREEN_TRIANGLE_VERTICES = 3u
    }
}
