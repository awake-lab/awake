// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.BindGroupDescriptor
import io.ygdrasil.webgpu.BindGroupEntry
import io.ygdrasil.webgpu.BufferBinding
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPURenderPipeline

/**
 * The per-instance JOINT PALETTES behind one animated instanced draw call -- [InstanceBuffer]'s
 * animated companion, mirroring Vulkan's `SkinnedInstanceBuffer` (see its doc comment for why
 * this is a storage buffer rather than a vertex attribute or a uniform array). Read by
 * `skinned_instanced.wgsl` as `array<JointPalette>` indexed by `@builtin(instance_index)`.
 *
 * Bound as bind group 1 (`@group(1)`), not group 0: group 0 holds the shared
 * `viewProjection` + light uniform, which every animated instanced draw call can share, while
 * this buffer is per draw call.
 */
class SkinnedInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    /** Hard ceiling on animated instances per draw call. Each one costs [FLOATS_PER_INSTANCE]
     * floats = 4 KB, 16x what a static instance costs in [InstanceBuffer] -- raising this is a
     * real memory decision. [update] fails loudly naming it rather than silently truncating. */
    private val maxInstances: Int = DEFAULT_MAX_INSTANCES,
) {
    private val buffer: GPUBuffer = graphicsDevice.wgpuContext.device.createBuffer(
        BufferDescriptor(
            size = (maxInstances.toLong() * FLOATS_PER_INSTANCE * Float.SIZE_BYTES).toULong(),
            usage = GPUBufferUsage.Storage or GPUBufferUsage.CopyDst,
        ),
    )

    // Cached per pipeline object for the same reason Renderer's uniform bind groups are: an
    // "auto"-layout bind group is only valid against the exact pipeline layout it came from.
    private var bindGroup: GPUBindGroup? = null
    private var bindGroupPipeline: GPURenderPipeline? = null

    // Reused across frames so a steady instance count allocates nothing per frame.
    private var packed: FloatArray = FloatArray(0)

    /** Packs [palettes] at a fixed [FLOATS_PER_INSTANCE] stride -- see Vulkan's
     * `SkinnedInstanceBuffer.update` for why a shorter palette is fine. */
    fun update(palettes: List<FloatArray>) {
        require(palettes.size <= maxInstances) {
            "Animated instance count (${palettes.size}) exceeds SkinnedInstanceBuffer capacity " +
                "($maxInstances) -- raise maxInstances or draw fewer instances."
        }
        if (palettes.isEmpty()) return
        if (packed.size != palettes.size * FLOATS_PER_INSTANCE) {
            packed = FloatArray(palettes.size * FLOATS_PER_INSTANCE)
        }
        var index = 0
        while (index < palettes.size) {
            val palette = palettes[index]
            require(palette.size <= FLOATS_PER_INSTANCE) {
                "Joint palette ${palette.size} floats exceeds MAX_JOINTS ($MAX_JOINTS) * 16."
            }
            palette.copyInto(packed, index * FLOATS_PER_INSTANCE)
            index += 1
        }
        graphicsDevice.wgpuContext.device.queue.writeBuffer(buffer, 0uL, fastArrayBufferOf(packed))
    }

    /** This buffer as [pipeline]'s group-1 bind group, built once per pipeline object. */
    fun bindGroupFor(pipeline: GPURenderPipeline): GPUBindGroup {
        val cached = bindGroup
        if (cached != null && bindGroupPipeline === pipeline) return cached
        val created = graphicsDevice.wgpuContext.device.createBindGroup(
            BindGroupDescriptor(
                layout = pipeline.getBindGroupLayout(PALETTE_GROUP),
                entries = listOf(
                    BindGroupEntry(binding = 0u, resource = BufferBinding(buffer = buffer)),
                ),
            ),
        )
        bindGroup = created
        bindGroupPipeline = pipeline
        return created
    }

    fun destroy() {
        buffer.close()
    }

    companion object {
        /** `skinned_instanced.wgsl`'s own MAX_JOINTS. */
        const val MAX_JOINTS = 64

        /** One fixed-size `JointPalette` struct: 64 `mat4` = 1024 floats = 4 KB per instance. */
        const val FLOATS_PER_INSTANCE = MAX_JOINTS * 16

        /** Same 256 (1 MB) default as Vulkan's `SkinnedInstanceBuffer` -- see its doc comment. */
        const val DEFAULT_MAX_INSTANCES = 256

        /** `@group(1)` -- see this class's own doc comment. */
        const val PALETTE_GROUP = 1u
    }
}
