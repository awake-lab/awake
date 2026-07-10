package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.mesh.meshIndexFormat
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.vulkan.webgpu.WebGpuHandles
import io.ygdrasil.webgpu.ArrayBuffer
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.Color
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.RenderPassColorAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.beginRenderPass

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): real wgpu4k implementation of a
 * single triangle/cube draw. No fences/semaphores/frame-in-flight bookkeeping -- the
 * browser's own frame pacing replaces what `SwapchainManager`'s Vulkan sync fields are for.
 *
 * [DrawCall.material] is deliberately **not** touched here -- `Material`'s wasmJs actual is
 * still `TODO()` (out of scope for this slice, see docs/MVP_PLAN.md). Instead this class
 * owns one small uniform buffer + bind group directly per [RenderPipeline] (matching how
 * wgpu4k's own example scenes manage their uniform buffer, with no separate "Material"
 * abstraction), rewritten via `queue.writeBuffer` before each draw call. This only actually
 * works correctly for a single draw call per frame today -- multiple draw calls sharing one
 * uniform buffer within one render pass would clobber each other's MVP matrix, since
 * `queue.writeBuffer` is a queue-scheduled op, not something that interleaves mid-encoder.
 * Real per-draw-call (or per-Material) uniform buffers are Material's job once it's real.
 */
actual class Renderer actual constructor(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    commandPool: Long,
    maxFramesInFlight: Int
) {
    private val graphicsDevice = graphicsDevice
    private val renderPipeline = renderPipeline

    private var uniformBuffer: GPUBuffer? = null
    private var uniformBindGroup: GPUBindGroup? = null

    private fun ensureUniformResources(pipeline: GPURenderPipeline) {
        if (uniformBuffer != null) return
        val device = graphicsDevice.wgpuContext.device
        val buffer = device.createBuffer(
            BufferDescriptor(
                size = (16 * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Uniform or GPUBufferUsage.CopyDst
            )
        )
        uniformBuffer = buffer
        uniformBindGroup = device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(0u),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = buffer))
                )
            )
        )
    }

    actual fun draw(camera: Camera, drawCalls: List<DrawCall>) {
        val device = graphicsDevice.wgpuContext.device
        val renderingContext = graphicsDevice.wgpuContext.renderingContext
        val pipeline = WebGpuHandles.resolve<GPURenderPipeline>(renderPipeline.graphicsPipeline[0])
        ensureUniformResources(pipeline)

        val aspect = renderingContext.width.toFloat() / renderingContext.height.toFloat()
        val viewProjection = camera.viewProjectionMatrix(aspect)

        val encoder = device.createCommandEncoder()
        val colorView = renderingContext.getCurrentTexture().createView()

        encoder.beginRenderPass(
            RenderPassDescriptor(
                colorAttachments = listOf(
                    RenderPassColorAttachment(
                        view = colorView,
                        loadOp = GPULoadOp.Clear,
                        clearValue = Color(0.0, 0.0, 0.0, 1.0),
                        storeOp = GPUStoreOp.Store
                    )
                )
            )
        ) {
            setPipeline(pipeline)
            var drawIndex = 0
            while (drawIndex < drawCalls.size) {
                val drawCall = drawCalls[drawIndex]
                // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
                // Camera.viewProjectionMatrix's docs), matching vulkanMain's Renderer.
                val mvp = drawCall.model * viewProjection
                device.queue.writeBuffer(uniformBuffer!!, 0uL, ArrayBuffer.of(mvp.data))
                setBindGroup(0u, uniformBindGroup!!)
                setVertexBuffer(0u, WebGpuHandles.resolve(drawCall.mesh.vertexBuffer.handle))
                setIndexBuffer(WebGpuHandles.resolve(drawCall.mesh.indexBuffer.handle), meshIndexFormat)
                drawIndexed(drawCall.mesh.indexCount.toUInt())
                drawIndex += 1
            }
            end()
        }

        device.queue.submit(listOf(encoder.finish()))
    }

    actual fun destroy() {
        uniformBuffer?.close()
        uniformBuffer = null
        uniformBindGroup = null
    }
}
