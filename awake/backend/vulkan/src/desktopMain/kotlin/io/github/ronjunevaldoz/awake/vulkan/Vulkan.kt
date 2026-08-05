// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
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

    /**
     * Creates a new Vulkan instance with the provided application information.
     *
     * @param createInfo The VkInstanceCreateInfo containing the application-specific information.
     * @return The handle to the created Vulkan instance.
     */
    @VkReturnType("VkInstance")
    actual external fun vkCreateInstance(createInfo: VkInstanceCreateInfo): Long

    /**
     * Destroys the specified Vulkan instance.
     *
     * @param instance The handle to the Vulkan instance to be destroyed.
     */
    actual external fun vkDestroyInstance(@VkHandleRef("VkInstance") instance: Long)

    /**
     * Enumerates the Vulkan extension properties available for the instance.
     *
     * @return An array of VkExtensionProperties representing the available instance extensions.
     */
    actual external fun vkEnumerateInstanceLayerProperties(): Array<VkLayerProperties>

    /**
     * Enumerates the Vulkan extension properties available for the instance.
     *
     * @return An array of VkExtensionProperties representing the available instance extensions.
     */
    actual external fun vkEnumerateInstanceExtensionProperties(layerName: String?): Array<VkExtensionProperties>

    /**
     * Enumerates the Vulkan extension properties available for a specific physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @return An array of VkExtensionProperties representing the available device extensions.
     */
    actual external fun vkEnumerateDeviceExtensionProperties(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        layerName: String?
    ): Array<VkExtensionProperties>

    /**
     * Enumerates the available Vulkan physical devices for the specified instance.
     *
     * @param instance The handle to the Vulkan instance.
     * @return An array of VkPhysicalDevice handle in a form of type Long
     */
    @VkReturnType("VkPhysicalDevice")
    actual external fun vkEnumeratePhysicalDevices(@VkHandleRef("VkInstance") instance: Long): LongArray

    /**
     * Retrieves properties of the specified physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @return The VkPhysicalDeviceProperties representing the properties of the physical device.
     */
    actual external fun vkGetPhysicalDeviceProperties(@VkHandleRef("VkPhysicalDevice") physicalDevice: Long): VkPhysicalDeviceProperties

    /**
     * Retrieves features of the specified physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @return The VkPhysicalDeviceFeatures representing the features of the physical device.
     */
    actual external fun vkGetPhysicalDeviceFeatures(@VkHandleRef("VkPhysicalDevice") physicalDevice: Long): VkPhysicalDeviceFeatures

    /**
     * Retrieves properties of the queue families available on the specified physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @return An array of VkQueueFamilyProperties representing the properties of queue families.
     */
    actual external fun vkGetPhysicalDeviceQueueFamilyProperties(@VkHandleRef("VkPhysicalDevice") physicalDevice: Long): Array<VkQueueFamilyProperties>

    /**
     * Retrieves the images associated with the specified Vulkan swapchain.
     *
     * @param device The handle to the Vulkan logical device.
     * @param swapchain The handle to the Vulkan swapchain.
     * @return An array of VkImage representing the images in the swapchain.
     */
    @VkReturnType("VkImage")
    actual external fun vkGetSwapchainImagesKHR(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkSwapchainKHR") swapchain: Long
    ): LongArray

    /**
     * Creates a new VkDevice object associated with the given physical device and using the provided device configuration.
     *
     * @param physicalDevice The handle to the physical device for which the logical device will be created.
     * @param deviceInfo The configuration settings for the logical device, encapsulated in VkDeviceCreateInfo.
     * @return A handle to the newly created VkDevice object.
     */
    @VkReturnType("VkDevice")
    actual external fun vkCreateDevice(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        deviceInfo: VkDeviceCreateInfo
    ): Long

    /**
     * Destroys the specified VkDevice object and releases its associated resources.
     *
     * @param device The handle to the logical device that will be destroyed.
     */
    actual external fun vkDestroyDevice(@VkHandleRef("VkDevice") device: Long)

    /**
     * Returns the VkQueue associated with the given device, queue family index, and queue index.
     *
     * @param device The handle to the logical device.
     * @param queueFamilyIndex The index of the queue family.
     * @param queueIndex The index of the queue within the queue family.
     * @return The VkQueue associated with the specified parameters.
     */
    @VkReturnType("VkQueue")
    actual external fun vkGetDeviceQueue(
        @VkHandleRef("VkDevice") device: Long,
        queueFamilyIndex: Int,
        queueIndex: Int
    ): Long

    /**
     * Creates an Android surface for Vulkan presentation.
     *
     * @param instance The handle to the Vulkan instance.
     * @param surfaceInfo Information required to create the Android surface.
     * @return The handle to the created Android surface.
     */
    @VkReturnType("VkSurfaceKHR")
    actual fun vkCreateAndroidSurfaceKHR(
        @VkHandleRef("VkInstance") instance: Long,
        surfaceInfo: VkAndroidSurfaceCreateInfoKHR
    ): Long {
        TODO("Not yet implemented")
    }

    /**
     * Checks if presentation is supported on the specified physical device and queue family.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @param queueFamilyIndex The index of the queue family to check for presentation support.
     * @param surface The handle to the surface to be presented.
     * @return `true` if presentation is supported, `false` otherwise.
     */
    actual external fun vkGetPhysicalDeviceSurfaceSupportKHR(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        queueFamilyIndex: Int,
        @VkHandleRef("VkSurfaceKHR") surface: Long
    ): Boolean

    /**
     * Destroys the Vulkan surface.
     *
     * @param instance The handle to the Vulkan instance.
     * @param surface The handle to the surface to be destroyed.
     */
    actual external fun vkDestroySurfaceKHR(
        @VkHandleRef("VkInstance") instance: Long,
        @VkHandleRef("VkSurfaceKHR") surface: Long
    )

    /**
     * Retrieves the capabilities of the surface on the specified physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @param surface The handle to the surface to retrieve capabilities from.
     * @return The capabilities of the specified surface.
     */
    actual external fun vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        @VkHandleRef("VkSurfaceKHR") surface: Long
    ): VkSurfaceCapabilitiesKHR

    /**
     * Retrieves the available surface formats on the specified physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @param surface The handle to the surface to query for formats.
     * @return An array of surface formats supported by the specified surface.
     */
    actual external fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        @VkHandleRef("VkSurfaceKHR") surface: Long
    ): Array<VkSurfaceFormatKHR>

    /**
     * Retrieves the supported presentation modes for the specified surface on the physical device.
     *
     * @param physicalDevice The handle to the Vulkan physical device.
     * @param surface The handle to the surface to query for presentation modes.
     * @return An array of supported presentation modes for the specified surface.
     */
    actual external fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        @VkHandleRef("VkPhysicalDevice") physicalDevice: Long,
        @VkHandleRef("VkSurfaceKHR") surface: Long
    ): Array<VkPresentModeKHR>

    /**
     * Creates a Vulkan swapchain for the specified device.
     *
     * @param device The handle to the Vulkan logical device.
     * @param createInfoKHR The structure containing swapchain creation information.
     * @return The handle to the created Vulkan swapchain.
     */
    @VkReturnType("VkSwapchainKHR")
    actual external fun vkCreateSwapchainKHR(
        @VkHandleRef("VkDevice") device: Long,
        createInfoKHR: VkSwapchainCreateInfoKHR
    ): Long

    /**
     * Destroys the Vulkan swapchain.
     *
     * @param device The handle to the Vulkan logical device.
     * @param swapchainKHR The handle to the swapchain to be destroyed.
     */
    actual external fun vkDestroySwapchainKHR(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkSwapchainKHR") swapchainKHR: Long
    )

    @VkReturnType("VkImageView")
    actual external fun vkCreateImageView(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkImageViewCreateInfo
    ): Long

    actual external fun vkDestroyImageView(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkImageView") imageView: Long
    )

    @VkReturnType("VkShaderModule")
    actual external fun vkCreateShaderModule(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkShaderModuleCreateInfo
    ): Long

    actual external fun vkDestroyShaderModule(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkShaderModule") shaderModule: Long
    )

    @VkReturnType("VkPipelineCache")
    actual external fun vkCreatePipelineCache(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkPipelineCacheCreateInfo
    ): Long

    actual external fun vkDestroyPipelineCache(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkPipelineCache") pipelineCache: Long
    )

    @VkReturnType("VkPipelineLayout")
    actual external fun vkCreatePipelineLayout(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkPipelineLayoutCreateInfo
    ): Long

    actual external fun vkDestroyPipelineLayout(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkPipelineLayout") pipelineLayout: Long
    )

    @VkReturnType("VkPipeline")
    actual external fun vkCreateGraphicsPipelines(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkPipelineCache") pipelineCache: Long,
        createInfos: Array<VkGraphicsPipelineCreateInfo>
    ): LongArray

    actual external fun vkDestroyPipeline(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkPipeline") pipeline: Long
    )

    @VkReturnType("VkRenderPass")
    actual external fun vkCreateRenderPass(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkRenderPassCreateInfo
    ): Long

    actual external fun vkDestroyRenderPass(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkRenderPass") renderPass: Long
    )

    @VkReturnType("VkFramebuffer")
    actual external fun vkCreateFramebuffer(
        @VkHandleRef("VkDevice") device: Long,
        framebufferInfo: VkFramebufferCreateInfo
    ): Long

    actual external fun vkDestroyFramebuffer(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkFramebuffer") framebuffer: Long
    )

    @VkReturnType("VkCommandBuffer")
    actual external fun vkAllocateCommandBuffers(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkCommandBufferAllocateInfo
    ): Long

    actual external fun vkBeginCommandBuffer(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        beginInfo: VkCommandBufferBeginInfo
    )

    @VkReturnType("VkCommandPool")
    actual external fun vkCreateCommandPool(
        @VkHandleRef("VkDevice") device: Long,
        createInfo: VkCommandPoolCreateInfo
    ): Long

    actual external fun vkDestroyCommandPool(
        @VkHandleRef("VkDevice") device: Long,
        @VkHandleRef("VkCommandPool") commandPool: Long
    )

    actual external fun vkCmdBindPipeline(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        pipelineBindPoint: VkPipelineBindPoint,
        @VkHandleRef("VkPipeline") graphicsPipeline: Long
    )

    actual external fun vkCmdSetViewport(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        firstViewport: Int,
        viewports: Array<VkViewport>
    )

    actual external fun vkCmdSetScissor(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        firstScissor: Int,
        scissors: Array<VkRect2D>
    )

    actual external fun vkCmdDraw(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        vertexCount: Int,
        instanceCount: Int,
        firstVertex: Int,
        firstInstance: Int
    )

    actual external fun vkCmdEndRenderPass(@VkHandleRef("VkCommandBuffer") commandBuffer: Long)

    actual external fun vkEndCommandBuffer(@VkHandleRef("VkCommandBuffer") commandBuffer: Long)

    @VkReturnType("VkDebugUtilsMessengerEXT")
    @VkSingleton
    actual external fun vkCreateDebugUtilsMessengerEXT(
        @VkHandleRef("VkInstance") instance: Long,
        createInfo: VkDebugUtilsMessengerCreateInfoEXT
    ): Long

    @VkSingleton
    actual external fun vkDestroyDebugUtilsMessengerEXT(
        @VkHandleRef("VkInstance") instance: Long,
        @VkHandleRef("VkDebugUtilsMessengerEXT") debugUtilsMessenger: Long
    )

    actual external fun vkCmdBeginRenderPass(
        @VkHandleRef("VkCommandBuffer") commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents
    )

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
        @VkHandleRef("VkFence") fence: Long
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
