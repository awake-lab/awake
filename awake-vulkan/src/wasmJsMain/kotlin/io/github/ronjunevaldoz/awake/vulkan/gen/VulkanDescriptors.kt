/*
 * Awake
 * Awake.awake-vulkan.wasmJsMain
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

package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorImageInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo

// Phase 2.5 (Web/WebGPU, decision D7) scaffolding-only stub -- see
// io.github.ronjunevaldoz.awake.vulkan.Vulkan.kt's header comment in this same source set
// for the full rationale.
actual object VulkanDescriptors {
    actual fun vkCreateDescriptorSetLayout(device: Long, createInfo: VkDescriptorSetLayoutCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyDescriptorSetLayout(device: Long, layout: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateDescriptorPool(device: Long, createInfo: VkDescriptorPoolCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyDescriptorPool(device: Long, pool: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkAllocateDescriptorSet(device: Long, pool: Long, layout: Long): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkUpdateDescriptorSetBuffer(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        bufferInfo: VkDescriptorBufferInfo
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkUpdateDescriptorSetImage(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        imageInfo: VkDescriptorImageInfo
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdBindDescriptorSet(
        commandBuffer: Long,
        pipelineLayout: Long,
        firstSet: Int,
        descriptorSet: Long
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
}
