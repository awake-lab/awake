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

import io.github.ronjunevaldoz.awake.vulkan.models.VkMemoryRequirements
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkBufferImageCopy
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSamplerCreateInfo

// Phase 2.5 (Web/WebGPU, decision D7) scaffolding-only stub -- see
// io.github.ronjunevaldoz.awake.vulkan.Vulkan.kt's header comment in this same source set
// for the full rationale.
actual object VulkanImages {
    actual fun vkCreateImage(device: Long, createInfo: VkImageCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyImage(device: Long, image: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetImageMemoryRequirements(device: Long, image: Long): VkMemoryRequirements =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkBindImageMemory(device: Long, image: Long, memory: Long, memoryOffset: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateSampler(device: Long, createInfo: VkSamplerCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroySampler(device: Long, sampler: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkTransitionImageLayout(
        commandBuffer: Long,
        image: Long,
        oldLayout: Int,
        newLayout: Int
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdCopyBufferToImage(
        commandBuffer: Long,
        srcBuffer: Long,
        dstImage: Long,
        copy: VkBufferImageCopy
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
}
