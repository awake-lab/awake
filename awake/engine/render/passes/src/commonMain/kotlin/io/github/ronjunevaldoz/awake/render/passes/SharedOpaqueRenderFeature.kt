// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes

import io.github.ronjunevaldoz.awake.render.command.CommandRecorder
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw

/**
 * The scene pass's geometry, written once for every backend: the primary pipeline's draws first,
 * then debug lines, then every other resolved pipeline's group. Body is Vulkan's
 * `OpaqueRenderFeature`/`recordDrawCalls` pair, reached through [CommandRecorder] instead of
 * `VkCommandBuffer` calls -- WebGPU's own equivalent loop collapsed into the same code.
 *
 * Holds no state: both backends already own their pipelines/meshes elsewhere and hand this
 * everything per call. The pass it records into is begun and ended by the backend, never here.
 *
 * Never imports a Vulkan or WebGPU type, at any phase. A capability this can't express belongs on
 * [CommandRecorder] as a new method, not as a backend branch in here.
 */
class SharedOpaqueRenderFeature {

    /**
     * [grouped] is this frame's draws keyed by resolved pipeline (a stable `groupBy`, so paint
     * order within a group is the caller's original order). [primaryPipeline]'s group is recorded
     * first so it lands before debug lines; every other group follows.
     *
     * [lines] is the world-space debug-line draw, or null when the frame staged none. Recorded
     * between the primary group and the rest, matching where Vulkan already draws them. A line
     * draw with no vertices skips its pipeline bind too -- binding a pipeline nothing is then
     * drawn with has no effect on the output either way.
     */
    fun recordCommands(
        recorder: CommandRecorder,
        primaryPipeline: PipelineHandle,
        grouped: Map<out PipelineHandle, List<PreparedDraw>>,
        lines: PreparedDraw? = null,
    ) {
        recorder.bindPipeline(primaryPipeline)
        // Two passes over the map rather than one lookup + one filtered loop: `grouped` is
        // projected `out` on its key (each backend keys it by its own pipeline type), and an
        // out-projected Map has no usable `get`. The map holds one entry per vertex format.
        grouped.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline) recordDraws(recorder, group)
        }

        if (lines != null && lines.elementCount > 0) {
            recorder.bindPipeline(lines.pipeline)
            recordDraws(recorder, lines)
        }

        grouped.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline) return@forEach
            recorder.bindPipeline(pipeline)
            recordDraws(recorder, group)
        }
    }

    /** Every draw in [draws] against whatever pipeline is already bound -- the offscreen
     * `renderToTexture` path calls this directly, having bound each group's pipeline itself. */
    fun recordDraws(recorder: CommandRecorder, draws: List<PreparedDraw>) {
        var index = 0
        while (index < draws.size) {
            recordDraws(recorder, draws[index])
            index += 1
        }
    }

    private fun recordDraws(recorder: CommandRecorder, draw: PreparedDraw) {
        draw.vertexBuffer?.let { recorder.bindVertexBuffer(VERTEX_BINDING, it) }
        recorder.bindMaterial(MATERIAL_SET, draw.materialBinding)
        draw.instanceVertexBuffer?.let { recorder.bindVertexBuffer(INSTANCE_MODEL_BINDING, it) }
        draw.jointPaletteBinding?.let { recorder.bindMaterial(JOINT_PALETTE_SET, it) }
        draw.instanceColorBuffer?.let { recorder.bindVertexBuffer(INSTANCE_COLOR_BINDING, it) }
        draw.instanceFrameBuffer?.let { recorder.bindVertexBuffer(INSTANCE_FRAME_BINDING, it) }
        val indexBuffer = draw.indexBuffer
        if (indexBuffer == null) {
            recorder.draw(draw.elementCount, draw.instances)
        } else {
            recorder.bindIndexBuffer(indexBuffer)
            recorder.drawIndexed(draw.elementCount, draw.instances)
        }
    }

    private companion object {
        const val MATERIAL_SET = 0
        const val JOINT_PALETTE_SET = 1
        const val VERTEX_BINDING = 0
        const val INSTANCE_MODEL_BINDING = 1
        const val INSTANCE_COLOR_BINDING = 2
        const val INSTANCE_FRAME_BINDING = 3
    }
}
