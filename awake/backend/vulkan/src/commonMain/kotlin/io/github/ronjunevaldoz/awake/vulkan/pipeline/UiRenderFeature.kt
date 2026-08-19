// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.ui.UiTextureRenderPipeline

/**
 * The UI overlay pass's content. Walks this frame's runs in original paint order, switching
 * pipeline at each run boundary -- lets e.g. a dropdown's overlay quad draw after a sibling
 * button's own label glyph, instead of a fixed "all quads, then all glyphs" order. Deliberately
 * NOT grouped/batched by pipeline, unlike the scene pass.
 *
 * Owns nothing: the UI pipelines are built lazily by `drawUi` and rebuilt on resize, so they
 * stay `Renderer`-owned and are read fresh through [RenderFrameContext.uiPipelines] each call.
 */
internal class UiRenderFeature : RenderFeature {
    override val pass = RenderPassSlot.Ui

    override fun recordCommands(context: RenderFrameContext): Unit = with(context) {
        val pipelines = uiPipelines() ?: return
        var runIndex = 0
        var textureSlotIndex = 0
        while (runIndex < uiRuns.size) {
            when (val run = uiRuns[runIndex]) {
                is Renderer.UiRun.QuadRun -> {
                    pipelines.quad.bind(commandBuffer)
                    run.mesh.bind(frameIndex, commandBuffer)
                    run.mesh.draw(frameIndex, commandBuffer)
                }

                is Renderer.UiRun.RoundedQuadRun -> {
                    pipelines.roundedQuad?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }

                is Renderer.UiRun.GlyphRun -> {
                    pipelines.glyph?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }

                is Renderer.UiRun.ClipRun -> {
                    // Clamped to the surface's own extent: nested scroll/clip regions can
                    // accumulate a few px of floating-point rounding past the frame edge.
                    // Vulkan tolerates an out-of-bounds scissor rect silently on most drivers,
                    // but it's equally out-of-spec here -- clamp defensively rather than rely
                    // on driver leniency (see WebGPU's Renderer.kt equivalent, which hits a
                    // hard validation error for the exact same unclamped rect).
                    val x = run.rect.x.toInt().coerceIn(0, surfaceWidth)
                    val y = run.rect.y.toInt().coerceIn(0, surfaceHeight)
                    val width = run.rect.width.toInt().coerceAtLeast(0)
                        .coerceAtMost(surfaceWidth - x)
                    val height = run.rect.height.toInt().coerceAtLeast(0)
                        .coerceAtMost(surfaceHeight - y)
                    Vulkan.vkCmdSetScissor(
                        commandBuffer,
                        0,
                        arrayOf(
                            VkRect2D(
                                offset = VkOffset2D(x, y),
                                extent = VkExtent2D(width, height),
                            ),
                        ),
                    )
                }

                is Renderer.UiRun.TextureRun ->
                    textureSlotIndex = recordTextureRun(
                        context,
                        pipelines.texture,
                        run,
                        textureSlotIndex,
                    )
            }
            runIndex += 1
        }
    }

    /** Render-target-backed textured quads (e.g. a minimap), one draw call per primitive. Each
     * primitive gets a distinct per-frame mesh and descriptor slot so command recording never
     * overwrites geometry or image bindings already referenced by an earlier texture draw --
     * hence the running slot index in and out. */
    private fun recordTextureRun(
        context: RenderFrameContext,
        texturePipeline: UiTextureRenderPipeline?,
        run: Renderer.UiRun.TextureRun,
        firstSlotIndex: Int,
    ): Int = with(context) {
        if (texturePipeline == null) return firstSlotIndex
        var slotIndex = firstSlotIndex
        var textureIndex = 0
        while (textureIndex < run.primitives.size) {
            val primitive = run.primitives[textureIndex]
            val material = primitive.material as Material
            val mesh = textureMeshForPrimitive(slotIndex)
            texturePipeline.bindMaterial(
                commandBuffer,
                frameIndex,
                slotIndex,
                material.samplerHandle,
                material.imageViewHandle,
            )
            mesh.update(frameIndex, primitive.vertices, primitive.indices)
            mesh.bind(frameIndex, commandBuffer)
            mesh.draw(frameIndex, commandBuffer)
            textureIndex += 1
            slotIndex += 1
        }
        return slotIndex
    }

    override fun destroy() = Unit
}
