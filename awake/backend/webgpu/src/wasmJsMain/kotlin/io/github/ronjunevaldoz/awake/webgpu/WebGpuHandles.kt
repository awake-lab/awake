// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu

/**
 * The `expect` renderer seam (GraphicsDevice/Mesh/RenderPipeline/etc., see
 * docs/MVP_PLAN.md's Phase 2.5 section) types every handle as `Long`, mirroring Vulkan's
 * raw-integer-handle model -- but wgpu4k hands back real typed Kotlin objects (GPUDevice,
 * GPUBuffer, GPURenderPipeline, ...), not integers. This table assigns each such object an
 * incrementing Long id so the wasmJs actuals can satisfy the existing Long-typed contract
 * without changing it (a contract change would ripple back into commonMain and force
 * re-verification across the 4 already-proven platforms, for zero benefit to them).
 * wasmJsMain-internal only -- invisible to commonMain and every other platform.
 */
object WebGpuHandles {
    private var nextHandle = 1L
    private val objects = HashMap<Long, Any>()

    fun register(value: Any): Long {
        val handle = nextHandle
        nextHandle += 1
        objects[handle] = value
        return handle
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resolve(handle: Long): T =
        objects[handle] as? T ?: error("No WebGPU object registered for handle $handle")

    fun release(handle: Long) {
        objects.remove(handle)
    }
}
