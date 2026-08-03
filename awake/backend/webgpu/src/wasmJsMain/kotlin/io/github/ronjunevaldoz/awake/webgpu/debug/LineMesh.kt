// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.debug

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage

/**
 * A world-space `LINE_LIST`-equivalent (`GPUPrimitiveTopology.LineList`) vertex buffer
 * rewritten every frame -- mirrors Vulkan's `debug.LineMesh` (see that class's doc comment),
 * but no index buffer, same reason: each consecutive vertex pair already is one line
 * segment. WebGPU's `queue.writeBuffer` is already safe per-frame with no staging needed
 * (see `ui.DynamicMesh`'s identical rationale for this backend).
 */
class LineMesh(
    private val graphicsDevice: GraphicsDevice,
    private val maxLines: Int
) {
    private val maxVertices = maxLines * VERTICES_PER_LINE
    private val vertexBuffer: GPUBuffer

    /** How many vertices this frame's [update] actually wrote -- [draw] only draws this
     * many, not the full buffer capacity. */
    var vertexCount: Int = 0
        private set

    init {
        val device = graphicsDevice.wgpuContext.device
        vertexBuffer = device.createBuffer(
            BufferDescriptor(
                size = (maxVertices * FLOATS_PER_VERTEX * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst
            )
        )
    }

    fun update(vertices: FloatArray) {
        require(vertices.size <= maxVertices * FLOATS_PER_VERTEX) {
            "Debug line count exceeds LineMesh capacity ($maxLines lines) -- " +
                "raise maxLines or draw fewer lines this frame."
        }
        val device = graphicsDevice.wgpuContext.device
        device.queue.writeBuffer(vertexBuffer, 0uL, fastArrayBufferOf(vertices))
        vertexCount = vertices.size / FLOATS_PER_VERTEX
    }

    fun vertexBufferRef(): GPUBuffer = vertexBuffer

    fun destroy() {
        vertexBuffer.close()
    }

    companion object {
        /** pos (vec3) + color (vec4) -- see `debug_line.wgsl`. */
        const val FLOATS_PER_VERTEX = 7
        const val VERTICES_PER_LINE = 2
    }
}
