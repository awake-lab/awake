// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.webgpu.debug.LineMesh
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.material.Material
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
    private val lineRenderPipeline: LineRenderPipeline,
    private val uiShaderCode: ByteArray,
    private val uiGlyphShaderCode: ByteArray,
    commandPool: Long,
    maxFramesInFlight: Int
) : RenderRenderer {
    private val graphicsDevice = graphicsDevice
    private val swapchainManager = swapchainManager
    private val renderPipeline = renderPipeline

    private var uniformBuffer: GPUBuffer? = null
    private var uniformBindGroup: GPUBindGroup? = null

    // Lazily built on the first drawUi() call of any kind (uiRenderPipeline) and on the
    // first call that passes a non-null font (uiGlyphRenderPipeline) -- see
    // ensureUiQuadPipeline()/ensureGlyphPipeline()'s doc comments. A game that never calls
    // drawUi never builds either pipeline at all.
    private var uiRenderPipeline: UiRenderPipeline? = null
    private var uiGlyphRenderPipeline: UiGlyphRenderPipeline? = null

    // Rewritten by drawUi() (called before draw() -- see WebGpuGameApplication.onRender()'s
    // ordering) so the UI pass, appended to the SAME command encoder as the 3D pass inside
    // draw(), always draws this frame's widgets, not last frame's.
    private val uiMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS)
    private val uiGlyphMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)

    // Rewritten by drawDebugLines() (staged before draw(), same pattern as uiMesh above).
    private val lineMesh = LineMesh(graphicsDevice, MAX_DEBUG_LINES)

    /** Uploads [geometry] as a GPU mesh, on demand -- see [RenderRenderer.createMesh]'s doc
     * comment. `runOneTimeCommands` is unused by this backend's `Mesh` (see its own doc
     * comment), so an empty lambda is passed. */
    override fun createMesh(geometry: MeshGeometry): RenderMesh =
        Mesh(graphicsDevice, {}, geometry.vertices, geometry.indices)

    /** Builds a [Material] -- this backend's `Material` is still a compile-only stub (see
     * its own doc comment), so this just constructs it, matching this backend's existing
     * (pre-refactor) behavior of never calling `createResources`. [texture]/[renderTarget]
     * are accepted for interface parity with the Vulkan backend but otherwise unused here. */
    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?): RenderMaterial =
        Material(graphicsDevice)

    /** Not yet implemented on this backend -- see docs/MVP_PLAN.md's `RenderTarget` decision
     * log entry (Vulkan-first rollout, WebGPU is a follow-up slice). */
    override fun createRenderTarget(width: Int, height: Int): RenderTarget =
        TODO("createRenderTarget not yet implemented on WebGPU -- see RenderTarget decision log")

    /** Not yet implemented on this backend -- see [createRenderTarget]'s doc comment. */
    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) {
        TODO("renderToTexture not yet implemented on WebGPU -- see RenderTarget decision log")
    }

    /** Not yet implemented on this backend -- see [createRenderTarget]'s doc comment. */
    override suspend fun readPixels(target: RenderTarget): TextureAsset =
        TODO("readPixels not yet implemented on WebGPU -- see RenderTarget decision log")

    /** Builds [uiRenderPipeline] on the first [drawUi] call of any kind -- quad rendering
     * doesn't need a font, so this doesn't wait for one. Cached after the first build (a
     * game calls [drawUi] every frame). */
    private fun ensureUiQuadPipeline() {
        if (uiRenderPipeline != null) return
        uiRenderPipeline = UiRenderPipeline(graphicsDevice, swapchainManager, uiShaderCode)
    }

    /** Builds [uiGlyphRenderPipeline] on the first [drawUi] call that passes a non-null
     * [font] -- cached after that (a game calls [drawUi] with the same font every frame). */
    private fun ensureGlyphPipeline(font: BitmapFont) {
        if (uiGlyphRenderPipeline != null) return
        uiGlyphRenderPipeline = UiGlyphRenderPipeline(graphicsDevice, swapchainManager, uiGlyphShaderCode, font)
    }

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
     * lagging a frame behind. Lazily builds the (quad) UI pipeline on first call, and the
     * glyph pipeline on the first call that passes a non-null [font] -- see
     * [ensureUiQuadPipeline]/[ensureGlyphPipeline]'s doc comments. */
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont?) {
        ensureUiQuadPipeline()
        if (font != null) ensureGlyphPipeline(font)
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

    /** Stages this frame's world-space debug lines (e.g. a frustum wireframe) -- rewrites
     * [lineMesh]'s buffer but issues no GPU commands itself, same "stage now, consume on
     * next draw" pattern as [drawUi]. Call before [draw] each frame. */
    override fun drawDebugLines(lines: List<LineSegment>) {
        require(lines.size <= MAX_DEBUG_LINES) {
            "Debug line count (${lines.size}) exceeds Renderer's LineMesh capacity ($MAX_DEBUG_LINES)."
        }
        val vertices = FloatArray(lines.size * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX)
        var lineIndex = 0
        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            val vertexBase = lineIndex * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX
            writeLineVertex(vertices, vertexBase, line.start.x, line.start.y, line.start.z, line.color)
            writeLineVertex(vertices, vertexBase + LineMesh.FLOATS_PER_VERTEX, line.end.x, line.end.y, line.end.z, line.color)
            lineIndex += 1
        }
        lineMesh.update(vertices)
    }

    private fun writeLineVertex(out: FloatArray, offset: Int, x: Float, y: Float, z: Float, color: FloatArray) {
        out[offset] = x
        out[offset + 1] = y
        out[offset + 2] = z
        out[offset + 3] = color[0]
        out[offset + 4] = color[1]
        out[offset + 5] = color[2]
        out[offset + 6] = if (color.size > 3) color[3] else 1f
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
        // Debug lines are already in world space, so their MVP is exactly viewProjection.
        lineRenderPipeline.writeMvp(viewProjection.data)

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

            // Debug lines (e.g. a frustum wireframe), same render pass as the 3D draw
            // calls above. This backend's 3D pass has no depth attachment at all today
            // (see LineRenderPipeline's doc comment), so this doesn't depth-test against
            // scene geometry -- matches this pass's existing (pre-existing, not new) lack
            // of depth testing for every other draw call too.
            if (lineMesh.vertexCount > 0) {
                setPipeline(lineRenderPipeline.pipeline)
                setBindGroup(0u, lineRenderPipeline.bindGroup)
                setVertexBuffer(0u, lineMesh.vertexBufferRef())
                draw(lineMesh.vertexCount.toUInt())
            }
            end()
        }

        // Second pass, same encoder, drawn on top of the 3D pass's output --
        // `loadOp = Load` (not `Clear`) is the whole trick, no separate framebuffer object
        // needed at all (WebGPU has no framebuffer object; a render pass just names a
        // texture view directly). Only recorded once drawUi() has actually built a UI
        // pipeline at least once -- a game that never calls drawUi never pays for this pass.
        val quadPipeline = uiRenderPipeline
        if (quadPipeline != null && (uiMesh.drawIndexCount > 0 || uiGlyphMesh.drawIndexCount > 0)) {
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
                setPipeline(quadPipeline.pipeline)
                setBindGroup(0u, quadPipeline.screenSizeBindGroup)
                setVertexBuffer(0u, uiMesh.vertexBufferRef())
                setIndexBuffer(uiMesh.indexBufferRef(), DynamicMesh.indexFormat)
                drawIndexed(uiMesh.drawIndexCount.toUInt())

                val glyphPipeline = uiGlyphRenderPipeline
                if (glyphPipeline != null && uiGlyphMesh.drawIndexCount > 0) {
                    setPipeline(glyphPipeline.pipeline)
                    setBindGroup(0u, glyphPipeline.bindGroup)
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
        uiRenderPipeline?.destroy()
        uiGlyphRenderPipeline?.destroy()
        uiMesh.destroy()
        uiGlyphMesh.destroy()
        lineMesh.destroy()
    }

    private companion object {
        const val MAX_UI_QUADS = 256
        const val MAX_DEBUG_LINES = 64
    }
}
