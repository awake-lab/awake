package io.github.ronjunevaldoz.awake.vulkan.commands

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.CommandPoolHandle

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
actual class TransferContext actual constructor(graphicsDevice: GraphicsDevice) {
    actual var commandPool: CommandPoolHandle = CommandPoolHandle(0)

    actual fun runOneTimeCommands(block: (Long) -> Unit) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    actual fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
