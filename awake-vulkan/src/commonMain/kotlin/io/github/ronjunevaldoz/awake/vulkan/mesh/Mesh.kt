/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.vulkan.mesh

import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: `expect class` -- see
 * [io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice]'s doc comment for why. Real
 * Vulkan body in `vulkanMain/.../mesh/Mesh.kt`.
 *
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): implements
 * [io.github.ronjunevaldoz.awake.render.mesh.Mesh] -- an `expect` class can implement an
 * interface declared in a different module, so this doesn't change
 * `VulkanApplication.kt`'s existing `Mesh(graphicsDevice, ...)` construction pattern at all.
 */
expect class Mesh(
    graphicsDevice: GraphicsDevice,
    runOneTimeCommands: ((commandBuffer: Long) -> Unit) -> Unit,
    vertices: FloatArray,
    indices: IntArray
) : RenderMesh {
    var vertexBuffer: BufferHandle
    var vertexBufferMemory: DeviceMemoryHandle
    var indexBuffer: BufferHandle
    var indexBufferMemory: DeviceMemoryHandle
    val indexCount: Int

    override fun bind(commandBuffer: Long)
    override fun draw(commandBuffer: Long)
    override fun destroy()
}
