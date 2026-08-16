// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
import io.github.ronjunevaldoz.awake.webgpu.debug.LineMesh
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.mesh.meshIndexFormat
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.RenderPassColorAttachment
import io.ygdrasil.webgpu.RenderPassDepthStencilAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.beginRenderPass

/** The 3D frame path -- `Renderer.draw`'s whole-frame orchestration (3D draw calls + debug
 * lines in one render pass, then the UI overlay pass on top of it) and debug-line staging.
 * See [Renderer]'s class doc comment for why this lives here as `internal` extension
 * functions rather than as members. */

/** Renders one frame: the 3D pass (every [drawCalls] entry plus any staged debug lines, into
 * the swapchain's current texture), then -- only if [Renderer.drawUi] built a UI pipeline at
 * least once and staged any runs -- a second pass on top of it. Named `performDraw`, not
 * `draw` -- [Renderer]'s `override fun draw(...)` (the actual `RenderRenderer` interface
 * method) is a one-line delegate to this extension function; an extension function can't
 * share its name with a member function it's called from without the member call winning
 * resolution and recursing into itself. */
internal fun Renderer.performDraw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
    swapchainManager.syncSurface()
    val device = graphicsDevice.wgpuContext.device
    val renderingContext = graphicsDevice.wgpuContext.renderingContext
    // wireframe with no wireframeRenderPipeline built (wireframeSupport = false, see
    // WebGpuGameApplication) just keeps drawing filled -- mirrors Vulkan's Renderer
    // .pipelineFor fallback, not a hard requirement to opt in.
    val useWireframe = wireframe && wireframeRenderPipeline != null
    val activeRenderPipeline = if (useWireframe) wireframeRenderPipeline!! else renderPipeline
    val pipeline = WebGpuHandles.resolve<GPURenderPipeline>(activeRenderPipeline.graphicsPipeline[0])
    // Separate uniform-resource sets per pipeline object -- see ensureWireframeUniformResources'
    // own doc comment for why a bind group can't be shared across two "auto"-layout pipelines.
    if (useWireframe) ensureWireframeUniformResources(pipeline) else ensureUniformResources(pipeline)
    val activeUniformBuffer = if (useWireframe) wireframeUniformBuffer!! else uniformBuffer!!
    val activeUniformBindGroup = if (useWireframe) wireframeUniformBindGroup!! else uniformBindGroup!!

    // Trimmed to the canvas: WebGPU rejects an out-of-bounds viewport/scissor outright,
    // invalidating the whole command buffer (same reason this file's UI ClipRun clamps).
    val sceneRect = sceneViewport?.clampedTo(
        renderingContext.width.toFloat(),
        renderingContext.height.toFloat(),
    )
    val aspect = sceneRect?.aspect
        ?: (renderingContext.width.toFloat() / renderingContext.height.toFloat())
    val viewProjection = camera.viewProjectionMatrix(aspect, clipSpace)
    // Debug lines are already in world space, so their MVP is exactly viewProjection.
    lineRenderPipeline.writeMvp(viewProjection.data)

    // vec4f (not vec3f) for both -- see triangle.wgsl's own Uniforms struct doc comment.
    val lightFloats = floatArrayOf(
        light.direction.x,
        light.direction.y,
        light.direction.z,
        0f,
        light.color.x,
        light.color.y,
        light.color.z,
        0f,
    )

    val encoder = device.createCommandEncoder()
    val colorView = renderingContext.getCurrentTexture().createView()

    encoder.beginRenderPass(
        RenderPassDescriptor(
            colorAttachments = listOf(
                RenderPassColorAttachment(
                    view = colorView,
                    loadOp = GPULoadOp.Clear,
                    clearValue = clearColorValue,
                    storeOp = GPUStoreOp.Store,
                ),
            ),
            depthStencilAttachment = RenderPassDepthStencilAttachment(
                view = requireNotNull(swapchainManager.depthTextureView),
                depthClearValue = 1.0f,
                depthLoadOp = GPULoadOp.Clear,
                depthStoreOp = GPUStoreOp.Store,
            ),
        ),
    ) {
        // Confines the scene to an editor's viewport panel; the UI pass keeps the full canvas.
        sceneRect?.let { rect ->
            setViewport(rect.x, rect.y, rect.width, rect.height, 0f, 1f)
            setScissorRect(rect.x.toUInt(), rect.y.toUInt(), rect.width.toUInt(), rect.height.toUInt())
        }
        var drawIndex = 0
        while (drawIndex < drawCalls.size) {
            val drawCall = drawCalls[drawIndex]
            // A mesh whose format has neither the primary pipeline nor an additionalPipelines
            // entry is skipped rather than drawn through a pipeline that expects a different
            // vertex layout, matching Vulkan's Renderer.pipelinesByFormat skip-on-mismatch
            // guard (see that class's doc comment).
            val isPrimaryFormat = drawCall.mesh.format == primaryVertexFormat
            val extraPipeline = if (isPrimaryFormat) null else additionalPipelines[drawCall.mesh.format]
            val extraMaterial = (drawCall.material as? Material)?.takeIf { it.hasTexture }
            if (!isPrimaryFormat && (extraPipeline == null || extraMaterial == null)) {
                drawIndex += 1
                continue
            }
            // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
            // Camera.viewProjectionMatrix's docs), matching vulkanMain's Renderer.
            val mvp = drawCall.model * viewProjection
            if (extraPipeline != null && extraMaterial != null) {
                // Textured path: the material owns its uniform buffer + bind group. Its
                // Uniforms is mvp + light + model + cameraPosition (textured.wgsl's PBR
                // specular needs a world-space position and view vector) -- the same 44 floats
                // vulkanMain's prepareDrawCalls writes for this format. Wireframe has no
                // companion pipeline per additional format, so these stay filled (same
                // fallback as Vulkan's pipelineFor).
                val texturedPipeline = WebGpuHandles.resolve<GPURenderPipeline>(extraPipeline.graphicsPipeline[0])
                setPipeline(texturedPipeline)
                extraMaterial.updateUniformBuffer(
                    mvp.data + lightFloats + drawCall.model.data +
                        floatArrayOf(camera.eye.x, camera.eye.y, camera.eye.z, 0f),
                )
                setBindGroup(0u, extraMaterial.bindGroupFor(texturedPipeline))
            } else {
                setPipeline(pipeline)
                device.queue.writeBuffer(activeUniformBuffer, 0uL, fastArrayBufferOf(mvp.data + lightFloats))
                setBindGroup(0u, activeUniformBindGroup)
            }
            // drawCall.mesh is the render-api interface (only bind()/draw()/destroy()) --
            // cast to this backend's own concrete Mesh for vertexBuffer/indexBuffer/
            // indexCount, safe since this Renderer only ever runs against this module's
            // own Mesh instances (never a different backend's).
            val mesh = drawCall.mesh as Mesh
            setVertexBuffer(0u, WebGpuHandles.resolve(mesh.vertexBuffer.handle))
            // `extraPipeline == null`: an additional pipeline has no LineList companion, so a
            // line index buffer would be fed to a TriangleList pipeline.
            if (useWireframe && extraPipeline == null) {
                // Same vertex buffer, a different (LineList-shaped) index buffer derived
                // from this mesh's own triangle indices -- see Mesh's lineIndexBuffer doc
                // comment.
                setIndexBuffer(WebGpuHandles.resolve(mesh.lineIndexBuffer.handle), meshIndexFormat)
                drawIndexed(mesh.lineIndexCount.toUInt())
            } else {
                setIndexBuffer(WebGpuHandles.resolve(mesh.indexBuffer.handle), meshIndexFormat)
                drawIndexed(mesh.indexCount.toUInt())
            }
            drawIndex += 1
        }

        // Debug lines share this render pass; lineRenderPipeline's depthCompare = Always keeps
        // them never depth-tested against scene geometry.
        if (lineMesh.vertexCount > 0) {
            setPipeline(lineRenderPipeline.pipeline)
            setBindGroup(0u, lineRenderPipeline.bindGroup)
            setVertexBuffer(0u, lineMesh.vertexBufferRef())
            draw(lineMesh.vertexCount.toUInt())
        }
        end()
    }

    // Second pass, same encoder, on top of the 3D output: `loadOp = Load` (not `Clear`) is the
    // whole trick. Only recorded once drawUi() has built a UI pipeline and staged runs.
    val quadPipeline = uiRenderPipeline
    if (quadPipeline != null && uiRuns.isNotEmpty()) {
        quadPipeline.writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
        uiGlyphRenderPipeline?.writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
        uiTextureRenderPipeline?.writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
        uiRoundedQuadRenderPipeline?.writeScreenSize(renderingContext.width.toFloat(), renderingContext.height.toFloat())
        encoder.beginRenderPass(
            RenderPassDescriptor(
                colorAttachments = listOf(
                    RenderPassColorAttachment(
                        view = colorView,
                        loadOp = GPULoadOp.Load,
                        storeOp = GPUStoreOp.Store,
                    ),
                ),
            ),
        ) {
            // Walk this frame's runs (staged by drawUi(), see Renderer.UiRun's doc comment)
            // in original paint order, switching pipeline at each run boundary -- see
            // Vulkan's Renderer.recordCommandBuffer()'s doc comment (this mirrors it) for
            // why a fixed per-type pass order broke cross-type paint order.
            val glyphPipeline = uiGlyphRenderPipeline
            val texturePipeline = uiTextureRenderPipeline
            var runIndex = 0
            while (runIndex < uiRuns.size) {
                when (val run = uiRuns[runIndex]) {
                    is Renderer.UiRun.QuadRun -> {
                        setPipeline(quadPipeline.pipeline)
                        setBindGroup(0u, quadPipeline.screenSizeBindGroup)
                        setVertexBuffer(0u, run.mesh.vertexBufferRef())
                        setIndexBuffer(run.mesh.indexBufferRef(), DynamicMesh.indexFormat)
                        drawIndexed(run.mesh.drawIndexCount.toUInt())
                    }
                    is Renderer.UiRun.RoundedQuadRun -> {
                        val roundedQuadPipeline = uiRoundedQuadRenderPipeline
                        if (roundedQuadPipeline != null) {
                            setPipeline(roundedQuadPipeline.pipeline)
                            setBindGroup(0u, roundedQuadPipeline.screenSizeBindGroup)
                            setVertexBuffer(0u, run.mesh.vertexBufferRef())
                            setIndexBuffer(run.mesh.indexBufferRef(), DynamicMesh.indexFormat)
                            drawIndexed(run.mesh.drawIndexCount.toUInt())
                        }
                    }
                    is Renderer.UiRun.GlyphRun -> {
                        if (glyphPipeline != null) {
                            setPipeline(glyphPipeline.pipeline)
                            setBindGroup(0u, glyphPipeline.bindGroup)
                            setVertexBuffer(0u, run.mesh.vertexBufferRef())
                            setIndexBuffer(run.mesh.indexBufferRef(), DynamicMesh.indexFormat)
                            drawIndexed(run.mesh.drawIndexCount.toUInt())
                        }
                    }
                    is Renderer.UiRun.ClipRun -> {
                        // Sets the scissor rect at this point in paint order; clamped to the render
                        // target bounds because WebGPU's scissor validation rejects an out-of-bounds
                        // rect outright (invalidating the whole command buffer), unlike Vulkan.
                        val maxX = renderingContext.width.toInt()
                        val maxY = renderingContext.height.toInt()
                        val x = run.rect.x.toInt().coerceIn(0, maxX)
                        val y = run.rect.y.toInt().coerceIn(0, maxY)
                        val width = run.rect.width.toInt().coerceAtLeast(0).coerceAtMost(maxX - x)
                        val height = run.rect.height.toInt().coerceAtLeast(0).coerceAtMost(maxY - y)
                        setScissorRect(x.toUInt(), y.toUInt(), width.toUInt(), height.toUInt())
                    }
                    is Renderer.UiRun.TextureRun -> {
                        // Render-target-backed textured quads (e.g. a minimap), one draw call per
                        // primitive; each binds its material's lazily-cached bind group.
                        if (texturePipeline != null) {
                            setPipeline(texturePipeline.pipeline)
                            var textureIndex = 0
                            while (textureIndex < run.primitives.size) {
                                val p = run.primitives[textureIndex]
                                val material = p.material as Material
                                setBindGroup(0u, texturePipeline.bindGroupFor(material))
                                textureQuadMesh.update(p.vertices, p.indices)
                                setVertexBuffer(0u, textureQuadMesh.vertexBufferRef())
                                setIndexBuffer(textureQuadMesh.indexBufferRef(), DynamicMesh.indexFormat)
                                drawIndexed(textureQuadMesh.drawIndexCount.toUInt())
                                textureIndex += 1
                            }
                        }
                    }
                }
                runIndex += 1
            }
            end()
        }
    }

    device.queue.submit(listOf(encoder.finish()))
}

/** Stages this frame's world-space debug lines (e.g. a frustum wireframe) -- rewrites
 * [Renderer.lineMesh]'s buffer but issues no GPU commands itself, same "stage now, consume
 * on next draw" pattern as `performDrawUi`. Call before [performDraw] each frame. Named
 * `performDrawDebugLines`, not `drawDebugLines` -- see [performDraw]'s doc comment for why. */
internal fun Renderer.performDrawDebugLines(lines: List<LineSegment>) {
    require(lines.size <= Renderer.MAX_DEBUG_LINES) {
        "Debug line count (${lines.size}) exceeds Renderer's LineMesh capacity (${Renderer.MAX_DEBUG_LINES})."
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
