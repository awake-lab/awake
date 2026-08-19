// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.core.math.Vec4
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.pipeline.WebGpuBufferHandle
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage

/**
 * The per-instance RGBA color+alpha behind one billboard-particle instanced draw call --
 * vertex buffer slot 2, alongside the mesh's own slot 0 and [InstanceBuffer]'s model matrices
 * at slot 1 (see `RenderPipeline`'s `instanceAlpha` parameter). Mirrors [InstanceBuffer]'s
 * shape with `FLOATS_PER_INSTANCE = 4` (one `vec4f` per instance, not a generalization of
 * [InstanceBuffer]'s own `mat4` stride), its own slot -- not a generalization of [InstanceBuffer]
 * itself, since widening its stride would ripple into every other instanced format.
 */
class AlphaInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    private val maxInstances: Int = InstanceBuffer.DEFAULT_MAX_INSTANCES,
) {
    private val buffer: GPUBuffer = graphicsDevice.wgpuContext.device.createBuffer(
        BufferDescriptor(
            size = (maxInstances * FLOATS_PER_INSTANCE * Float.SIZE_BYTES).toULong(),
            usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst,
        ),
    )

    private var packed: FloatArray = FloatArray(0)

    fun update(colors: List<Vec4>) {
        require(colors.size <= maxInstances) {
            "Instance count (${colors.size}) exceeds AlphaInstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (colors.isEmpty()) return
        if (packed.size != colors.size * FLOATS_PER_INSTANCE) {
            packed = FloatArray(colors.size * FLOATS_PER_INSTANCE)
        }
        var index = 0
        while (index < colors.size) {
            val color = colors[index]
            val offset = index * FLOATS_PER_INSTANCE
            packed[offset] = color.x
            packed[offset + 1] = color.y
            packed[offset + 2] = color.z
            packed[offset + 3] = color.w
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

    private companion object {
        const val FLOATS_PER_INSTANCE = 4
    }
}
