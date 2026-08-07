// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.mesh

import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
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
 *
 * [lineIndexBuffer]/[lineIndexCount] are a second, `LineList`-topology index buffer derived
 * from [indices] -- each triangle's 3 edges become 2-index line entries (shared edges between
 * adjacent triangles are emitted twice, same overdraw a Vulkan `VK_POLYGON_MODE_LINE` pipeline
 * already produces for the same reason: no half-edge/adjacency structure is built to dedupe
 * them). `Renderer.wireframe`'s WebGPU implementation binds these instead of [indexBuffer]
 * against a `LineList` pipeline -- see that class's own doc comment for why this approach (not
 * a barycentric-coordinate fragment shader) was chosen: WebGPU has no polygon-mode-line
 * equivalent, and this reuses the exact same vertex buffer/shader, only the index buffer and
 * pipeline topology differ. Built unconditionally (like [indexBuffer] itself), not lazily on
 * first wireframe use -- keeps this class's resource lifecycle a single, unconditional init
 * block instead of a second lazy-build path a mesh's caller would need to remember to trigger.
 */
class Mesh(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    vertices: FloatArray,
    indices: IntArray,
    override val format: VertexFormat = VertexFormat.PositionColorUv
) : RenderMesh {
    var vertexBuffer: BufferHandle
    var vertexBufferMemory: DeviceMemoryHandle
    var indexBuffer: BufferHandle
    var indexBufferMemory: DeviceMemoryHandle
    val indexCount: Int = indices.size
    var lineIndexBuffer: BufferHandle
    val lineIndexCount: Int

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

        val lineIndices = triangleIndicesToLineIndices(indices)
        lineIndexCount = lineIndices.size
        val rawLineIndexBuffer: GPUBuffer = device.createBuffer(
            BufferDescriptor(
                size = (lineIndices.size * Int.SIZE_BYTES).toULong(),
                usage = GPUBufferUsage.Index or GPUBufferUsage.CopyDst
            )
        )
        device.queue.writeBuffer(rawLineIndexBuffer, 0uL, fastArrayBufferOf(lineIndices))
        lineIndexBuffer = BufferHandle(WebGpuHandles.register(rawLineIndexBuffer))
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
        WebGpuHandles.release(lineIndexBuffer.handle)
    }
}

/** One (start, end) index pair per triangle edge, 3 edges per triangle -- see [Mesh]'s own
 * `lineIndexBuffer` doc comment for the overdraw/no-dedup rationale. */
internal fun triangleIndicesToLineIndices(indices: IntArray): IntArray {
    val triangleCount = indices.size / 3
    val lineIndices = IntArray(triangleCount * 6)
    var triangle = 0
    while (triangle < triangleCount) {
        val base = triangle * 3
        val i0 = indices[base]
        val i1 = indices[base + 1]
        val i2 = indices[base + 2]
        val out = triangle * 6
        lineIndices[out] = i0
        lineIndices[out + 1] = i1
        lineIndices[out + 2] = i1
        lineIndices[out + 3] = i2
        lineIndices[out + 4] = i2
        lineIndices[out + 5] = i0
        triangle += 1
    }
    return lineIndices
}

// Referenced by Renderer.kt (same source set) -- GPUIndexFormat isn't derivable from an
// IntArray-typed index buffer alone, so this constant documents the assumption both files
// share: indices are always UInt32 here (matching Vulkan's VK_INDEX_TYPE_UINT32 usage).
internal val meshIndexFormat = GPUIndexFormat.Uint32
