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

package io.github.ronjunevaldoz.awake.vulkan.commands

import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandPoolCreateFlagBits
import io.github.ronjunevaldoz.awake.vulkan.handles.CommandPoolHandle
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.utils.findQueueFamilies

/**
 * Phase 2 (renderer abstraction): owns the command pool and the graphics queue used for
 * one-time (upload/transfer) command buffers -- extracted verbatim from
 * `VulkanApplication`'s `createCommandPool`/`runOneTimeCommands` functions. This is the
 * collaborator [io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh] and
 * [io.github.ronjunevaldoz.awake.vulkan.texture.Texture] previously received as a bare
 * `((commandBuffer: Long) -> Unit) -> Unit` lambda -- passing this instead (or its bound
 * `::runOneTimeCommands` method reference, which is call-compatible) gives callers a real
 * object to depend on instead of a floating function.
 *
 * [commandPool] is also exposed directly because `VulkanApplication` still allocates its
 * per-frame render command buffers from the same pool (a swapchain-frame concern, not a
 * transfer concern, so it isn't moved here).
 */
class TransferContext(private val graphicsDevice: GraphicsDevice) {
    private val device get() = graphicsDevice.device
    private val physicalDevice get() = graphicsDevice.physicalDevice
    private val surface get() = graphicsDevice.surface
    private val graphicsQueue get() = graphicsDevice.graphicsQueue

    var commandPool: CommandPoolHandle = CommandPoolHandle(0)
        private set

    init {
        val (graphicsFamily, _) = findQueueFamilies(physicalDevice, surface)

        val poolInfo = VkCommandPoolCreateInfo(
            flags = VkCommandPoolCreateFlagBits.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT.value,
            queueFamilyIndex = graphicsFamily!!
        )

        commandPool = CommandPoolHandle(Vulkan.vkCreateCommandPool(device, poolInfo))
    }

    /** Runs [block] on a fresh one-time command buffer, submitted and waited-on via a
     * throwaway fence rather than vkQueueWaitIdle/vkFreeCommandBuffers (neither of which
     * exist in the legacy Vulkan object yet). */
    fun runOneTimeCommands(block: (Long) -> Unit) {
        val allocInfo = VkCommandBufferAllocateInfo(
            commandPool = commandPool.handle,
            level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            commandBufferCount = 1
        )
        val commandBuffer = Vulkan.vkAllocateCommandBuffers(device, allocInfo)
        Vulkan.vkBeginCommandBuffer(
            commandBuffer,
            VkCommandBufferBeginInfo(
                flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value
            )
        )
        block(commandBuffer)
        Vulkan.vkEndCommandBuffer(commandBuffer)

        val fence = Vulkan.vkCreateFence(device, VkFenceCreateInfo())
        Vulkan.vkQueueSubmit(
            graphicsQueue,
            arrayOf(VkSubmitInfo(pCommandBuffers = arrayOf(commandBuffer))),
            fence
        )
        Vulkan.vkWaitForFences(device, longArrayOf(fence), true, Long.MAX_VALUE)
        Vulkan.vkDestroyFence(device, fence)
    }

    fun destroy() {
        Vulkan.vkDestroyCommandPool(device, commandPool.handle)
    }
}
