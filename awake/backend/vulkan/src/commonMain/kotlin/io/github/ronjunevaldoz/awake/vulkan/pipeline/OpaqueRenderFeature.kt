// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline

/**
 * The scene pass's geometry: the primary pipeline's draw calls first, then debug lines, then
 * every other resolved pipeline's group. Body moved verbatim from `recordCommandBuffer` --
 * grouping/ordering semantics are unchanged, they are just reached through
 * [RenderFrameContext] now instead of off `Renderer`'s fields.
 *
 * A capability, not authored content (docs/reference/render-extensibility.md): always present,
 * draws nothing of its own when a frame has no draw calls and no staged lines.
 */
internal class OpaqueRenderFeature(
    private val lineRenderPipeline: LineRenderPipeline,
) : RenderFeature {
    override val pass = RenderPassSlot.Scene

    override fun recordCommands(context: RenderFrameContext) = with(context) {
        primaryPipeline.bind(commandBuffer)
        recordDrawCalls(groupedDrawCalls[primaryPipeline] ?: emptyList())

        // Debug lines (e.g. a frustum wireframe), same render pass/depth attachment as the 3D
        // draw calls above -- real depth-testing against scene geometry, not an X-ray overlay.
        // Lines are already in world space (no per-line model matrix), so their MVP is exactly
        // this frame's viewProjection.
        lineRenderPipeline.writeMvp(frameIndex, viewProjection.data)
        lineRenderPipeline.bind(commandBuffer, frameIndex)
        lineMesh.bind(frameIndex, commandBuffer)
        lineMesh.draw(frameIndex, commandBuffer)

        groupedDrawCalls.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline) return@forEach
            pipeline.bind(commandBuffer)
            recordDrawCalls(group)
        }
    }

    /** The pipeline is this feature's to own now (it was constructor-injected into `Renderer`
     * and destroyed by `VulkanGameApplication` before this refactor). `lineMesh` is NOT
     * destroyed here -- it stays a `Renderer`-owned per-frame staging buffer. */
    override fun destroy() {
        lineRenderPipeline.destroy()
    }
}
