// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.mesh.meshIndexFormat
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.webgpu.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
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
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    private val uiRenderPipeline: UiRenderPipeline,
    private val uiGlyphRenderPipeline: UiGlyphRenderPipeline,
    commandPool: Long,
    maxFramesInFlight: Int
) : RenderRenderer {
    private val graphicsDevice = graphicsDevice
    private val renderPipeline = renderPipeline

    private var uniformBuffer: GPUBuffer? = null
    private var uniformBindGroup: GPUBindGroup? = null

    // Rewritten by drawUi() (called before draw() -- see WebGpuGameApplication.onRender()'s
    // ordering) so the UI pass, appended to the SAME command encoder as the 3D pass inside
    // draw(), always draws this frame's widgets, not last frame's.
    private val uiMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS)
    private val uiGlyphMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)

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

    /** Stages this frame's UI overlay content -- rewrites [uiMesh]'s buffers but issues no
     * GPU commands itself. Must be called BEFORE [draw] (see `WebGpuGameApplication
     * .onRender()`'s ordering) so [draw]'s UI pass draws this frame's widgets rather than
     * lagging a frame behind. */
    override fun drawUi(primitives: List<UiDrawPrimitive>) {
        val quads = primitives.filterIsInstance<UiDrawPrimitive.Quad>()
        require(quads.size <= MAX_UI_QUADS) {
            "UI quad count (${quads.size}) exceeds Renderer's DynamicMesh capacity ($MAX_UI_QUADS)."
        }
        val vertices = FloatArray(quads.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX)
        val indices = IntArray(quads.size * DynamicMesh.INDICES_PER_QUAD)
        var quadIndex = 0
        while (quadIndex < quads.size) {
            val quad = quads[quadIndex]
            val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX
            writeVertex(vertices, vertexBase + 0 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y, quad.color)
            writeVertex(vertices, vertexBase + 1 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.color)
            writeVertex(vertices, vertexBase + 2 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.color)
            writeVertex(vertices, vertexBase + 3 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.color)

            val vertexOffset = quadIndex * DynamicMesh.VERTICES_PER_QUAD
            val indexBase = quadIndex * DynamicMesh.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        uiMesh.update(vertices, indices)

        val glyphs = primitives.filterIsInstance<UiDrawPrimitive.Glyph>()
        require(glyphs.size <= MAX_UI_QUADS) {
            "UI glyph count (${glyphs.size}) exceeds Renderer's DynamicMesh capacity ($MAX_UI_QUADS)."
        }
        val glyphVertices = FloatArray(glyphs.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
        val glyphIndices = IntArray(glyphs.size * DynamicMesh.INDICES_PER_QUAD)
        var glyphIndex = 0
        while (glyphIndex < glyphs.size) {
            val glyph = glyphs[glyphIndex]
            val vertexBase = glyphIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX
            writeGlyphVertex(glyphVertices, vertexBase + 0 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y, glyph.u0, glyph.v0, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 1 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y, glyph.u1, glyph.v0, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 2 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y + glyph.h, glyph.u1, glyph.v1, glyph.color)
            writeGlyphVertex(glyphVertices, vertexBase + 3 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y + glyph.h, glyph.u0, glyph.v1, glyph.color)

            val vertexOffset = glyphIndex * DynamicMesh.VERTICES_PER_QUAD
            val indexBase = glyphIndex * DynamicMesh.INDICES_PER_QUAD
            glyphIndices[indexBase] = vertexOffset
            glyphIndices[indexBase + 1] = vertexOffset + 1
            glyphIndices[indexBase + 2] = vertexOffset + 2
            glyphIndices[indexBase + 3] = vertexOffset + 2
            glyphIndices[indexBase + 4] = vertexOffset + 3
            glyphIndices[indexBase + 5] = vertexOffset
            glyphIndex += 1
        }
        uiGlyphMesh.update(glyphVertices, glyphIndices)
    }

    private fun writeVertex(out: FloatArray, offset: Int, x: Float, y: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = color[0]
        out[offset + 3] = color[1]
        out[offset + 4] = color[2]
        out[offset + 5] = if (color.size > 3) color[3] else 1f
    }

    private fun writeGlyphVertex(out: FloatArray, offset: Int, x: Float, y: Float, u: Float, v: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = u
        out[offset + 3] = v
        out[offset + 4] = color[0]
        out[offset + 5] = color[1]
        out[offset + 6] = color[2]
        out[offset + 7] = if (color.size > 3) color[3] else 1f
    }

    override fun draw(camera: Camera, drawCalls: List<DrawCall>) {
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
                // drawCall.mesh is the render-api interface (only bind()/draw()/destroy()) --
                // cast to this backend's own concrete Mesh for vertexBuffer/indexBuffer/
                // indexCount, safe since this Renderer only ever runs against this module's
                // own Mesh instances (never a different backend's).
                val mesh = drawCall.mesh as Mesh
                setVertexBuffer(0u, WebGpuHandles.resolve(mesh.vertexBuffer.handle))
                setIndexBuffer(WebGpuHandles.resolve(mesh.indexBuffer.handle), meshIndexFormat)
                drawIndexed(mesh.indexCount.toUInt())
                drawIndex += 1
            }
            end()
        }

        // Second pass, same encoder, drawn on top of the 3D pass's output --
        // `loadOp = Load` (not `Clear`) is the whole trick, no separate framebuffer object
        // needed at all (WebGPU has no framebuffer object; a render pass just names a
        // texture view directly).
        if (uiMesh.drawIndexCount > 0 || uiGlyphMesh.drawIndexCount > 0) {
            encoder.beginRenderPass(
                RenderPassDescriptor(
                    colorAttachments = listOf(
                        RenderPassColorAttachment(
                            view = colorView,
                            loadOp = GPULoadOp.Load,
                            storeOp = GPUStoreOp.Store
                        )
                    )
                )
            ) {
                setPipeline(uiRenderPipeline.pipeline)
                setBindGroup(0u, uiRenderPipeline.screenSizeBindGroup)
                setVertexBuffer(0u, uiMesh.vertexBufferRef())
                setIndexBuffer(uiMesh.indexBufferRef(), DynamicMesh.indexFormat)
                drawIndexed(uiMesh.drawIndexCount.toUInt())

                if (uiGlyphMesh.drawIndexCount > 0) {
                    setPipeline(uiGlyphRenderPipeline.pipeline)
                    setBindGroup(0u, uiGlyphRenderPipeline.bindGroup)
                    setVertexBuffer(0u, uiGlyphMesh.vertexBufferRef())
                    setIndexBuffer(uiGlyphMesh.indexBufferRef(), DynamicMesh.indexFormat)
                    drawIndexed(uiGlyphMesh.drawIndexCount.toUInt())
                }
                end()
            }
        }

        device.queue.submit(listOf(encoder.finish()))
    }

    override fun destroy() {
        uniformBuffer?.close()
        uniformBuffer = null
        uniformBindGroup = null
        uiMesh.destroy()
        uiGlyphMesh.destroy()
    }

    private companion object {
        const val MAX_UI_QUADS = 256
    }
}
