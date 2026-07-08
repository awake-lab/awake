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

package io.github.ronjunevaldoz.awake.vulkan.gen

import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorBufferInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDescriptorSetLayoutCreateInfo

/**
 * Phase 1d descriptor-set API surface, generated via jni-binding-generator (see
 * [VulkanBuffers] for the package-scoping rationale shared by every object in this
 * package). Deliberately scoped to a single descriptor set per allocate/update/bind call
 * (no `descriptorSetCount`/array-of-sets support) -- jni-binding-generator's array support
 * is proven for *struct fields* (Phase 1a) but not yet exercised here for function-level
 * array params/returns of handles, and the MVP's textured-cube milestone only ever needs
 * one descriptor set per frame-in-flight, so there's no real need to risk that untested path.
 */
expect object VulkanDescriptors {
    fun vkCreateDescriptorSetLayout(device: Long, createInfo: VkDescriptorSetLayoutCreateInfo): Long
    fun vkDestroyDescriptorSetLayout(device: Long, layout: Long)
    fun vkCreateDescriptorPool(device: Long, createInfo: VkDescriptorPoolCreateInfo): Long
    fun vkDestroyDescriptorPool(device: Long, pool: Long)
    fun vkAllocateDescriptorSet(device: Long, pool: Long, layout: Long): Long
    fun vkUpdateDescriptorSetBuffer(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        bufferInfo: VkDescriptorBufferInfo
    )

    fun vkCmdBindDescriptorSet(
        commandBuffer: Long,
        pipelineLayout: Long,
        firstSet: Int,
        descriptorSet: Long
    )
}
