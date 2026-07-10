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

package io.github.ronjunevaldoz.awake.vulkan.material

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.BufferHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorPoolHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.handles.DeviceMemoryHandle
import io.github.ronjunevaldoz.awake.vulkan.texture.Texture

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: `expect class` -- see
 * [io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice]'s doc comment for why. Real
 * Vulkan body in `vulkanMain/.../material/Material.kt`.
 */
expect class Material(graphicsDevice: GraphicsDevice) {
    val descriptorSetLayout: DescriptorSetLayoutHandle
    var descriptorPool: DescriptorPoolHandle
    var descriptorSet: DescriptorSetHandle
    var uniformBuffer: BufferHandle
    var uniformBufferMemory: DeviceMemoryHandle

    fun createResources(texture: Texture)
    fun updateUniformBuffer(mvp: FloatArray)
    fun bind(commandBuffer: Long, pipelineLayout: Long)
    fun destroy()
}
