// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.render.passes.ui.UiBatchCoalescer
import io.github.ronjunevaldoz.awake.render.passes.ui.UiStagedRun
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.ClipRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.GlyphRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.QuadRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.RoundedQuadRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.TextureRun
import io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget

/**
 * UI overlay primitive staging and headless rendering for the Vulkan backend.
 */

/**
 * Stages this frame's UI overlay content by coalescing [primitives] and uploading to dynamic meshes.
 */
internal fun Renderer.performDrawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
    waitForCurrentFrameResourceSlot()
    if (swapchainManager.imageViews.isNotEmpty()) {
        ensureUiQuadPipeline()
        if (font != null) ensureGlyphPipeline(font)
        if (primitives.any { it is UiDrawPrimitive.Texture }) ensureTextureQuadPipeline()
        if (primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.ShadowQuad }) ensureRoundedQuadPipeline()
    }

    val stagedRuns = UiBatchCoalescer.coalesce(primitives, Renderer.MAX_UI_QUADS)
    val runs = mutableListOf<Renderer.UiRun>()
    var quadRunCount = 0
    var roundedQuadRunCount = 0
    var glyphRunCount = 0

    for (staged in stagedRuns) {
        when (staged) {
            is UiStagedRun.QuadRun -> {
                val mesh = quadMeshForRun(quadRunCount++)
                mesh.update(swapchainManager.currentFrame, staged.vertices, staged.indices)
                runs += QuadRun(mesh)
            }
            is UiStagedRun.RoundedQuadRun -> {
                val mesh = roundedQuadMeshForRun(roundedQuadRunCount++)
                mesh.update(swapchainManager.currentFrame, staged.vertices, staged.indices)
                runs += RoundedQuadRun(mesh)
            }
            is UiStagedRun.GlyphRun -> {
                val mesh = glyphMeshForRun(glyphRunCount++)
                mesh.update(swapchainManager.currentFrame, staged.vertices, staged.indices)
                runs += GlyphRun(mesh)
            }
            is UiStagedRun.TextureRun -> {
                runs += TextureRun(staged.primitives.map {
                    Renderer.TexturedPrimitiveRun(it.texture, it.vertices, it.indices)
                })
            }
            is UiStagedRun.ClipRun -> {
                runs += ClipRun(staged.rect)
            }
        }
    }
    uiRuns = runs
}

/**
 * Headless/offscreen test hook: renders a full frame's worth of [UiDrawPrimitive]s into [target].
 */
fun Renderer.renderUiToTexture(target: RenderTarget, primitives: List<UiDrawPrimitive>, font: UiFont?) {
    val offscreen = target as OffscreenRenderTarget
    performDrawUi(primitives, font)
    val frameIndex = swapchainManager.currentFrame

    ensureOffscreenQuadPipeline()
    if (font != null) ensureOffscreenGlyphPipeline(font)
    if (primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.ShadowQuad }) ensureOffscreenRoundedQuadPipeline()

    val quadPipeline = requireNotNull(offscreenQuadRenderPipeline)
    quadPipeline.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())
    offscreenRoundedQuadRenderPipeline?.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())
    offscreenGlyphRenderPipeline?.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())

    runOffscreenCommands { commandBuffer ->
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
            framebuffer = offscreen.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height)),
            pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue),
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        val viewport = VkViewport(width = offscreen.width.toFloat(), height = offscreen.height.toFloat())
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val fullScissor = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(fullScissor))

        var runIndex = 0
        while (runIndex < uiRuns.size) {
            when (val run = uiRuns[runIndex]) {
                is Renderer.UiRun.QuadRun -> {
                    quadPipeline.bind(commandBuffer)
                    run.mesh.bind(frameIndex, commandBuffer)
                    run.mesh.draw(frameIndex, commandBuffer)
                }
                is Renderer.UiRun.RoundedQuadRun -> {
                    offscreenRoundedQuadRenderPipeline?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.GlyphRun -> {
                    offscreenGlyphRenderPipeline?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.ClipRun -> {
                    val maxX = offscreen.width
                    val maxY = offscreen.height
                    val x = run.rect.x.toInt().coerceIn(0, maxX)
                    val y = run.rect.y.toInt().coerceIn(0, maxY)
                    val width = run.rect.width.toInt().coerceAtLeast(0).coerceAtMost(maxX - x)
                    val height = run.rect.height.toInt().coerceAtLeast(0).coerceAtMost(maxY - y)
                    val scissor = VkRect2D(offset = VkOffset2D(x, y), extent = VkExtent2D(width, height))
                    Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
                }
                is Renderer.UiRun.TextureRun -> Unit
            }
            runIndex += 1
        }

        Vulkan.vkCmdEndRenderPass(commandBuffer)
        offscreen.transitionToShaderReadOnly(commandBuffer)
    }
}

/**
 * Headless/offscreen test hook: renders glyph primitives into [target] using the real Vulkan glyph pipeline.
 */
fun Renderer.renderUiGlyphsToTexture(target: RenderTarget, glyphs: List<UiDrawPrimitive.Glyph>, font: UiFont) {
    val offscreen = target as OffscreenRenderTarget
    waitForCurrentFrameResourceSlot()
    val frameIndex = swapchainManager.currentFrame
    ensureOffscreenGlyphPipeline(font)
    val glyphPipeline = requireNotNull(offscreenGlyphRenderPipeline)
    glyphPipeline.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())

    val glyphRunMeshes = buildList {
        var chunkStart = 0
        var glyphRunCount = 0
        while (chunkStart < glyphs.size) {
            val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, glyphs.size)
            val mesh = glyphMeshForRun(glyphRunCount)
            val run = UiBatchCoalescer.buildGlyphRun(glyphs.subList(chunkStart, chunkEnd))
            mesh.update(frameIndex, run.vertices, run.indices)
            add(mesh)
            glyphRunCount += 1
            chunkStart = chunkEnd
        }
    }

    runOffscreenCommands { commandBuffer ->
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
            framebuffer = offscreen.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height)),
            pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue),
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        val viewport = VkViewport(width = offscreen.width.toFloat(), height = offscreen.height.toFloat())
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        glyphPipeline.bind(commandBuffer)
        glyphRunMeshes.forEach { mesh ->
            mesh.bind(frameIndex, commandBuffer)
            mesh.draw(frameIndex, commandBuffer)
        }
        Vulkan.vkCmdEndRenderPass(commandBuffer)
        offscreen.transitionToShaderReadOnly(commandBuffer)
    }
}
