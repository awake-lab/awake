// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException
import kotlin.math.abs

/** The 3D frame path -- [Renderer.draw]'s whole-frame orchestration (wait/acquire -> update
 * uniforms -> record -> submit -> present), the shared per-draw-call recording loop, the
 * swapchain command-buffer recording (3D pass + UI overlay pass), the offscreen one-time-
 * command runner both [Renderer.renderToTexture]/[Renderer.readPixels] use, and debug-line
 * staging. See [Renderer]'s class doc comment for why this lives here as `internal` extension
 * functions rather than as members. */

/** Renders one frame: waits for this frame-in-flight slot, acquires a swapchain image,
 * prepares each [DrawCall]'s MVP matrix (model combined with [camera]'s view/projection)
 * into a concrete material uniform slot, records and submits a command buffer that draws
 * every call in order, then presents. Mutable per-frame resources (material uniforms,
 * debug-line uniforms/buffers, and UI dynamic meshes/descriptors) are indexed by the same
 * frame slot, so this path only waits that slot's fence rather than stalling the entire
 * device after every submit.
 *
 * Named `performDraw`, not `draw` -- [Renderer]'s `override fun draw(...)` (the actual
 * `RenderRenderer` interface method) is a one-line delegate to this extension function; an
 * extension function can't share its name with a member function it's called from without
 * the member call winning resolution and recursing into itself. Frame acquisition itself
 * is [acquireSwapchainImage] ([RendererSwapchainAcquire.kt]), split into its own file to
 * keep both detekt's method-length limit and this file's function-count limit. */
internal fun Renderer.performDraw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
    val currentFrame = swapchainManager.currentFrame
    val imageIndex = acquireSwapchainImage(currentFrame) ?: return

    val aspect = swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat()
    val viewProjection = camera.viewProjectionMatrix(aspect, clipSpace)
    // Debug lines are already in world space (no per-line model matrix), so their MVP
    // is exactly this frame's viewProjection.
    lineRenderPipeline.writeMvp(currentFrame, viewProjection.data)
    val lightViewProjection = if (shadowMap != null) lightViewProjection(light) else null
    val materialUsage = mutableMapOf<RenderMaterial, Int>()
    val preparedDrawCalls =
        prepareDrawCalls(currentFrame, viewProjection, drawCalls, light, lightViewProjection, materialUsage)

    // Runs BEFORE the swapchain command buffer is recorded: the main pass's fragment shader
    // samples this frame's shadow map, so its depth content must already be complete (the
    // fence wait inside runOffscreenCommands guarantees that) by the time recordCommandBuffer
    // binds the primary pipeline below. Skipped (not just a no-op render) whenever shadows
    // aren't supported/enabled -- see performShadowPass's own doc comment.
    performShadowPass(preparedDrawCalls)

    Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
    recordCommandBuffer(commandBuffers[currentFrame], currentFrame, imageIndex, preparedDrawCalls)

    val waitSemaphores = arrayOf(swapchainManager.imageAvailableSemaphores[currentFrame])
    val waitStages =
        intArrayOf(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value)
    val signalSemaphores = arrayOf(swapchainManager.renderFinishedSemaphores[imageIndex])

    val submitInfo = VkSubmitInfo(
        pWaitSemaphores = waitSemaphores,
        pWaitDstStageMask = waitStages,
        pCommandBuffers = arrayOf(commandBuffers[currentFrame]),
        pSignalSemaphores = signalSemaphores
    )

    Vulkan.vkQueueSubmit(graphicsQueue, arrayOf(submitInfo), swapchainManager.inFlightFences[currentFrame])

    val presentInfo = VkPresentInfoKHR(
        pWaitSemaphores = signalSemaphores,
        pSwapchains = arrayOf(swapchainManager.swapChain),
        pImageIndices = intArrayOf(imageIndex),
        pResults = VkResult.values()
    )

    try {
        Vulkan.vkQueuePresentKHR(presentQueue, presentInfo)
    } catch (e: VkResultException) {
        when (e.result) {
            VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> recreateSwapChain()
            else -> throw e
        }
    }

    swapchainManager.currentFrame = (currentFrame + 1) % commandBuffers.size
}

/** Binds+draws each [drawCalls] entry against its own resolved [PreparedDrawCall.pipeline]'s
 * layout -- shared by [recordCommandBuffer] (the swapchain frame, which groups by pipeline
 * and binds each group's pipeline before calling this) and [Renderer.renderToTexture] (an
 * offscreen frame, which only ever resolves the primary pipeline, already bound before this
 * is called there), so neither duplicates this loop. */
internal fun Renderer.recordDrawCalls(commandBuffer: Long, drawCalls: List<PreparedDrawCall>) {
    var drawIndex = 0
    val drawCount = drawCalls.size
    while (drawIndex < drawCount) {
        val prepared = drawCalls[drawIndex]
        prepared.drawCall.mesh.bind(commandBuffer)
        prepared.material.bind(
            commandBuffer,
            prepared.pipeline.pipelineLayout,
            prepared.frameIndex,
            prepared.uniformSlotIndex
        )
        prepared.drawCall.mesh.draw(commandBuffer)
        drawIndex += 1
    }
}

internal fun Renderer.recordCommandBuffer(
    commandBuffer: Long,
    frameIndex: Int,
    acquiredImageIndex: Int,
    drawCalls: List<PreparedDrawCall>
) {
    val beginInfo = VkCommandBufferBeginInfo(
        flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value,
    )
    Vulkan.vkBeginCommandBuffer(commandBuffer, beginInfo)

    val renderPassInfo = VkRenderPassBeginInfo(
        renderPass = renderPipeline.renderPass,
        framebuffer = framebuffers[acquiredImageIndex],
        renderArea = VkRect2D(
            extent = swapchainManager.extent
        ),
        pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue)
    )
    Vulkan.vkCmdBeginRenderPass(
        commandBuffer,
        renderPassInfo,
        VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE
    )

    // Grouped by resolved pipeline (see prepareDrawCalls) instead of 3 hardcoded blocks --
    // the primary group is drawn first (even if empty, renderPipeline is still bound first,
    // matching this method's old unconditional bind-before-anything-else behavior), then
    // debug lines, then every other format's group -- same relative draw order this method
    // always had, now generalized to however many pipelines pipelinesByFormat actually holds.
    val groupedDrawCalls = drawCalls.groupBy { it.pipeline }
    renderPipeline.bind(commandBuffer)
    val viewport = VkViewport(
        width = swapchainManager.extent.width.toFloat(),
        height = swapchainManager.extent.height.toFloat(),
    )
    Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
    val scissor = VkRect2D(
        extent = swapchainManager.extent
    )
    Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
    recordDrawCalls(commandBuffer, groupedDrawCalls[renderPipeline] ?: emptyList())

    // Debug lines (e.g. a frustum wireframe), same render pass/depth attachment as
    // the 3D draw calls above -- real depth-testing against scene geometry, not an
    // X-ray overlay. Still inside this pass, before it ends.
    lineRenderPipeline.bind(commandBuffer, frameIndex)
    lineMesh.bind(frameIndex, commandBuffer)
    lineMesh.draw(frameIndex, commandBuffer)

    groupedDrawCalls.forEach { (pipeline, group) ->
        if (pipeline === renderPipeline) return@forEach
        pipeline.bind(commandBuffer)
        recordDrawCalls(commandBuffer, group)
    }

    Vulkan.vkCmdEndRenderPass(commandBuffer)

    // Second pass, same command buffer, drawn on top of the 3D pass's output (that
    // pass's finalLayout leaves the image in COLOR_ATTACHMENT_OPTIMAL specifically so
    // this pass can pick up from there -- see RenderPipeline.kt's createRenderPass).
    // Only recorded once drawUi() has actually built a UI pipeline at least once (a game
    // that never calls drawUi never pays for this pass at all) -- mirrors WebGPU's
    // Renderer.draw()'s equivalent `uiMesh.drawIndexCount > 0` guard.
    val uiPipeline = uiRenderPipeline
    if (uiPipeline != null) {
        val uiRenderPassInfo = VkRenderPassBeginInfo(
            renderPass = uiPipeline.renderPass,
            framebuffer = uiFramebuffers[acquiredImageIndex],
            renderArea = VkRect2D(extent = swapchainManager.extent)
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, uiRenderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))

        // Walk this frame's runs (staged by drawUi(), see UiRun's doc comment) in
        // original paint order, switching pipeline at each run boundary -- this is what
        // makes a dropdown's overlay quad draw after a sibling button's OWN label glyph
        // when it comes later in the primitive list, instead of a fixed "all quads, then
        // all glyphs, then all textures" pass order always drawing every glyph on top of
        // every quad regardless of source order.
        val glyphPipeline = uiGlyphRenderPipeline
        val texturePipeline = uiTextureRenderPipeline
        val roundedQuadPipeline = uiRoundedQuadRenderPipeline
        var runIndex = 0
        var textureSlotIndex = 0
        while (runIndex < uiRuns.size) {
            when (val run = uiRuns[runIndex]) {
                is Renderer.UiRun.QuadRun -> {
                    uiPipeline.bind(commandBuffer)
                    run.mesh.bind(frameIndex, commandBuffer)
                    run.mesh.draw(frameIndex, commandBuffer)
                }
                is Renderer.UiRun.RoundedQuadRun -> {
                    if (roundedQuadPipeline != null) {
                        roundedQuadPipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.GlyphRun -> {
                    if (glyphPipeline != null) {
                        glyphPipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.ClipRun -> {
                    // Clamped to the swapchain's own extent: nested scroll/clip regions can
                    // accumulate a few px of floating-point rounding past the frame edge.
                    // Vulkan tolerates an out-of-bounds scissor rect silently on most
                    // drivers, but it's equally out-of-spec here -- clamp defensively rather
                    // than rely on driver leniency (see WebGPU's Renderer.kt equivalent,
                    // which hits a hard validation error for the exact same unclamped rect).
                    val maxX = swapchainManager.extent.width
                    val maxY = swapchainManager.extent.height
                    val x = run.rect.x.toInt().coerceIn(0, maxX)
                    val y = run.rect.y.toInt().coerceIn(0, maxY)
                    val width = run.rect.width.toInt().coerceAtLeast(0).coerceAtMost(maxX - x)
                    val height = run.rect.height.toInt().coerceAtLeast(0).coerceAtMost(maxY - y)
                    val scissor = VkRect2D(
                        offset = VkOffset2D(x, y),
                        extent = VkExtent2D(width, height)
                    )
                    Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
                }
                is Renderer.UiRun.TextureRun -> {
                    // Render-target-backed textured quads (e.g. a minimap), one draw
                    // call per primitive. Each primitive gets a distinct per-frame mesh
                    // and descriptor slot so command recording never overwrites geometry
                    // or image bindings already referenced by an earlier texture draw.
                    if (texturePipeline != null) {
                        var textureIndex = 0
                        while (textureIndex < run.primitives.size) {
                            val primitive = run.primitives[textureIndex]
                            val material = primitive.material as Material
                            val mesh = textureMeshForPrimitive(textureSlotIndex)
                            texturePipeline.bindMaterial(
                                commandBuffer,
                                frameIndex,
                                textureSlotIndex,
                                material.samplerHandle,
                                material.imageViewHandle
                            )
                            mesh.update(frameIndex, primitive.vertices, primitive.indices)
                            mesh.bind(frameIndex, commandBuffer)
                            mesh.draw(frameIndex, commandBuffer)
                            textureIndex += 1
                            textureSlotIndex += 1
                        }
                    }
                }
            }
            runIndex += 1
        }

        Vulkan.vkCmdEndRenderPass(commandBuffer)
    } else {
        val presentTransitionPassInfo = VkRenderPassBeginInfo(
            renderPass = presentTransitionRenderPass,
            framebuffer = presentTransitionFramebuffers[acquiredImageIndex],
            renderArea = VkRect2D(extent = swapchainManager.extent)
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            presentTransitionPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE
        )
        Vulkan.vkCmdEndRenderPass(commandBuffer)
    }

    Vulkan.vkEndCommandBuffer(commandBuffer)
}

/** Waits until the current frame-in-flight slot is no longer referenced by the GPU before
 * CPU code rewrites host-visible resources assigned to that slot. This is intentionally much
 * narrower than `vkDeviceWaitIdle`: other submitted frame slots may continue running. */
internal fun Renderer.waitForCurrentFrameResourceSlot() {
    val currentFrame = swapchainManager.currentFrame
    val fence = swapchainManager.inFlightFences.getOrNull(currentFrame) ?: return
    if (fence == 0L) return
    Vulkan.vkWaitForFences(device, longArrayOf(fence), true, Long.MAX_VALUE)
}

internal data class PreparedDrawCall(
    val drawCall: DrawCall,
    val pipeline: RenderPipeline,
    val material: Material,
    val frameIndex: Int,
    val uniformSlotIndex: Int
)

/** Resolves each [drawCalls] entry against [Renderer.pipelinesByFormat] by its
 * [DrawCall.mesh]'s own [io.github.ronjunevaldoz.awake.render.mesh.Mesh.format] and writes
 * its uniform buffer -- one shared pass for every vertex format a [Renderer] can draw,
 * replacing what used to be a separate `prepare*DrawCalls` function (and a separate
 * `Prepared*DrawCall` type) per format. A [drawCall] whose format has no entry in
 * [Renderer.pipelinesByFormat] is SKIPPED, not drawn through [Renderer.renderPipeline] as a
 * fallback -- rendering one format's vertex data through a different format's pipeline would
 * silently misinterpret the vertex buffer (wrong attribute count/offsets), which is worse
 * than not drawing it.
 *
 * [light] only reaches [drawCall]s resolved to [Renderer.renderPipeline] itself (the primary/
 * lit format) -- every other resolved pipeline gets [DrawCall.extraUniformFloats] instead
 * (e.g. a skinned mesh's joint palette), exactly matching what each format's own shader
 * expects (`triangle.wgsl` reads light; `textured.wgsl`/`skinned.wgsl` don't).
 *
 * [lightViewProjection] is `null` for every [Renderer] not built with shadow support (see
 * [Renderer.shadowMap]'s doc comment) -- in that case the written uniform buffer is byte-for-
 * byte identical to before shadows existed (mvp + 8 light floats). Non-null only appends 16
 * more floats (`drawCall.model * lightViewProjection`, same "Kotlin order gives the
 * conventional product" convention as `mvp` itself) for [Renderer.renderPipeline]-resolved
 * calls, matching `lit_shadow.wgsl`'s `Uniforms.lightMvp`. */
internal fun Renderer.prepareDrawCalls(
    frameIndex: Int,
    viewProjection: Mat4,
    drawCalls: List<DrawCall>,
    light: SceneLight,
    lightViewProjection: Mat4? = null,
    materialUsage: MutableMap<RenderMaterial, Int> = mutableMapOf()
): List<PreparedDrawCall> {
    val prepared = ArrayList<PreparedDrawCall>(drawCalls.size)
    // `vec4f` (not `vec3f`) for both fields on the WGSL side specifically to sidestep vec3's
    // implicit 16-byte alignment padding in a uniform-buffer struct -- see triangle.wgsl's own
    // Uniforms struct doc comment. The unused 4th float of each is simply never read there.
    val lightFloats = floatArrayOf(
        light.direction.x, light.direction.y, light.direction.z, 0f,
        light.color.x, light.color.y, light.color.z, 0f
    )
    var drawIndex = 0
    while (drawIndex < drawCalls.size) {
        val drawCall = drawCalls[drawIndex]
        val pipeline = pipelinesByFormat[drawCall.mesh.format]
        if (pipeline != null) {
            val material = drawCall.material as Material
            val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
            // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
            // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
            // order) gives the conventional `projection * view * model`.
            val mvp = drawCall.model * viewProjection
            val extraFloats = if (pipeline === renderPipeline) {
                if (lightViewProjection != null) {
                    lightFloats + (drawCall.model * lightViewProjection).data
                } else {
                    lightFloats
                }
            } else {
                drawCall.extraUniformFloats
            }
            material.updateUniformBuffer(frameIndex, uniformSlotIndex, mvp.data + extraFloats)
            prepared += PreparedDrawCall(drawCall, pipeline, material, frameIndex, uniformSlotIndex)
        }
        drawIndex += 1
    }
    return prepared
}

private fun MutableMap<RenderMaterial, Int>.nextSlot(material: RenderMaterial): Int {
    val slot = this[material] ?: 0
    this[material] = slot + 1
    return slot
}

/** The directional light's own view-projection, built the same "view * projection" (Kotlin
 * operator order) way [Camera.viewProjectionMatrix] builds a real camera's -- an orthographic
 * projection instead of a perspective one (correct for a directional/parallel-rays light, and
 * simpler than fitting a perspective frustum to one). The box is a FIXED size centered on the
 * world origin, not derived from actual scene bounds or the active camera's frustum.
 * ponytail: fixed shadow-box centered at the origin, not scene/camera-fit; upgrade path is a
 * per-frame bounding-box (or camera-frustum) fit once a demo's content moves far from origin. */
private fun Renderer.lightViewProjection(light: SceneLight): Mat4 {
    val direction = light.direction.normalized()
    val eye = direction * SHADOW_LIGHT_DISTANCE
    val up = if (abs(direction.y) > 0.99f) Vec3(0f, 0f, 1f) else Vec3(0f, 1f, 0f)
    val view = Mat4.setLookAt(eye = eye, center = Vec3.ZERO, up = up)
    val projection = Mat4.orthographic(
        left = -SHADOW_ORTHO_HALF_SIZE,
        right = SHADOW_ORTHO_HALF_SIZE,
        bottom = -SHADOW_ORTHO_HALF_SIZE,
        top = SHADOW_ORTHO_HALF_SIZE,
        near = SHADOW_NEAR,
        far = SHADOW_FAR,
        clipSpace = clipSpace
    )
    if (clipSpace.flipY) projection.m11 *= -1f
    return view * projection
}

/** The shadow depth pre-pass -- renders every [drawCalls] entry resolved to
 * [Renderer.renderPipeline] (the primary/lit format; nothing else casts a shadow today) from
 * the light's own point of view into [Renderer.shadowMap], reusing [runOffscreenCommands]
 * (the same one-time-command path [Renderer.renderToTexture] already uses) rather than a
 * second command-buffer/fence scheme. A no-op whenever shadows aren't supported
 * ([Renderer.shadowMap] is `null`) or are runtime-disabled ([Renderer.shadowsEnabled]).
 *
 * Binds each [PreparedDrawCall.material]'s own descriptor set (already written with
 * `lightMvp` by [prepareDrawCalls]) against [Renderer.shadowRenderPipeline]'s layout instead
 * of building a second per-draw uniform scheme -- see that pipeline's own doc comment for why
 * the two layouts are binding-compatible. */
internal fun Renderer.performShadowPass(drawCalls: List<PreparedDrawCall>) {
    val map = shadowMap ?: return
    val pipeline = shadowRenderPipeline ?: return
    if (!shadowsEnabled) return
    runOffscreenCommands { commandBuffer ->
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = map.renderPass,
            framebuffer = map.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(map.size, map.size)),
            pClearValues = arrayOf(Renderer.clearDepthValue)
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        pipeline.bind(commandBuffer)
        val viewport = VkViewport(width = map.size.toFloat(), height = map.size.toFloat())
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(extent = VkExtent2D(map.size, map.size))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        var drawIndex = 0
        while (drawIndex < drawCalls.size) {
            val prepared = drawCalls[drawIndex]
            if (prepared.pipeline === renderPipeline) {
                prepared.drawCall.mesh.bind(commandBuffer)
                prepared.material.bind(commandBuffer, pipeline.pipelineLayout, prepared.frameIndex, prepared.uniformSlotIndex)
                prepared.drawCall.mesh.draw(commandBuffer)
            }
            drawIndex += 1
        }
        Vulkan.vkCmdEndRenderPass(commandBuffer)
    }
}

private const val SHADOW_LIGHT_DISTANCE = 15f
private const val SHADOW_ORTHO_HALF_SIZE = 12f
private const val SHADOW_NEAR = 0.1f
private const val SHADOW_FAR = 40f

/** [Renderer.renderToTexture]/[Renderer.readPixels]'s own one-time-command runner -- see
 * [Renderer.offscreenCommandBuffer]'s doc comment for why this doesn't use
 * `transferContext.runOneTimeCommands`. Allocates its command buffer/fence once, on
 * first use, then resets and reuses both every call after that. */
internal fun Renderer.runOffscreenCommands(block: (Long) -> Unit) {
    if (offscreenCommandBuffer == 0L) {
        offscreenCommandBuffer = Vulkan.vkAllocateCommandBuffers(
            device,
            VkCommandBufferAllocateInfo(
                commandPool = transferContext.commandPool.handle,
                level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
                commandBufferCount = 1
            )
        )
        offscreenFence = Vulkan.vkCreateFence(device, VkFenceCreateInfo())
    }
    Vulkan.vkResetCommandBuffer(offscreenCommandBuffer, 0)
    Vulkan.vkBeginCommandBuffer(
        offscreenCommandBuffer,
        VkCommandBufferBeginInfo(flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value)
    )
    block(offscreenCommandBuffer)
    Vulkan.vkEndCommandBuffer(offscreenCommandBuffer)

    Vulkan.vkResetFences(device, longArrayOf(offscreenFence))
    Vulkan.vkQueueSubmit(
        graphicsQueue,
        arrayOf(VkSubmitInfo(pCommandBuffers = arrayOf(offscreenCommandBuffer))),
        offscreenFence
    )
    Vulkan.vkWaitForFences(device, longArrayOf(offscreenFence), true, Long.MAX_VALUE)
}

/** Stages this frame's world-space debug lines (e.g. a frustum wireframe) -- rewrites
 * [Renderer.lineMesh]'s buffer but issues no GPU commands itself, same "stage now, consume on
 * next draw" pattern as `drawUi`. Call before [performDraw] each frame.
 *
 * Named `performDrawDebugLines`, not `drawDebugLines` -- see [performDraw]'s doc comment for
 * why. */
internal fun Renderer.performDrawDebugLines(lines: List<LineSegment>) {
    waitForCurrentFrameResourceSlot()
    require(lines.size <= Renderer.MAX_DEBUG_LINES) {
        "Debug line count (${lines.size}) exceeds Renderer's LineMesh capacity (${Renderer.MAX_DEBUG_LINES})."
    }
    val vertices = FloatArray(lines.size * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX)
    var lineIndex = 0
    while (lineIndex < lines.size) {
        val line = lines[lineIndex]
        val vertexBase = lineIndex * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX
        writeLineVertex(vertices, vertexBase, line.start.x, line.start.y, line.start.z, line.color)
        writeLineVertex(
            vertices,
            vertexBase + LineMesh.FLOATS_PER_VERTEX,
            line.end.x,
            line.end.y,
            line.end.z,
            line.color
        )
        lineIndex += 1
    }
    lineMesh.update(swapchainManager.currentFrame, vertices)
}
