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
     * Records draw commands for all opaque pipeline groups and optional world-space debug lines.
     *
     * Groups are keyed by resolved pipeline ([grouped]). If the primary pipeline group has non-empty
     * draws, [primaryPipeline] is bound first, followed by [lines] (if present and non-empty), and
     * finally all remaining format groups.
     *
     * @param recorder The command recorder to record GPU commands into.
     * @param primaryPipeline The primary pipeline handle for default format geometry.
     * @param grouped The map of prepared draw lists keyed by pipeline handle.
     * @param lines The optional world-space debug line draw.
     */
    fun recordCommands(
        recorder: CommandRecorder,
        primaryPipeline: PipelineHandle,
        grouped: Map<out PipelineHandle, List<PreparedDraw>>,
        lines: PreparedDraw? = null,
    ) {
        var primaryBound = false
        grouped.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline && group.isNotEmpty()) {
                if (!primaryBound) {
                    recorder.bindPipeline(primaryPipeline)
                    primaryBound = true
                }
                recordDraws(recorder, group)
            }
        }

        if (lines != null && lines.elementCount > 0) {
            recorder.bindPipeline(lines.pipeline)
            recordDraws(recorder, lines)
        }

        grouped.forEach { (pipeline, group) ->
            if (pipeline === primaryPipeline || group.isEmpty()) return@forEach
            recorder.bindPipeline(pipeline)
            recordDraws(recorder, group)
        }
    }

    /**
     * Records an ordered list of prepared draws against whatever pipeline is currently bound.
     *
     * Redundant vertex buffer and index buffer bindings across consecutive draws are elided.
     *
     * @param recorder The command recorder to record GPU commands into.
     * @param draws The list of prepared draw commands to execute.
     */
    fun recordDraws(recorder: CommandRecorder, draws: List<PreparedDraw>) {
        var lastVertexBuffer: io.github.ronjunevaldoz.awake.render.command.BufferHandle? = null
        var lastIndexBuffer: io.github.ronjunevaldoz.awake.render.command.BufferHandle? = null
        var index = 0
        while (index < draws.size) {
            val draw = draws[index]
            val vertexBuffer = draw.vertexBuffer
            if (vertexBuffer != null && vertexBuffer !== lastVertexBuffer) {
                recorder.bindVertexBuffer(VERTEX_BINDING, vertexBuffer)
                lastVertexBuffer = vertexBuffer
            }
            recorder.bindMaterial(MATERIAL_SET, draw.materialBinding)
            draw.instanceVertexBuffer?.let { recorder.bindVertexBuffer(INSTANCE_MODEL_BINDING, it) }
            draw.jointPaletteBinding?.let { recorder.bindMaterial(JOINT_PALETTE_SET, it) }
            draw.instanceColorBuffer?.let { recorder.bindVertexBuffer(INSTANCE_COLOR_BINDING, it) }
            draw.instanceFrameBuffer?.let { recorder.bindVertexBuffer(INSTANCE_FRAME_BINDING, it) }
            val indexBuffer = draw.indexBuffer
            if (indexBuffer == null) {
                lastIndexBuffer = null
                recorder.draw(draw.elementCount, draw.instances)
            } else {
                if (indexBuffer !== lastIndexBuffer) {
                    recorder.bindIndexBuffer(indexBuffer)
                    lastIndexBuffer = indexBuffer
                }
                recorder.drawIndexed(draw.elementCount, draw.instances)
            }
            index += 1
        }
    }

    /**
     * Records a single prepared draw against whatever pipeline is currently bound.
     *
     * @param recorder The command recorder to record GPU commands into.
     * @param draw The single prepared draw command to execute.
     */
    fun recordDraws(recorder: CommandRecorder, draw: PreparedDraw) {
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
