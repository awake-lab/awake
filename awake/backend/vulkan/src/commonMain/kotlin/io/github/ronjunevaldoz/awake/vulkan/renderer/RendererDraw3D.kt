// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
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
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException

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
    val imageFence = swapchainManager.imagesInFlight[imageIndex]
    if (imageFence != 0L) {
        Vulkan.vkWaitForFences(device, longArrayOf(imageFence), true, Long.MAX_VALUE)
    }
    swapchainManager.imagesInFlight[imageIndex] = swapchainManager.inFlightFences[currentFrame]
    Vulkan.vkResetFences(device, longArrayOf(swapchainManager.inFlightFences[currentFrame]))

    val aspect = swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat()
    val viewProjection = camera.viewProjectionMatrix(aspect)
    // Debug lines are already in world space (no per-line model matrix), so their MVP
    // is exactly this frame's viewProjection.
    lineRenderPipeline.writeMvp(currentFrame, viewProjection.data)
    val materialUsage = mutableMapOf<RenderMaterial, Int>()
    val preparedDrawCalls = prepareDrawCalls(currentFrame, viewProjection, drawCalls, materialUsage)
    val preparedSkinnedDrawCalls = prepareSkinnedDrawCalls(currentFrame, viewProjection, materialUsage)
    val preparedTexturedDrawCalls = prepareTexturedDrawCalls(currentFrame, viewProjection, materialUsage)

    Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
    recordCommandBuffer(
        commandBuffers[currentFrame],
        currentFrame,
        imageIndex,
        preparedDrawCalls,
        preparedSkinnedDrawCalls,
        preparedTexturedDrawCalls
    )

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

/** Binds+draws each [drawCalls] entry against whatever render pass/pipeline is already
 * bound on [commandBuffer] -- shared by [recordCommandBuffer] (the swapchain frame) and
 * [Renderer.renderToTexture] (an offscreen frame), so the two don't duplicate this loop. */
internal fun Renderer.recordDrawCalls(commandBuffer: Long, drawCalls: List<PreparedDrawCall>) {
    var drawIndex = 0
    val drawCount = drawCalls.size
    while (drawIndex < drawCount) {
        val prepared = drawCalls[drawIndex]
        prepared.drawCall.mesh.bind(commandBuffer)
        prepared.material.bind(
            commandBuffer,
            renderPipeline.pipelineLayout,
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
    drawCalls: List<PreparedDrawCall>,
    skinnedDrawCalls: List<PreparedSkinnedDrawCall>,
    texturedDrawCalls: List<PreparedTexturedDrawCall>
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
    lineRenderPipeline.bind(commandBuffer, frameIndex)
    lineMesh.bind(frameIndex, commandBuffer)
    lineMesh.draw(frameIndex, commandBuffer)

    val skinnedPipeline = skinnedRenderPipeline
    if (skinnedPipeline != null) {
        skinnedPipeline.bind(commandBuffer)
        var skinnedDrawIndex = 0
        while (skinnedDrawIndex < skinnedDrawCalls.size) {
            val prepared = skinnedDrawCalls[skinnedDrawIndex]
            prepared.drawCall.mesh.bind(commandBuffer)
            prepared.material.bind(
                commandBuffer,
                skinnedPipeline.pipelineLayout,
                prepared.frameIndex,
                prepared.uniformSlotIndex
            )
            prepared.drawCall.mesh.draw(commandBuffer)
            skinnedDrawIndex += 1
        }
    }
    pendingSkinnedDraws.clear()

    val texturedPipeline = texturedRenderPipeline
    if (texturedPipeline != null) {
        texturedPipeline.bind(commandBuffer)
        var texturedDrawIndex = 0
        while (texturedDrawIndex < texturedDrawCalls.size) {
            val prepared = texturedDrawCalls[texturedDrawIndex]
            prepared.drawCall.mesh.bind(commandBuffer)
            prepared.material.bind(
                commandBuffer,
                texturedPipeline.pipelineLayout,
                prepared.frameIndex,
                prepared.uniformSlotIndex
            )
            prepared.drawCall.mesh.draw(commandBuffer)
            texturedDrawIndex += 1
        }
    }
    pendingTexturedDraws.clear()

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
    val material: Material,
    val frameIndex: Int,
    val uniformSlotIndex: Int
)

internal data class PreparedSkinnedDrawCall(
    val drawCall: Renderer.SkinnedDrawCall,
    val material: Material,
    val frameIndex: Int,
    val uniformSlotIndex: Int
)

internal fun Renderer.prepareDrawCalls(
    frameIndex: Int,
    viewProjection: Mat4,
    drawCalls: List<DrawCall>,
    materialUsage: MutableMap<RenderMaterial, Int> = mutableMapOf()
): List<PreparedDrawCall> {
    val prepared = ArrayList<PreparedDrawCall>(drawCalls.size)
    var drawIndex = 0
    while (drawIndex < drawCalls.size) {
        val drawCall = drawCalls[drawIndex]
        val material = drawCall.material as Material
        val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
        // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
        // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
        // order) gives the conventional `projection * view * model`.
        val mvp = drawCall.model * viewProjection
        material.updateUniformBuffer(frameIndex, uniformSlotIndex, mvp.data)
        prepared += PreparedDrawCall(drawCall, material, frameIndex, uniformSlotIndex)
        drawIndex += 1
    }
    return prepared
}

internal fun Renderer.prepareSkinnedDrawCalls(
    frameIndex: Int,
    viewProjection: Mat4,
    materialUsage: MutableMap<RenderMaterial, Int> = mutableMapOf()
): List<PreparedSkinnedDrawCall> {
    val prepared = ArrayList<PreparedSkinnedDrawCall>(pendingSkinnedDraws.size)
    var drawIndex = 0
    while (drawIndex < pendingSkinnedDraws.size) {
        val drawCall = pendingSkinnedDraws[drawIndex]
        val material = drawCall.material as Material
        val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
        val mvp = drawCall.model * viewProjection
        material.updateUniformBuffer(frameIndex, uniformSlotIndex, mvp.data + drawCall.jointPalette)
        prepared += PreparedSkinnedDrawCall(drawCall, material, frameIndex, uniformSlotIndex)
        drawIndex += 1
    }
    return prepared
}

internal data class PreparedTexturedDrawCall(
    val drawCall: Renderer.TexturedDrawCall,
    val material: Material,
    val frameIndex: Int,
    val uniformSlotIndex: Int
)

internal fun Renderer.prepareTexturedDrawCalls(
    frameIndex: Int,
    viewProjection: Mat4,
    materialUsage: MutableMap<RenderMaterial, Int> = mutableMapOf()
): List<PreparedTexturedDrawCall> {
    val prepared = ArrayList<PreparedTexturedDrawCall>(pendingTexturedDraws.size)
    var drawIndex = 0
    while (drawIndex < pendingTexturedDraws.size) {
        val drawCall = pendingTexturedDraws[drawIndex]
        val material = drawCall.material as Material
        val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
        val mvp = drawCall.model * viewProjection
        material.updateUniformBuffer(frameIndex, uniformSlotIndex, mvp.data)
        prepared += PreparedTexturedDrawCall(drawCall, material, frameIndex, uniformSlotIndex)
        drawIndex += 1
    }
    return prepared
}

private fun MutableMap<RenderMaterial, Int>.nextSlot(material: RenderMaterial): Int {
    val slot = this[material] ?: 0
    this[material] = slot + 1
    return slot
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
