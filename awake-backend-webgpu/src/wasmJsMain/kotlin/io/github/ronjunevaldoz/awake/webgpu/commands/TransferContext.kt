// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.commands

import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.handles.CommandPoolHandle

// Phase 2.5 (Web/WebGPU, decision D7) milestone 1: compile-only stub -- see
// docs/MVP_PLAN.md.
class TransferContext(graphicsDevice: GraphicsDevice) {
    var commandPool: CommandPoolHandle = CommandPoolHandle(0)

    fun runOneTimeCommands(block: (Long) -> Unit) {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }

    fun destroy() {
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
    }
}
