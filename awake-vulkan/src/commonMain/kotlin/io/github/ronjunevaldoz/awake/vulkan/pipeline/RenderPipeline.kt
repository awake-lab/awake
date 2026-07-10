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

package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * Phase 2.5 (Web/WebGPU, decision D7) milestone 1: `expect class` -- see
 * [io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice]'s doc comment for why. Real
 * Vulkan body in `vulkanMain/.../pipeline/RenderPipeline.kt`.
 */
expect class RenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    vertShaderCode: ByteArray,
    fragShaderCode: ByteArray,
    vertexStride: Int
) {
    var renderPass: Long
    var pipelineLayout: Long
    var pipelineCache: Long
    var graphicsPipeline: LongArray

    fun bind(commandBuffer: Long)
    fun destroy()
}
