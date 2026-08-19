// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.renderer.PreparedDrawCall
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.texture.ShadowMap

/**
 * The shadow depth pre-pass. NOT a [RenderFeature]: it begins and ends its own render pass
 * (the shadow map's, not the scene's) and runs on its own one-time command buffer before the
 * frame's command buffer is recorded at all, so it shares no signature with the shared-pass
 * features. Kept as a deliberate special case rather than generalized -- if a second
 * standalone-pass feature ever exists, that is the moment to look for a shared shape.
 *
 * Renders every [PreparedDrawCall] whose resolved pipeline shares the primary
 * [VertexFormat] (not just the literal primary pipeline object) from the light's point of view
 * into [shadowMap]. That also covers the instanced primary-format pipeline (same vertex layout,
 * just instanced) -- an instanced crowd casts a shadow like its non-instanced counterpart.
 * Still does NOT cover skinned-instanced or particle draws: [shadowRenderPipeline] is built
 * with ONE fixed vertex layout, and those use a genuinely different one it cannot correctly
 * bind -- extending coverage to them needs a shadow pipeline per format, not a wider check.
 *
 * Binds each [PreparedDrawCall.material]'s own descriptor set (already written with `lightMvp`
 * by `prepareDrawCalls`) against [shadowRenderPipeline]'s layout instead of a second per-draw
 * uniform scheme -- see that pipeline's own doc comment for why the two layouts are
 * binding-compatible.
 */
internal class ShadowFeature(
    /** Also read by `Renderer` itself: a material's descriptor set layout gains its extra
     * shadow bindings only when this exists, and `prepareDrawCalls` writes `lightMvp` only
     * then. */
    internal val shadowMap: ShadowMap,
    private val shadowRenderPipeline: ShadowRenderPipeline,
) {
    /** [commandBuffer] is the caller's already-begun one-time buffer (`Renderer` owns that
     * runner); [castFormat] is the one vertex format this pipeline can bind. */
    fun recordCommands(
        commandBuffer: Long,
        drawCalls: List<PreparedDrawCall>,
        castFormat: VertexFormat,
    ) {
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = shadowMap.renderPass,
            framebuffer = shadowMap.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(shadowMap.size, shadowMap.size)),
            pClearValues = arrayOf(Renderer.clearDepthValue),
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            renderPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE,
        )
        shadowRenderPipeline.bind(commandBuffer)
        val size = shadowMap.size.toFloat()
        Vulkan.vkCmdSetViewport(
            commandBuffer,
            0,
            arrayOf(VkViewport(width = size, height = size)),
        )
        Vulkan.vkCmdSetScissor(
            commandBuffer,
            0,
            arrayOf(VkRect2D(extent = VkExtent2D(shadowMap.size, shadowMap.size))),
        )
        var drawIndex = 0
        while (drawIndex < drawCalls.size) {
            val prepared = drawCalls[drawIndex]
            if (prepared.pipeline.vertexFormat == castFormat) {
                prepared.drawCall.mesh.bind(commandBuffer)
                prepared.material.bind(
                    commandBuffer,
                    shadowRenderPipeline.pipelineLayout,
                    prepared.frameIndex,
                    prepared.uniformSlotIndex,
                )
                prepared.drawCall.mesh.draw(commandBuffer)
            }
            drawIndex += 1
        }
        Vulkan.vkCmdEndRenderPass(commandBuffer)
    }

    fun destroy() {
        shadowRenderPipeline.destroy()
        shadowMap.destroy()
    }
}
