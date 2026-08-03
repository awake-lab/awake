// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.BufferHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPUIndexFormat

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): real wgpu4k implementation.
 * `device.queue.writeBuffer()` uploads directly -- no HOST_VISIBLE staging buffer + one-time
 * command buffer + `vkCmdCopyBuffer` dance the way Vulkan's `Mesh.kt` needs, a real
 * simplification, not an oversight. [vertexBufferMemory]/[indexBufferMemory] have no WebGPU
 * equivalent (buffer memory is managed internally, no separate allocation object to hold a
 * handle to) and just mirror their buffer's handle. [runOneTimeCommands] is unused for the
 * same reason.
 */
class Mesh(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    vertices: FloatArray,
    indices: IntArray
) : RenderMesh {
    var vertexBuffer: BufferHandle
    var vertexBufferMemory: DeviceMemoryHandle
    var indexBuffer: BufferHandle
    var indexBufferMemory: DeviceMemoryHandle
    val indexCount: Int = indices.size

    init {
        val device = graphicsDevice.wgpuContext.device

        val rawVertexBuffer: GPUBuffer = device.createBuffer(
            BufferDescriptor(
                size = (vertices.size * Float.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Vertex or GPUBufferUsage.CopyDst
            )
        )
        device.queue.writeBuffer(rawVertexBuffer, 0uL, fastArrayBufferOf(vertices))
        val vertexHandle = WebGpuHandles.register(rawVertexBuffer)
        vertexBuffer = BufferHandle(vertexHandle)
        vertexBufferMemory = DeviceMemoryHandle(vertexHandle)

        val rawIndexBuffer: GPUBuffer = device.createBuffer(
            BufferDescriptor(
                size = (indices.size * Int.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Index or GPUBufferUsage.CopyDst
            )
        )
        device.queue.writeBuffer(rawIndexBuffer, 0uL, fastArrayBufferOf(indices))
        val indexHandle = WebGpuHandles.register(rawIndexBuffer)
        indexBuffer = BufferHandle(indexHandle)
        indexBufferMemory = DeviceMemoryHandle(indexHandle)
    }

    override fun bind(commandBuffer: Long) {
        TODO("WebGPU binds vertex/index buffers directly in Renderer.draw(), see docs/MVP_PLAN.md")
    }

    override fun draw(commandBuffer: Long) {
        TODO("WebGPU issues drawIndexed directly in Renderer.draw(), see docs/MVP_PLAN.md")
    }

    override fun destroy() {
        WebGpuHandles.release(vertexBuffer.handle)
        WebGpuHandles.release(indexBuffer.handle)
    }
}

// Referenced by Renderer.kt (same source set) -- GPUIndexFormat isn't derivable from an
// IntArray-typed index buffer alone, so this constant documents the assumption both files
// share: indices are always UInt32 here (matching Vulkan's VK_INDEX_TYPE_UINT32 usage).
internal val meshIndexFormat = GPUIndexFormat.Uint32
