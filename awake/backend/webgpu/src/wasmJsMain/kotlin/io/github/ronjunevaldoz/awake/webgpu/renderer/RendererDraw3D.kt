// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.render.command.BufferHandle
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.renderer.skyboxUniformFloats
import io.github.ronjunevaldoz.awake.webgpu.debug.LineMesh
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.debug.SkyboxRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuCommandRecorder
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh
import io.ygdrasil.webgpu.GPULoadOp
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
    val primaryHandle = activeRenderPipeline.handle
    // Separate uniform-resource sets per pipeline object -- see ensureWireframeUniformResources'
    // own doc comment for why a bind group can't be shared across two "auto"-layout pipelines.
    if (useWireframe) {
        ensureWireframeUniformResources(primaryHandle.pipeline)
    } else {
        ensureUniformResources(primaryHandle.pipeline)
    }
    val primary = PrimaryPipelineBinding(
        pipeline = primaryHandle,
        uniformBuffer = if (useWireframe) wireframeUniformBuffer!! else uniformBuffer!!,
        binding = if (useWireframe) wireframeUniformBinding!! else uniformBinding!!,
        wireframe = useWireframe,
    )

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
    // Reuses this frame's already-built viewProjection: the sky needs its inverse to turn each
    // pixel back into a world-space ray. Null whenever the sky isn't drawn this frame.
    val skybox = skyboxRenderPipeline?.takeIf { showEnvironment }
    val skyboxUniforms = skybox?.let {
        skyboxUniformFloats(viewProjection, camera.eye, light.direction, horizonColor, zenithColor)
    }
    skyboxUniforms?.let { skybox?.writeUniforms(it) }

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

    // Prepared before the pass opens: pipeline resolution, uniform writes, instance-buffer
    // fills. See prepareOpaqueDraws' own doc comment for why hoisting those out of the encoder
    // changes nothing (queue writes never interleaved with encoding to begin with).
    val opaqueDraws = prepareOpaqueDraws(drawCalls, camera.eye, viewProjection, lightFloats, primary)

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
        // The sky goes FIRST, before any geometry -- depth test/write are both off on this
        // pipeline, so it neither occludes nor is occluded by what follows. No vertex buffer:
        // the vertex shader generates a full-screen triangle from vertex_index.
        if (skybox != null && skyboxUniforms != null) {
            setPipeline(skybox.pipeline)
            setBindGroup(0u, skybox.bindGroup)
            draw(SkyboxRenderPipeline.FULLSCREEN_TRIANGLE_VERTICES)
        }
        // Every opaque draw, grouped by pipeline -- one implementation, shared verbatim with
        // the Vulkan backend, reached through this backend's own CommandRecorder.
        sharedOpaqueFeature.recordCommands(
            recorder = WebGpuCommandRecorder(this),
            primaryPipeline = primary.pipeline,
            grouped = opaqueDraws,
            // Debug lines used to be recorded last, after every group; the shared order puts
            // them between the primary group and the rest, matching Vulkan. Harmless for depth
            // (lineRenderPipeline is depthCompare = Always, depthWriteEnabled = false), but a
            // line under a later-drawn textured mesh is now painted over rather than on top.
            lines = LineDraw(lineRenderPipeline, lineMesh),
        )
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

/** This frame's staged debug lines as one non-indexed [PreparedDraw] -- the shared opaque
 * feature draws it between the primary pipeline's group and the rest. No index buffer at all:
 * consecutive vertex pairs are the segments under `LineList`, so [elementCount] is a vertex
 * count, and 0 vertices means the feature skips the whole thing (bind included). */
private class LineDraw(
    private val linePipeline: LineRenderPipeline,
    private val lineMesh: LineMesh,
) : PreparedDraw {
    override val pipeline: PipelineHandle get() = linePipeline.handle
    override val materialBinding: MaterialBinding get() = linePipeline.bindGroupHandle
    override val vertexBuffer: BufferHandle get() = lineMesh.vertexBinding
    override val indexBuffer: BufferHandle? get() = null
    override val elementCount: Int get() = lineMesh.vertexCount
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

/** `[metallic, roughness, pad, pad, baseColorFactor.rgba, emissiveFactor.rgb, pad]` -- the
 * textured/glTF pipeline's factor multipliers (see `textured.wgsl`'s Uniforms), same packing
 * and defaults as Vulkan's own `RendererDraw3D.pbrTexturedMaterialFloats`. Defaults to
 * factor = 1 / emissive = 0 (a no-op multiply), matching this pipeline's behavior before these
 * fields existed. */
internal fun pbrTexturedMaterialFloats(drawCall: DrawCall): FloatArray {
    val supplied = drawCall.extraUniformFloats
    if (supplied.size >= PBR_TEXTURED_MATERIAL_FLOATS) return supplied.copyOf(PBR_TEXTURED_MATERIAL_FLOATS)
    return floatArrayOf(
        DEFAULT_METALLIC_FACTOR, DEFAULT_ROUGHNESS_FACTOR, 0f, 0f,
        1f, 1f, 1f, 1f,
        0f, 0f, 0f, 0f,
    )
}

/** `[fogColor.rgb, fogDensity]` -- density rides in the 4th component, matching
 * `textured.wgsl`'s `fogColor : vec4f` (see `UniformFields.FogColor`). Same packing as Vulkan's own
 * `RendererDraw3D.fogFloats`. The primary path here is `triangle.wgsl`, which has no worldPos/
 * cameraPosition to fog against, so only the textured path gets this. */
internal fun Renderer.fogFloats(): FloatArray =
    floatArrayOf(fogColor[0], fogColor[1], fogColor[2], fogDensity)

private const val PBR_TEXTURED_MATERIAL_FLOATS = 12
private const val DEFAULT_METALLIC_FACTOR = 1f
private const val DEFAULT_ROUGHNESS_FACTOR = 1f
