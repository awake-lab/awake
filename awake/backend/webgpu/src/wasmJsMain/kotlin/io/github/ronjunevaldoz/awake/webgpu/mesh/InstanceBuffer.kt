// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuBufferHandle
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage

/**
 * The per-instance model matrices behind one instanced draw call -- bound as vertex buffer slot
 * 1, alongside the mesh's own slot 0 (see `RenderPipeline`'s `instanced` parameter, which
 * declares the matching `GPUVertexStepMode.Instance` layout). Mirrors Vulkan's `InstanceBuffer`;
 * thinner for the same reason this backend's `ui.DynamicMesh` is thinner than Vulkan's --
 * `queue.writeBuffer` is already safe to call every frame with no mapping ceremony.
 *
 * Fixed capacity, rewritten wholesale every frame -- unlike this module's `Mesh`, which sizes
 * itself to the exact array it was constructed with and never updates.
 */
class InstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    /** Hard ceiling on instances per draw call -- [update] fails loudly (rather than silently
     * truncating) with a message naming this knob. */
    private val maxInstances: Int = DEFAULT_MAX_INSTANCES,
) {
    private val buffer: GPUBuffer = graphicsDevice.wgpuContext.device.createBuffer(
        BufferDescriptor(
            size = (maxInstances * FLOATS_PER_INSTANCE * Float.SIZE_BYTES).toULong(),
            usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst,
        ),
    )

    // Reused across frames so a steady instance count allocates nothing per frame.
    private var packed: FloatArray = FloatArray(0)

    /** Packs [models] into this buffer. `Mat4.data` is already column-major, which is the order
     * `instanced.wgsl`'s 4 `vec4` attributes reassemble into a `mat4x4` -- a straight copy, no
     * transpose. */
    fun update(models: List<Mat4>) {
        require(models.size <= maxInstances) {
            "Instance count (${models.size}) exceeds InstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (models.isEmpty()) return
        if (packed.size != models.size * FLOATS_PER_INSTANCE) {
            packed = FloatArray(models.size * FLOATS_PER_INSTANCE)
        }
        var index = 0
        while (index < models.size) {
            models[index].data.copyInto(packed, index * FLOATS_PER_INSTANCE)
            index += 1
        }
        graphicsDevice.wgpuContext.device.queue.writeBuffer(buffer, 0uL, fastArrayBufferOf(packed))
    }

    fun bufferRef(): GPUBuffer = buffer

    /** This buffer as the shared render layer's opaque handle -- built once, not per draw. */
    val binding: WebGpuBufferHandle by lazy { WebGpuBufferHandle(buffer) }

    fun destroy() {
        buffer.close()
    }

    companion object {
        /** One `mat4` per instance. */
        const val FLOATS_PER_INSTANCE = 16

        /** Same 4096 (256 KB) default as Vulkan's `InstanceBuffer` -- see its doc comment. */
        const val DEFAULT_MAX_INSTANCES = 4096
    }
}
