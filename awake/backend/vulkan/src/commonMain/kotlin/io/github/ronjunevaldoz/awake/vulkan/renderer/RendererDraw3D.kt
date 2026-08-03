// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
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
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException

/** The 3D frame path -- [Renderer.draw]'s whole-frame orchestration (wait/acquire -> update
 * uniforms -> record -> submit -> present), the shared per-draw-call recording loop, the
 * swapchain command-buffer recording (3D pass + UI overlay pass), the offscreen one-time-
 * command runner both [Renderer.renderToTexture]/[Renderer.readPixels] use, and debug-line
 * staging. See [Renderer]'s class doc comment for why this lives here as `internal` extension
 * functions rather than as members. */

/** Renders one frame: waits for this frame-in-flight slot, acquires a swapchain image,
 * writes each [DrawCall]'s MVP matrix (model combined with [camera]'s view/projection)
 * into its own material's uniform buffer, records and submits a command buffer that
 * draws every call in order, then presents. Fully serializes frames afterward (see the
 * `vkDeviceWaitIdle` call below) so each material's single (not per-frame-in-flight)
 * uniform buffer can be safely rewritten every frame -- a real engine would double-buffer
 * those per frame-in-flight instead of paying this full-pipeline stall; deferred as a
 * later Phase 2 concern, unchanged from before this extraction.
 *
 * Named `performDraw`, not `draw` -- [Renderer]'s `override fun draw(...)` (the actual
 * `RenderRenderer` interface method) is a one-line delegate to this extension function; an
 * extension function can't share its name with a member function it's called from without
 * the member call winning resolution and recursing into itself. */
internal fun Renderer.performDraw(camera: Camera, drawCalls: List<DrawCall>) {
    val currentFrame = swapchainManager.currentFrame
    Vulkan.vkWaitForFences(
        device,
        longArrayOf(swapchainManager.inFlightFences[currentFrame]),
        true,
        Long.MAX_VALUE
    )
    // Reset only AFTER a successful acquire (not before) -- if acquire throws and this
    // frame bails out early (see the catch below), an already-reset-but-never-submitted
    // fence would stay unsignaled forever, hanging the NEXT draw() call's
    // vkWaitForFences on this same frame-in-flight slot indefinitely.
    val imageIndex: Int
    try {
        imageIndex = Vulkan.vkAcquireNextImageKHR(
            device,
            swapchainManager.swapChain,
            Int.MAX_VALUE.toLong(),
            swapchainManager.imageAvailableSemaphores[currentFrame],
            0
        )
    } catch (e: VkResultException) {
        when (e.result) {
            // A resized/minimized/moved-to-another-display window makes the swapchain
            // stale before this frame's acquire even runs (not just after present, see
            // the catch around vkQueuePresentKHR below) -- recreate and skip this frame
            // entirely: there's no valid acquired image to record/submit/present against.
            VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> {
                recreateSwapChain()
                return
            }
            else -> throw e
        }
    }
    Vulkan.vkResetFences(device, longArrayOf(swapchainManager.inFlightFences[currentFrame]))

    val aspect = swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat()
    val viewProjection = camera.viewProjectionMatrix(aspect)
    // Debug lines are already in world space (no per-line model matrix), so their MVP
    // is exactly this frame's viewProjection.
    lineRenderPipeline.writeMvp(viewProjection.data)
    var drawIndex = 0
    val drawCount = drawCalls.size
    while (drawIndex < drawCount) {
        val drawCall = drawCalls[drawIndex]
        // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
        // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
        // order) gives the conventional `projection * view * model`.
        val mvp = drawCall.model * viewProjection
        drawCall.material.updateUniformBuffer(mvp.data)
        drawIndex += 1
    }

    Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
    recordCommandBuffer(commandBuffers[currentFrame], imageIndex, drawCalls)

    val waitSemaphores = arrayOf(swapchainManager.imageAvailableSemaphores[currentFrame])
    val waitStages =
        intArrayOf(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value)
    val signalSemaphores = arrayOf(swapchainManager.renderFinishedSemaphores[currentFrame])

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

    VulkanBuffers.vkDeviceWaitIdle(device)
}

/** Binds+draws each [drawCalls] entry against whatever render pass/pipeline is already
 * bound on [commandBuffer] -- shared by [recordCommandBuffer] (the swapchain frame) and
 * [Renderer.renderToTexture] (an offscreen frame), so the two don't duplicate this loop. */
internal fun Renderer.recordDrawCalls(commandBuffer: Long, drawCalls: List<DrawCall>) {
    var drawIndex = 0
    val drawCount = drawCalls.size
    while (drawIndex < drawCount) {
        val drawCall = drawCalls[drawIndex]
        drawCall.mesh.bind(commandBuffer)
        drawCall.material.bind(commandBuffer, renderPipeline.pipelineLayout)
        drawCall.mesh.draw(commandBuffer)
        drawIndex += 1
    }
}

internal fun Renderer.recordCommandBuffer(commandBuffer: Long, acquiredImageIndex: Int, drawCalls: List<DrawCall>) {
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

    recordDrawCalls(commandBuffer, drawCalls)

    // Debug lines (e.g. a frustum wireframe), same render pass/depth attachment as
    // the 3D draw calls above -- real depth-testing against scene geometry, not an
    // X-ray overlay. Still inside this pass, before it ends.
    lineRenderPipeline.bind(commandBuffer)
    lineMesh.bind(commandBuffer)
    lineMesh.draw(commandBuffer)

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
        val quadMesh = textureQuadMesh
        var runIndex = 0
        while (runIndex < uiRuns.size) {
            when (val run = uiRuns[runIndex]) {
                is Renderer.UiRun.QuadRun -> {
                    uiPipeline.bind(commandBuffer)
                    run.mesh.bind(commandBuffer)
                    run.mesh.draw(commandBuffer)
                }
                is Renderer.UiRun.RoundedQuadRun -> {
                    if (roundedQuadPipeline != null) {
                        roundedQuadPipeline.bind(commandBuffer)
                        run.mesh.bind(commandBuffer)
                        run.mesh.draw(commandBuffer)
                    }
                }
                is Renderer.UiRun.GlyphRun -> {
                    if (glyphPipeline != null) {
                        glyphPipeline.bind(commandBuffer)
                        run.mesh.bind(commandBuffer)
                        run.mesh.draw(commandBuffer)
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
                    // call per primitive -- each rewrites the texture pipeline's one
                    // shared descriptor set (see UiTextureRenderPipeline's doc comment
                    // for why that's safe here). Geometry is already staged, including
                    // any exact convex path clipping, so command recording stays simple.
                    if (texturePipeline != null && quadMesh != null) {
                        var textureIndex = 0
                        while (textureIndex < run.primitives.size) {
                            val primitive = run.primitives[textureIndex]
                            val material = primitive.material as Material
                            texturePipeline.bindMaterial(commandBuffer, material.samplerHandle, material.imageViewHandle)
                            quadMesh.update(primitive.vertices, primitive.indices)
                            quadMesh.bind(commandBuffer)
                            quadMesh.draw(commandBuffer)
                            textureIndex += 1
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
