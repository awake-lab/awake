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
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import cnames.structs.VkInstance_T
import platform.MoltenVK.VK_STRUCTURE_TYPE_APPLICATION_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkInstanceVar
import platform.MoltenVK.vkCreateInstance as nativeVkCreateInstance
import platform.MoltenVK.vkDestroyInstance as nativeVkDestroyInstance
import platform.MoltenVK.VkApplicationInfo as NativeVkApplicationInfo
import platform.MoltenVK.VkInstanceCreateInfo as NativeVkInstanceCreateInfo

// Phase 6 (MoltenVK cinterop) has not landed yet — see docs/MVP_PLAN.md.
//
// vkCreateInstance/vkDestroyInstance below are a real cinterop implementation (not a
// TODO stub) -- proof-of-concept that awake-vulkan's iOS target actually links against
// and calls into the vendored MoltenVK.xcframework (awake-vulkan/ios-native/MoltenVK).
// Compiled, not yet hardware/simulator-verified -- no iOS app target drives a real frame
// yet (Phase 6 is still "MoltenVK cinterop bindings," not "iOS demo runs"). The other 56
// functions below remain TODO() stubs; each needs this same struct-marshalling treatment.
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
        TODO("Not yet implemented")
    }

    actual fun vkGetDeviceQueue(device: Long, queueFamilyIndex: Int, queueIndex: Int): Long {
        TODO("Not yet implemented")
    }

    actual fun vkCreateAndroidSurfaceKHR(instance: Long, surfaceInfo: VkAndroidSurfaceCreateInfoKHR): Long {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceSurfaceSupportKHR(
        physicalDevice: Long,
        queueFamilyIndex: Int,
        surface: Long
    ): Boolean {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySurfaceKHR(instance: Long, surface: Long) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    actual fun vkCreateImageView(device: Long, createInfo: VkImageViewCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyImageView(device: Long, imageView: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateShaderModule(device: Long, createInfo: VkShaderModuleCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyShaderModule(device: Long, shaderModule: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreatePipelineCache(device: Long, createInfo: VkPipelineCacheCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipelineCache(device: Long, pipelineCache: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreatePipelineLayout(device: Long, createInfo: VkPipelineLayoutCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipelineLayout(device: Long, pipelineLayout: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateGraphicsPipelines(
        device: Long,
        pipelineCache: Long,
        createInfos: Array<VkGraphicsPipelineCreateInfo>
    ): LongArray {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyPipeline(device: Long, pipeline: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateRenderPass(device: Long, createInfo: VkRenderPassCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyRenderPass(device: Long, renderPass: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateFramebuffer(device: Long, framebufferInfo: VkFramebufferCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyFramebuffer(device: Long, framebuffer: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkAllocateCommandBuffers(device: Long, createInfo: VkCommandBufferAllocateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkBeginCommandBuffer(commandBuffer: Long, beginInfo: VkCommandBufferBeginInfo) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateCommandPool(device: Long, createInfo: VkCommandPoolCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyCommandPool(device: Long, commandPool: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdBindPipeline(
        commandBuffer: Long,
        pipelineBindPoint: VkPipelineBindPoint,
        graphicsPipeline: Long
    ) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    actual fun vkCmdBeginRenderPass(
        commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents
    ) {
        TODO("Not yet implemented")
    }

    actual fun vkCmdEndRenderPass(commandBuffer: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkEndCommandBuffer(commandBuffer: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateSemaphore(device: Long, createInfo: VkSemaphoreCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySemaphore(device: Long, semaphore: Long) {
        TODO("Not yet implemented")
    }

    actual fun vkCreateFence(device: Long, createInfo: VkFenceCreateInfo): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroyFence(device: Long, fence: Long) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}
