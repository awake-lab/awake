/*
 * Awake
 * Awake.awake-vulkan.desktopMain
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

// Phase 1b (desktop native build) has not landed yet — see docs/MVP_PLAN.md.
actual object VulkanImages {
    actual fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyImage(device: Long, image: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements {
        TODO("Not yet implemented")
    }

    actual fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySampler(device: Long, sampler: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int
    ) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy
    ) {
        TODO("Not yet implemented")
    }
}
