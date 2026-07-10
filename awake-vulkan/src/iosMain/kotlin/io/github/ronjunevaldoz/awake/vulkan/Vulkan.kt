/*
 * Awake
 * Awake.awake-vulkan.iosMain
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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import cnames.structs.VkCommandPool_T
import cnames.structs.VkFence_T
import cnames.structs.VkFramebuffer_T
import cnames.structs.VkImageView_T
import cnames.structs.VkInstance_T
import cnames.structs.VkPipelineCache_T
import cnames.structs.VkPipelineLayout_T
import cnames.structs.VkPipeline_T
import cnames.structs.VkRenderPass_T
import cnames.structs.VkSemaphore_T
import cnames.structs.VkShaderModule_T
import cnames.structs.VkSurfaceKHR_T
import cnames.structs.VkSwapchainKHR_T
import platform.MoltenVK.VK_STRUCTURE_TYPE_APPLICATION_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkInstanceVar
import platform.MoltenVK.VkQueueVar
import platform.MoltenVK.vkBeginCommandBuffer as nativeVkBeginCommandBuffer
import platform.MoltenVK.vkCmdBindPipeline as nativeVkCmdBindPipeline
import platform.MoltenVK.vkCmdDraw as nativeVkCmdDraw
import platform.MoltenVK.vkCmdEndRenderPass as nativeVkCmdEndRenderPass
import platform.MoltenVK.vkCreateInstance as nativeVkCreateInstance
import platform.MoltenVK.vkDestroyCommandPool as nativeVkDestroyCommandPool
import platform.MoltenVK.vkDestroyDebugUtilsMessengerEXT as nativeVkDestroyDebugUtilsMessengerEXT
import platform.MoltenVK.vkDestroyDevice as nativeVkDestroyDevice
import platform.MoltenVK.vkDestroyFence as nativeVkDestroyFence
import platform.MoltenVK.vkDestroyFramebuffer as nativeVkDestroyFramebuffer
import platform.MoltenVK.vkDestroyImageView as nativeVkDestroyImageView
import platform.MoltenVK.vkDestroyInstance as nativeVkDestroyInstance
import platform.MoltenVK.vkDestroyPipeline as nativeVkDestroyPipeline
import platform.MoltenVK.vkDestroyPipelineCache as nativeVkDestroyPipelineCache
import platform.MoltenVK.vkDestroyPipelineLayout as nativeVkDestroyPipelineLayout
import platform.MoltenVK.vkDestroyRenderPass as nativeVkDestroyRenderPass
import platform.MoltenVK.vkDestroySemaphore as nativeVkDestroySemaphore
import platform.MoltenVK.vkDestroyShaderModule as nativeVkDestroyShaderModule
import platform.MoltenVK.vkDestroySurfaceKHR as nativeVkDestroySurfaceKHR
import platform.MoltenVK.vkDestroySwapchainKHR as nativeVkDestroySwapchainKHR
import platform.MoltenVK.vkEndCommandBuffer as nativeVkEndCommandBuffer
import platform.MoltenVK.vkGetDeviceQueue as nativeVkGetDeviceQueue
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceSupportKHR as nativeVkGetPhysicalDeviceSurfaceSupportKHR
import platform.MoltenVK.vkResetCommandBuffer as nativeVkResetCommandBuffer
import platform.MoltenVK.VkApplicationInfo as NativeVkApplicationInfo
import platform.MoltenVK.VkCommandBufferBeginInfo as NativeVkCommandBufferBeginInfo
import platform.MoltenVK.VkInstanceCreateInfo as NativeVkInstanceCreateInfo

// Phase 6 (MoltenVK cinterop) is in progress -- see docs/MVP_PLAN.md.
//
// Functions with real cinterop bodies (not TODO() stubs) actually link against and call
// into the vendored MoltenVK.xcframework (awake-vulkan/ios-native/MoltenVK). Compiled,
// not yet hardware/simulator-verified -- no iOS app target drives a real frame yet (Phase
// 6 is still "MoltenVK cinterop bindings," not "iOS demo runs"). Functions still marked
// TODO() below need the same struct-marshalling treatment -- see vkCreateInstance for the
// nested-struct-and-arrays pattern, or any vkDestroyXxx for the trivial handle-only one.
@OptIn(ExperimentalForeignApi::class)
actual object Vulkan {
    actual fun vkCreateInstance(createInfo: VkInstanceCreateInfo): Long = memScoped {
        val nativeAppInfo = createInfo.pApplicationInfo?.firstOrNull()?.let { appInfo ->
            alloc<NativeVkApplicationInfo>().apply {
                sType = VK_STRUCTURE_TYPE_APPLICATION_INFO
                pNext = null
                pApplicationName = appInfo.pApplicationName.cstr.ptr
                applicationVersion = appInfo.applicationVersion.toUInt()
                pEngineName = appInfo.pEngineName.cstr.ptr
                engineVersion = appInfo.engineVersion.toUInt()
                apiVersion = appInfo.apiVersion.toUInt()
            }
        }
        val nativeCreateInfo = alloc<NativeVkInstanceCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            pApplicationInfo = nativeAppInfo?.ptr
            val layerNames = createInfo.ppEnabledLayerNames
            enabledLayerCount = (layerNames?.size ?: 0).toUInt()
            ppEnabledLayerNames = layerNames?.let { names -> allocArrayOf(names.map { it.cstr.ptr }) }
            val extensionNames = createInfo.ppEnabledExtensionNames
            enabledExtensionCount = (extensionNames?.size ?: 0).toUInt()
            ppEnabledExtensionNames =
                extensionNames?.let { names -> allocArrayOf(names.map { it.cstr.ptr }) }
        }
        val instanceVar = alloc<VkInstanceVar>()
        val result = nativeVkCreateInstance(nativeCreateInfo.ptr, null, instanceVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateInstance failed: $result" }
        // VK_SUCCESS guarantees pInstance was written -- see the Vulkan spec's
        // vkCreateInstance return-value contract.
        instanceVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyInstance(instance: Long) {
        val nativeInstance = instance.toCPointer<VkInstance_T>()
        nativeVkDestroyInstance(nativeInstance, null)
    }

    actual fun vkEnumerateInstanceLayerProperties(): Array<VkLayerProperties> {
        TODO("Not yet implemented")
    }

    actual fun vkEnumerateInstanceExtensionProperties(layerName: String?): Array<VkExtensionProperties> {
        TODO("Not yet implemented")
    }

    actual fun vkEnumerateDeviceExtensionProperties(
        physicalDevice: Long,
        layerName: String?
    ): Array<VkExtensionProperties> {
        TODO("Not yet implemented")
    }

    actual fun vkEnumeratePhysicalDevices(instance: Long): LongArray {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceProperties(physicalDevice: Long): VkPhysicalDeviceProperties {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceFeatures(physicalDevice: Long): VkPhysicalDeviceFeatures {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice: Long): Array<VkQueueFamilyProperties> {
        TODO("Not yet implemented")
    }

    actual fun vkGetSwapchainImagesKHR(device: Long, swapchain: Long): LongArray {
        TODO("Not yet implemented")
    }

    actual fun vkCreateDevice(physicalDevice: Long, deviceInfo: VkDeviceCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyDevice(device: Long) {
        nativeVkDestroyDevice(device.toCPointer(), null)
    }

    actual fun vkGetDeviceQueue(device: Long, queueFamilyIndex: Int, queueIndex: Int): Long = memScoped {
        val queueVar = alloc<VkQueueVar>()
        nativeVkGetDeviceQueue(device.toCPointer(), queueFamilyIndex.toUInt(), queueIndex.toUInt(), queueVar.ptr)
        queueVar.value!!.rawValue.toLong()
    }

    actual fun vkCreateAndroidSurfaceKHR(instance: Long, surfaceInfo: VkAndroidSurfaceCreateInfoKHR): Long {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceSurfaceSupportKHR(
        physicalDevice: Long,
        queueFamilyIndex: Int,
        surface: Long
    ): Boolean = memScoped {
        val supportedVar = alloc<UIntVar>()
        nativeVkGetPhysicalDeviceSurfaceSupportKHR(
            physicalDevice.toCPointer(),
            queueFamilyIndex.toUInt(),
            surface.toCPointer<VkSurfaceKHR_T>(),
            supportedVar.ptr
        )
        supportedVar.value != 0u
    }

    actual fun vkDestroySurfaceKHR(instance: Long, surface: Long) {
        nativeVkDestroySurfaceKHR(instance.toCPointer(), surface.toCPointer<VkSurfaceKHR_T>(), null)
    }

    actual fun vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        physicalDevice: Long,
        surface: Long
    ): VkSurfaceCapabilitiesKHR {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkSurfaceFormatKHR> {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkPresentModeKHR> {
        TODO("Not yet implemented")
    }

    actual fun vkCreateSwapchainKHR(device: Long, createInfoKHR: VkSwapchainCreateInfoKHR): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySwapchainKHR(device: Long, swapchainKHR: Long) {
        nativeVkDestroySwapchainKHR(device.toCPointer(), swapchainKHR.toCPointer<VkSwapchainKHR_T>(), null)
    }

    actual fun vkCreateImageView(device: Long, createInfo: VkImageViewCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyImageView(device: Long, imageView: Long) {
        nativeVkDestroyImageView(device.toCPointer(), imageView.toCPointer<VkImageView_T>(), null)
    }

    actual fun vkCreateShaderModule(device: Long, createInfo: VkShaderModuleCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyShaderModule(device: Long, shaderModule: Long) {
        nativeVkDestroyShaderModule(device.toCPointer(), shaderModule.toCPointer<VkShaderModule_T>(), null)
    }

    actual fun vkCreatePipelineCache(device: Long, createInfo: VkPipelineCacheCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipelineCache(device: Long, pipelineCache: Long) {
        nativeVkDestroyPipelineCache(device.toCPointer(), pipelineCache.toCPointer<VkPipelineCache_T>(), null)
    }

    actual fun vkCreatePipelineLayout(device: Long, createInfo: VkPipelineLayoutCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipelineLayout(device: Long, pipelineLayout: Long) {
        nativeVkDestroyPipelineLayout(device.toCPointer(), pipelineLayout.toCPointer<VkPipelineLayout_T>(), null)
    }

    actual fun vkCreateGraphicsPipelines(
        device: Long,
        pipelineCache: Long,
        createInfos: Array<VkGraphicsPipelineCreateInfo>
    ): LongArray {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipeline(device: Long, pipeline: Long) {
        nativeVkDestroyPipeline(device.toCPointer(), pipeline.toCPointer<VkPipeline_T>(), null)
    }

    actual fun vkCreateRenderPass(device: Long, createInfo: VkRenderPassCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyRenderPass(device: Long, renderPass: Long) {
        nativeVkDestroyRenderPass(device.toCPointer(), renderPass.toCPointer<VkRenderPass_T>(), null)
    }

    actual fun vkCreateFramebuffer(device: Long, framebufferInfo: VkFramebufferCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyFramebuffer(device: Long, framebuffer: Long) {
        nativeVkDestroyFramebuffer(device.toCPointer(), framebuffer.toCPointer<VkFramebuffer_T>(), null)
    }

    actual fun vkAllocateCommandBuffers(device: Long, createInfo: VkCommandBufferAllocateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkBeginCommandBuffer(commandBuffer: Long, beginInfo: VkCommandBufferBeginInfo) = memScoped {
        val nativeBeginInfo = alloc<NativeVkCommandBufferBeginInfo>().apply {
            sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO
            pNext = null
            flags = beginInfo.flags.toUInt()
            // pInheritanceInfo only matters for secondary command buffers -- out of scope
            // for this pass, matching this codebase's primary-command-buffer-only usage.
            pInheritanceInfo = null
        }
        nativeVkBeginCommandBuffer(commandBuffer.toCPointer(), nativeBeginInfo.ptr)
        Unit
    }

    actual fun vkCreateCommandPool(device: Long, createInfo: VkCommandPoolCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyCommandPool(device: Long, commandPool: Long) {
        nativeVkDestroyCommandPool(device.toCPointer(), commandPool.toCPointer<VkCommandPool_T>(), null)
    }

    actual fun vkCmdBindPipeline(
        commandBuffer: Long,
        pipelineBindPoint: VkPipelineBindPoint,
        graphicsPipeline: Long
    ) {
        nativeVkCmdBindPipeline(
            commandBuffer.toCPointer(),
            pipelineBindPoint.value.toUInt(),
            graphicsPipeline.toCPointer<VkPipeline_T>()
        )
    }

    actual fun vkCmdSetViewport(commandBuffer: Long, firstViewport: Int, viewports: Array<VkViewport>) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdSetScissor(commandBuffer: Long, firstScissor: Int, scissors: Array<VkRect2D>) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdDraw(
        commandBuffer: Long,
        vertexCount: Int,
        instanceCount: Int,
        firstVertex: Int,
        firstInstance: Int
    ) {
        nativeVkCmdDraw(
            commandBuffer.toCPointer(),
            vertexCount.toUInt(),
            instanceCount.toUInt(),
            firstVertex.toUInt(),
            firstInstance.toUInt()
        )
    }

    actual fun vkCmdBeginRenderPass(
        commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents
    ) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdEndRenderPass(commandBuffer: Long) {
        nativeVkCmdEndRenderPass(commandBuffer.toCPointer())
    }

    actual fun vkEndCommandBuffer(commandBuffer: Long) {
        nativeVkEndCommandBuffer(commandBuffer.toCPointer())
    }

    actual fun vkCreateSemaphore(device: Long, createInfo: VkSemaphoreCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySemaphore(device: Long, semaphore: Long) {
        nativeVkDestroySemaphore(device.toCPointer(), semaphore.toCPointer<VkSemaphore_T>(), null)
    }

    actual fun vkCreateFence(device: Long, createInfo: VkFenceCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyFence(device: Long, fence: Long) {
        nativeVkDestroyFence(device.toCPointer(), fence.toCPointer<VkFence_T>(), null)
    }

    actual fun vkWaitForFences(device: Long, fences: LongArray, waitAll: Boolean, timeout: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkResetFences(device: Long, fences: LongArray) {
        TODO("Not yet implemented")
    }

    actual fun vkAcquireNextImageKHR(
        device: Long,
        swapchain: Long,
        timeout: Long,
        semaphore: Long,
        fence: Long
    ): Int {
        TODO("Not yet implemented")
    }

    actual fun vkResetCommandBuffer(commandBuffer: Long, flags: Int) {
        nativeVkResetCommandBuffer(commandBuffer.toCPointer(), flags.toUInt())
    }

    actual fun vkQueueSubmit(queue: Long, pSubmits: Array<VkSubmitInfo>, fence: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkQueuePresentKHR(queue: Long, pPresentInfoKHR: VkPresentInfoKHR) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateDebugUtilsMessengerEXT(
        instance: Long,
        createInfo: VkDebugUtilsMessengerCreateInfoEXT
    ): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyDebugUtilsMessengerEXT(instance: Long, debugUtilsMessenger: Long) {
        nativeVkDestroyDebugUtilsMessengerEXT(
            instance.toCPointer(),
            debugUtilsMessenger.toCPointer<cnames.structs.VkDebugUtilsMessengerEXT_T>(),
            null
        )
    }
}
