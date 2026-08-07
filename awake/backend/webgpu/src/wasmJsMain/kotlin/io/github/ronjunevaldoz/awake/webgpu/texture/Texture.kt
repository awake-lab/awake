// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.texture

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.ImageHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.ImageViewHandle
import io.github.ronjunevaldoz.awake.webgpu.handles.SamplerHandle

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
class Texture(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    data: ByteArray,
    width: Int,
    height: Int,
) {
    var image: ImageHandle = ImageHandle(0)
    var imageMemory: DeviceMemoryHandle = DeviceMemoryHandle(0)
    var imageView: ImageViewHandle = ImageViewHandle(0)
    var sampler: SamplerHandle = SamplerHandle(0)

    fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
