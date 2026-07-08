/*
 * Awake
 * Awake.awake-vulkan.androidMain
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

actual object VulkanDescriptors {
    actual external fun vkCreateDescriptorSetLayout(
        device: Long,
        createInfo: VkDescriptorSetLayoutCreateInfo
    ): Long

    actual external fun vkDestroyDescriptorSetLayout(device: Long, layout: Long)
    actual external fun vkCreateDescriptorPool(device: Long, createInfo: VkDescriptorPoolCreateInfo): Long
    actual external fun vkDestroyDescriptorPool(device: Long, pool: Long)
    actual external fun vkAllocateDescriptorSet(device: Long, pool: Long, layout: Long): Long
    actual external fun vkUpdateDescriptorSetBuffer(
        device: Long,
        dstSet: Long,
        dstBinding: Int,
        descriptorType: Int,
        bufferInfo: VkDescriptorBufferInfo
    )

    actual external fun vkCmdBindDescriptorSet(
        commandBuffer: Long,
        pipelineLayout: Long,
        firstSet: Int,
        descriptorSet: Long
    )
}
