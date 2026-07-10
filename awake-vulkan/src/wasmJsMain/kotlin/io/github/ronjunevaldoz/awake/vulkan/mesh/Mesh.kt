package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class Mesh actual constructor(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    vertices: FloatArray,
    indices: IntArray
) {
    actual var vertexBuffer: BufferHandle = BufferHandle(0)
    actual var vertexBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
    actual var indexBuffer: BufferHandle = BufferHandle(0)
    actual var indexBufferMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
    actual val indexCount: Int = indices.size

    actual fun bind(commandBuffer: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun draw(commandBuffer: Long) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
