// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage

/**
 * The per-instance alpha values behind one billboard-particle instanced draw call -- vertex
 * buffer slot 2, alongside the mesh's own slot 0 and [InstanceBuffer]'s model matrices at slot 1
 * (see `RenderPipeline`'s `instanceAlpha` parameter). Mirrors [InstanceBuffer]'s shape with
 * `FLOATS_PER_INSTANCE = 1`, its own slot -- not a generalization of [InstanceBuffer] itself,
 * since widening its stride would ripple into every other instanced format.
 */
class AlphaInstanceBuffer(
    private val graphicsDevice: GraphicsDevice,
    private val maxInstances: Int = InstanceBuffer.DEFAULT_MAX_INSTANCES,
) {
    private val buffer: GPUBuffer = graphicsDevice.wgpuContext.device.createBuffer(
        BufferDescriptor(
            size = (maxInstances * Float.SIZE_BYTES).toULong(),
            usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst,
        ),
    )

    private var packed: FloatArray = FloatArray(0)

    fun update(alphas: List<Float>) {
        require(alphas.size <= maxInstances) {
            "Instance count (${alphas.size}) exceeds AlphaInstanceBuffer capacity ($maxInstances) -- " +
                "raise maxInstances or draw fewer instances."
        }
        if (alphas.isEmpty()) return
        if (packed.size != alphas.size) {
            packed = FloatArray(alphas.size)
        }
        var index = 0
        while (index < alphas.size) {
            packed[index] = alphas[index]
            index += 1
        }
        graphicsDevice.wgpuContext.device.queue.writeBuffer(buffer, 0uL, fastArrayBufferOf(packed))
    }

    fun bufferRef(): GPUBuffer = buffer

    fun destroy() {
        buffer.close()
    }
}
