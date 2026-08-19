// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.command.CommandRecorder
import io.github.ronjunevaldoz.awake.render.command.MaterialBinding
import io.github.ronjunevaldoz.awake.render.command.PipelineHandle
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanBuffers
import io.github.ronjunevaldoz.awake.vulkan.gen.VulkanDescriptors
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkIndexType
import io.github.ronjunevaldoz.awake.render.command.BufferHandle as RenderBufferHandle

/**
 * This backend's half of the [CommandRecorder] port: one `vkCmd*` call per method, no draw-order
 * logic (that lives once, in `SharedOpaqueRenderFeature`). The only file in this module allowed to
 * turn a shared render-feature call into a real Vulkan call.
 *
 * [commandBuffer] is a `var` rather than a constructor parameter because one frame records into
 * several buffers (the swapchain frame's, and the offscreen one `renderToTexture` reuses) -- one
 * recorder per `Renderer`, retargeted, beats allocating one per record site per frame. Safe for
 * the same reason `Renderer`'s other per-frame scratch state is: a `Renderer` records from one
 * thread.
 */
internal class VulkanCommandRecorder : CommandRecorder {
    var commandBuffer: Long = 0

    /** Set by [bindPipeline]. Vulkan needs the bound pipeline's layout to bind a descriptor set;
     * WebGPU's `setBindGroup` doesn't, which is why [CommandRecorder.bindMaterial] doesn't carry
     * one and this is tracked here instead. */
    private var boundPipelineLayout: Long = 0

    override fun bindPipeline(pipeline: PipelineHandle) {
        val vulkanPipeline = pipeline as VulkanPipelineHandle
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            vulkanPipeline.pipelineHandle,
        )
        boundPipelineLayout = vulkanPipeline.pipelineLayoutHandle
    }

    override fun bindMaterial(set: Int, binding: MaterialBinding) {
        VulkanDescriptors.vkCmdBindDescriptorSet(
            commandBuffer,
            boundPipelineLayout,
            set,
            (binding as VulkanMaterialBinding).descriptorSetHandle,
        )
    }

    override fun bindVertexBuffer(binding: Int, buffer: RenderBufferHandle) {
        VulkanBuffers.vkCmdBindVertexBuffers(
            commandBuffer,
            binding,
            (buffer as VulkanBufferBinding).asArray,
            ZERO_OFFSET,
        )
    }

    override fun bindIndexBuffer(buffer: RenderBufferHandle) {
        VulkanBuffers.vkCmdBindIndexBuffer(
            commandBuffer,
            (buffer as VulkanBufferBinding).handle,
            0,
            VkIndexType.VK_INDEX_TYPE_UINT32,
        )
    }

    override fun draw(vertexCount: Int, instanceCount: Int) {
        Vulkan.vkCmdDraw(commandBuffer, vertexCount, instanceCount, 0, 0)
    }

    override fun drawIndexed(indexCount: Int, instanceCount: Int) {
        VulkanBuffers.vkCmdDrawIndexed(commandBuffer, indexCount, instanceCount, 0, 0, 0)
    }

    private companion object {
        /** Every vertex buffer in this backend is bound at offset 0; shared so the bind path
         * allocates nothing. */
        val ZERO_OFFSET = longArrayOf(0L)
    }
}

/** A pipeline the shared layer can bind. Implemented by every real pipeline class here, which is
 * why the recorder above can cast unconditionally. */
interface VulkanPipelineHandle : PipelineHandle {
    val pipelineHandle: Long
    val pipelineLayoutHandle: Long
}

/** A descriptor set the shared layer can bind at some set index. Implemented by the already-
 * allocated per-frame/per-draw slot objects (`Material`'s uniform slots, `PerFrameUniformSlots
 * .Slot`, `SkinnedInstanceBuffer`'s frame resources), so resolving one per draw allocates
 * nothing. */
interface VulkanMaterialBinding : MaterialBinding {
    val descriptorSetHandle: Long
}

/**
 * A `VkBuffer` the shared layer can bind. Built once per buffer at resource-creation time, not
 * per draw -- [asArray] is the single-element array `vkCmdBindVertexBuffers` takes, kept here so
 * the per-frame bind path allocates nothing (the hand-written `Mesh.bind` it replaces built a
 * fresh one every call).
 */
class VulkanBufferBinding(val handle: Long) : RenderBufferHandle {
    val asArray = longArrayOf(handle)
}
