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

// Phase 2.5 (Web/WebGPU, decision D7) is scaffolding-only right now -- this file exists
// purely to get `awake-vulkan` compiling for the `wasmJs` target (mirrors the very first
// iOS stub-out pass, see `git show 38f76029dbbbde0b572a5337151265b173a3964c`, before real
// MoltenVK cinterop bodies were written). Every function below is an honest `TODO()` --
// no WebGPU implementation exists yet. See docs/MVP_PLAN.md's Phase 2.5 section.
actual object Vulkan {
    actual fun vkCreateInstance(createInfo: VkInstanceCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyInstance(instance: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkEnumerateInstanceLayerProperties(): Array<VkLayerProperties> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkEnumerateInstanceExtensionProperties(layerName: String?): Array<VkExtensionProperties> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkEnumerateDeviceExtensionProperties(
        physicalDevice: Long,
        layerName: String?
    ): Array<VkExtensionProperties> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkEnumeratePhysicalDevices(instance: Long): LongArray =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceProperties(physicalDevice: Long): VkPhysicalDeviceProperties =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceFeatures(physicalDevice: Long): VkPhysicalDeviceFeatures =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice: Long): Array<VkQueueFamilyProperties> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetSwapchainImagesKHR(device: Long, swapchain: Long): LongArray =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateDevice(physicalDevice: Long, deviceInfo: VkDeviceCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyDevice(device: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetDeviceQueue(device: Long, queueFamilyIndex: Int, queueIndex: Int): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateAndroidSurfaceKHR(instance: Long, surfaceInfo: VkAndroidSurfaceCreateInfoKHR): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceSurfaceSupportKHR(
        physicalDevice: Long,
        queueFamilyIndex: Int,
        surface: Long
    ): Boolean =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroySurfaceKHR(instance: Long, surface: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        physicalDevice: Long,
        surface: Long
    ): VkSurfaceCapabilitiesKHR =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkSurfaceFormatKHR> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkPresentModeKHR> =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateSwapchainKHR(device: Long, createInfoKHR: VkSwapchainCreateInfoKHR): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroySwapchainKHR(device: Long, swapchainKHR: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateImageView(device: Long, createInfo: VkImageViewCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyImageView(device: Long, imageView: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateShaderModule(device: Long, createInfo: VkShaderModuleCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyShaderModule(device: Long, shaderModule: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreatePipelineCache(device: Long, createInfo: VkPipelineCacheCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyPipelineCache(device: Long, pipelineCache: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreatePipelineLayout(device: Long, createInfo: VkPipelineLayoutCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyPipelineLayout(device: Long, pipelineLayout: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateGraphicsPipelines(
        device: Long,
        pipelineCache: Long,
        createInfos: Array<VkGraphicsPipelineCreateInfo>
    ): LongArray =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyPipeline(device: Long, pipeline: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateRenderPass(device: Long, createInfo: VkRenderPassCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyRenderPass(device: Long, renderPass: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateFramebuffer(device: Long, framebufferInfo: VkFramebufferCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyFramebuffer(device: Long, framebuffer: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkAllocateCommandBuffers(device: Long, createInfo: VkCommandBufferAllocateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkBeginCommandBuffer(commandBuffer: Long, beginInfo: VkCommandBufferBeginInfo): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateCommandPool(device: Long, createInfo: VkCommandPoolCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyCommandPool(device: Long, commandPool: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdBindPipeline(
        commandBuffer: Long,
        pipelineBindPoint: VkPipelineBindPoint,
        graphicsPipeline: Long
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdSetViewport(commandBuffer: Long, firstViewport: Int, viewports: Array<VkViewport>): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdSetScissor(commandBuffer: Long, firstScissor: Int, scissors: Array<VkRect2D>): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdDraw(
        commandBuffer: Long,
        vertexCount: Int,
        instanceCount: Int,
        firstVertex: Int,
        firstInstance: Int
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdBeginRenderPass(
        commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents
    ): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCmdEndRenderPass(commandBuffer: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkEndCommandBuffer(commandBuffer: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateSemaphore(device: Long, createInfo: VkSemaphoreCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroySemaphore(device: Long, semaphore: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateFence(device: Long, createInfo: VkFenceCreateInfo): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyFence(device: Long, fence: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkWaitForFences(device: Long, fences: LongArray, waitAll: Boolean, timeout: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkResetFences(device: Long, fences: LongArray): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkAcquireNextImageKHR(
        device: Long,
        swapchain: Long,
        timeout: Long,
        semaphore: Long,
        fence: Long
    ): Int =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkResetCommandBuffer(commandBuffer: Long, flags: Int): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkQueueSubmit(queue: Long, pSubmits: Array<VkSubmitInfo>, fence: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkQueuePresentKHR(queue: Long, pPresentInfoKHR: VkPresentInfoKHR): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkCreateDebugUtilsMessengerEXT(
        instance: Long,
        createInfo: VkDebugUtilsMessengerCreateInfoEXT
    ): Long =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")

    actual fun vkDestroyDebugUtilsMessengerEXT(instance: Long, debugUtilsMessenger: Long): Unit =
        TODO("WebGPU not yet implemented -- see Phase 2.5, docs/MVP_PLAN.md")
}
