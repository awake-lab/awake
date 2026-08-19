// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.command.BufferHandle
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import io.github.ronjunevaldoz.awake.render.passes.SharedOpaqueRenderFeature
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.debug.LineRenderPipeline

/**
 * The scene pass's geometry. The draw order itself (primary group, debug lines, then every other
 * resolved pipeline's group) lives once in [SharedOpaqueRenderFeature] and runs identically on
 * WebGPU; this class is what still has to be per-backend: owning the line pipeline, writing its
 * uniform, and describing the line draw in the port's terms.
 *
 * A capability, not authored content (docs/reference/render-extensibility.md): always present,
 * draws nothing of its own when a frame has no draw calls and no staged lines.
 */
internal class OpaqueRenderFeature(
    private val lineRenderPipeline: LineRenderPipeline,
) : RenderFeature {
    override val pass = RenderPassSlot.Scene

    private val shared = SharedOpaqueRenderFeature()

    override fun recordCommands(context: RenderFrameContext) = with(context) {
        // Debug lines (e.g. a frustum wireframe) share the scene pass's depth attachment --
        // real depth-testing against scene geometry, not an X-ray overlay. They are already in
        // world space (no per-line model matrix), so their MVP is exactly this viewProjection.
        lineRenderPipeline.writeMvp(frameIndex, viewProjection.data)
        shared.recordCommands(
            recorder = recorder,
            primaryPipeline = primaryPipeline,
            grouped = groupedDrawCalls,
            lines = LineDraw(lineRenderPipeline, lineMesh, frameIndex),
        )
    }

    /** The pipeline is this feature's to own now (it was constructor-injected into `Renderer`
     * and destroyed by `VulkanGameApplication` before this refactor). `lineMesh` is NOT
     * destroyed here -- it stays a `Renderer`-owned per-frame staging buffer. */
    override fun destroy() {
        lineRenderPipeline.destroy()
    }
}

/** This frame's staged debug lines as one non-indexed [PreparedDraw]. One small object per frame,
 * not per draw call -- every handle it exposes was created with its resource. */
private class LineDraw(
    private val linePipeline: LineRenderPipeline,
    private val lineMesh: LineMesh,
    private val frameIndex: Int,
) : PreparedDraw {
    override val pipeline: PipelineHandle get() = linePipeline
    override val materialBinding: MaterialBinding get() = linePipeline.uniformBinding(frameIndex)
    override val vertexBuffer: BufferHandle get() = lineMesh.binding(frameIndex)

    /** No index buffer at all: consecutive vertex pairs are the line segments under
     * `LINE_LIST`, so [elementCount] is a vertex count. */
    override val indexBuffer: BufferHandle? get() = null
    override val elementCount: Int get() = lineMesh.vertexCount(frameIndex)
}
