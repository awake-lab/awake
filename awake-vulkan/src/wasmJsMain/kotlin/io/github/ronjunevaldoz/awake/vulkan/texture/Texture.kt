package io.github.ronjunevaldoz.awake.vulkan.texture

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.ImageHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.ImageViewHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.SamplerHandle

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class Texture actual constructor(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    data: ByteArray,
    width: Int,
    height: Int
) {
    actual var image: ImageHandle = ImageHandle(0)
    actual var imageMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
    actual var imageView: ImageViewHandle = ImageViewHandle(0)
    actual var sampler: SamplerHandle = SamplerHandle(0)

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
