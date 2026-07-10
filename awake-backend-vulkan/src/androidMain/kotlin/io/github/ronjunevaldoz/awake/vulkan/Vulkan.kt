/*
 * Awake
 * Awake.awake-vulkan.main
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

package io.github.ronjunevaldoz.awake.vulkan

import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtensionProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkLayerProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkQueueFamilyProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceCapabilitiesKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkSurfaceFormatKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkAndroidSurfaceCreateInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandPoolCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkDeviceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFramebufferCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkImageViewCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkInstanceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSemaphoreCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSwapchainCreateInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCreateInfoEXT
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceFeatures
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceProperties

actual object Vulkan {
    init {
        System.loadLibrary("awake-vulkan")
    }

    actual external fun vkCreateInstance(createInfo: VkInstanceCreateInfo): Long
    actual external fun vkDestroyInstance(instance: Long)


    /**
     * Enumerates the Vulkan extension properties available for the instance.
     *
     * @return An array of VkExtensionProperties representing the available instance extensions.
     */
    actual external fun vkEnumerateInstanceLayerProperties(): Array<VkLayerProperties>
    actual external fun vkEnumerateInstanceExtensionProperties(layerName: String?): Array<VkExtensionProperties>
    actual external fun vkEnumerateDeviceExtensionProperties(
        physicalDevice: Long,
        layerName: String?
    ): Array<VkExtensionProperties>

    actual external fun vkEnumeratePhysicalDevices(instance: Long): LongArray

    actual external fun vkGetPhysicalDeviceProperties(physicalDevice: Long): VkPhysicalDeviceProperties
    actual external fun vkGetPhysicalDeviceFeatures(physicalDevice: Long): VkPhysicalDeviceFeatures
    actual external fun vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice: Long): Array<VkQueueFamilyProperties>
    actual external fun vkGetSwapchainImagesKHR(device: Long, swapchain: Long): LongArray

    actual external fun vkCreateDevice(physicalDevice: Long, deviceInfo: VkDeviceCreateInfo): Long
    actual external fun vkDestroyDevice(device: Long)

    actual external fun vkGetDeviceQueue(device: Long, queueFamilyIndex: Int, queueIndex: Int): Long

    actual external fun vkCreateAndroidSurfaceKHR(
        instance: Long,
        surfaceInfo: VkAndroidSurfaceCreateInfoKHR
    ): Long

    actual external fun vkGetPhysicalDeviceSurfaceSupportKHR(
        physicalDevice: Long,
        queueFamilyIndex: Int,
        surface: Long
    ): Boolean

    actual external fun vkDestroySurfaceKHR(instance: Long, surface: Long)

    actual external fun vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        physicalDevice: Long,
        surface: Long
    ): VkSurfaceCapabilitiesKHR

    actual external fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkSurfaceFormatKHR>

    actual external fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkPresentModeKHR>

    actual external fun vkCreateSwapchainKHR(
        device: Long,
        createInfoKHR: VkSwapchainCreateInfoKHR
    ): Long

    actual external fun vkDestroySwapchainKHR(
        device: Long,
        swapchainKHR: Long
    )

    actual external fun vkCreateDebugUtilsMessengerEXT(
        instance: Long,
        createInfo: VkDebugUtilsMessengerCreateInfoEXT
    ): Long

    actual external fun vkDestroyDebugUtilsMessengerEXT(instance: Long, debugUtilsMessenger: Long)
    actual external fun vkCreateImageView(
        device: Long,
        createInfo: VkImageViewCreateInfo
    ): Long

    actual external fun vkDestroyImageView(device: Long, imageView: Long)
    actual external fun vkCreateShaderModule(
        device: Long,
        createInfo: VkShaderModuleCreateInfo
    ): Long

    actual external fun vkDestroyShaderModule(device: Long, shaderModule: Long)
    actual external fun vkCreatePipelineCache(
        device: Long,
        createInfo: VkPipelineCacheCreateInfo
    ): Long

    actual external fun vkDestroyPipelineCache(
        device: Long,
        pipelineCache: Long
    )

    actual external fun vkCreateGraphicsPipelines(
        device: Long,
        pipelineCache: Long,
        createInfos: Array<VkGraphicsPipelineCreateInfo>
    ): LongArray

    actual external fun vkDestroyPipeline(device: Long, pipeline: Long)
    actual external fun vkCreatePipelineLayout(
        device: Long,
        createInfo: VkPipelineLayoutCreateInfo
    ): Long

    actual external fun vkDestroyPipelineLayout(device: Long, pipelineLayout: Long)
    actual external fun vkCreateRenderPass(
        device: Long,
        createInfo: VkRenderPassCreateInfo
    ): Long

    actual external fun vkDestroyRenderPass(device: Long, renderPass: Long)
    actual external fun vkCreateFramebuffer(
        device: Long,
        framebufferInfo: VkFramebufferCreateInfo
    ): Long

    actual external fun vkDestroyFramebuffer(device: Long, framebuffer: Long)
    actual external fun vkAllocateCommandBuffers(
        device: Long,
        createInfo: VkCommandBufferAllocateInfo
    ): Long

    actual external fun vkBeginCommandBuffer(
        commandBuffer: Long,
        beginInfo: VkCommandBufferBeginInfo
    )

    actual external fun vkCreateCommandPool(
        device: Long,
        createInfo: VkCommandPoolCreateInfo
    ): Long

    actual external fun vkDestroyCommandPool(device: Long, commandPool: Long)
    actual external fun vkCmdBindPipeline(
        commandBuffer: Long,
        pipelineBindPoint: VkPipelineBindPoint,
        graphicsPipeline: Long
    )

    actual external fun vkCmdSetViewport(
        commandBuffer: Long,
        firstViewport: Int,
        viewports: Array<VkViewport>
    )

    actual external fun vkCmdSetScissor(
        commandBuffer: Long,
        firstScissor: Int,
        scissors: Array<VkRect2D>
    )

    actual external fun vkCmdDraw(
        commandBuffer: Long,
        vertexCount: Int,
        instanceCount: Int,
        firstVertex: Int,
        firstInstance: Int
    )

    actual external fun vkCmdBeginRenderPass(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents
    )

    actual external fun vkCmdEndRenderPass(commandBuffer: Long)

    actual external fun vkEndCommandBuffer(commandBuffer: Long)

    @VkReturnType("VkSemaphore")
    actual external fun vkCreateSemaphore(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkSemaphoreCreateInfo
    ): Long

    actual external fun vkDestroySemaphore(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkSemaphore") semaphore: Long
    )

    @VkReturnType("VkFence")
    actual external fun vkCreateFence(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkFenceCreateInfo
    ): Long

    actual external fun vkDestroyFence(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkFence") fence: Long
    )

    actual external fun vkWaitForFences(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkFence") fences: LongArray,
        waitAll: Boolean,
        timeout: Long
    )

    actual external fun vkResetFences(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkFence") fences: LongArray
    )

    actual external fun vkAcquireNextImageKHR(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkSwapchainKHR") swapchain: Long,
        timeout: Long,
        @VkHandleRef("VkSemaphore") semaphore: Long,
        @VkHandleRef("VkFence") fence: Long,
    ): Int

    actual external fun vkResetCommandBuffer(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        flags: Int
    )

    actual external fun vkQueueSubmit(
        @VkHandleRef("VkQueue") queue: Long,
        pSubmits: Array<VkSubmitInfo>,
        @VkHandleRef("VkFence") fence: Long
    )

    actual external fun vkQueuePresentKHR(
        @VkHandleRef("VkQueue") queue: Long,
        pPresentInfoKHR: VkPresentInfoKHR
    )
}