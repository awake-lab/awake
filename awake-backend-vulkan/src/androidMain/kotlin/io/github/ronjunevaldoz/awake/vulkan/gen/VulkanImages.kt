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

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

actual object VulkanImages {
    actual external fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long
    actual external fun vkDestroyImage(device: Long, image: Long)
    actual external fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements
    actual external fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long)
    actual external fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long
    actual external fun vkDestroySampler(device: Long, sampler: Long)
    actual external fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int
    )

    actual external fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy
    )
}
