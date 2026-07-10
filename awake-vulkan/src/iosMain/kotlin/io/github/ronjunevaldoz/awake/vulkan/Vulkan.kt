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

import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtensionProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent3D
import io.github.ronjunevaldoz.awake.vulkan.models.VkLayerProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
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
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import cnames.structs.VkCommandBuffer_T
import cnames.structs.VkCommandPool_T
import cnames.structs.VkDescriptorSetLayout_T
import cnames.structs.VkFence_T
import cnames.structs.VkFramebuffer_T
import cnames.structs.VkImageView_T
import cnames.structs.VkImage_T
import cnames.structs.VkInstance_T
import cnames.structs.VkPhysicalDevice_T
import cnames.structs.VkPipelineCache_T
import cnames.structs.VkPipelineLayout_T
import cnames.structs.VkPipeline_T
import cnames.structs.VkRenderPass_T
import cnames.structs.VkSemaphore_T
import cnames.structs.VkShaderModule_T
import cnames.structs.VkSurfaceKHR_T
import cnames.structs.VkSwapchainKHR_T
import platform.MoltenVK.VK_STRUCTURE_TYPE_APPLICATION_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkCommandBufferVar
import platform.MoltenVK.VkCommandPoolVar
import platform.MoltenVK.VkFenceVar
import platform.MoltenVK.VkFramebufferVar
import platform.MoltenVK.VkImageViewVar
import platform.MoltenVK.VkInstanceVar
import platform.MoltenVK.VkPipelineCacheVar
import platform.MoltenVK.VkPipelineLayoutVar
import platform.MoltenVK.VkQueueVar
import platform.MoltenVK.VkSemaphoreVar
import platform.MoltenVK.VkShaderModuleVar
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
import platform.MoltenVK.vkEnumerateDeviceExtensionProperties as nativeVkEnumerateDeviceExtensionProperties
import platform.MoltenVK.vkEnumerateInstanceExtensionProperties as nativeVkEnumerateInstanceExtensionProperties
import platform.MoltenVK.vkEnumerateInstanceLayerProperties as nativeVkEnumerateInstanceLayerProperties
import platform.MoltenVK.vkEnumeratePhysicalDevices as nativeVkEnumeratePhysicalDevices
import platform.MoltenVK.vkGetDeviceQueue as nativeVkGetDeviceQueue
import platform.MoltenVK.vkGetPhysicalDeviceFeatures as nativeVkGetPhysicalDeviceFeatures
import platform.MoltenVK.vkGetPhysicalDeviceQueueFamilyProperties as nativeVkGetPhysicalDeviceQueueFamilyProperties
import platform.MoltenVK.vkGetSwapchainImagesKHR as nativeVkGetSwapchainImagesKHR
import platform.MoltenVK.vkCreateSemaphore as nativeVkCreateSemaphore
import platform.MoltenVK.vkCreateFence as nativeVkCreateFence
import platform.MoltenVK.vkCreateCommandPool as nativeVkCreateCommandPool
import platform.MoltenVK.vkAllocateCommandBuffers as nativeVkAllocateCommandBuffers
import platform.MoltenVK.vkCreateImageView as nativeVkCreateImageView
import platform.MoltenVK.vkCreateShaderModule as nativeVkCreateShaderModule
import platform.MoltenVK.vkCreatePipelineCache as nativeVkCreatePipelineCache
import platform.MoltenVK.vkCreatePipelineLayout as nativeVkCreatePipelineLayout
import platform.MoltenVK.vkCreateFramebuffer as nativeVkCreateFramebuffer
import platform.MoltenVK.vkCmdSetViewport as nativeVkCmdSetViewport
import platform.MoltenVK.vkCmdSetScissor as nativeVkCmdSetScissor
import platform.MoltenVK.vkWaitForFences as nativeVkWaitForFences
import platform.MoltenVK.vkResetFences as nativeVkResetFences
import platform.MoltenVK.vkAcquireNextImageKHR as nativeVkAcquireNextImageKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceCapabilitiesKHR as nativeVkGetPhysicalDeviceSurfaceCapabilitiesKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceFormatsKHR as nativeVkGetPhysicalDeviceSurfaceFormatsKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfacePresentModesKHR as nativeVkGetPhysicalDeviceSurfacePresentModesKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceSupportKHR as nativeVkGetPhysicalDeviceSurfaceSupportKHR
import platform.MoltenVK.vkResetCommandBuffer as nativeVkResetCommandBuffer
import platform.MoltenVK.VkApplicationInfo as NativeVkApplicationInfo
import platform.MoltenVK.VkCommandBufferAllocateInfo as NativeVkCommandBufferAllocateInfo
import platform.MoltenVK.VkCommandBufferBeginInfo as NativeVkCommandBufferBeginInfo
import platform.MoltenVK.VkCommandPoolCreateInfo as NativeVkCommandPoolCreateInfo
import platform.MoltenVK.VkExtensionProperties as NativeVkExtensionProperties
import platform.MoltenVK.VkFenceCreateInfo as NativeVkFenceCreateInfo
import platform.MoltenVK.VkFramebufferCreateInfo as NativeVkFramebufferCreateInfo
import platform.MoltenVK.VkImageViewCreateInfo as NativeVkImageViewCreateInfo
import platform.MoltenVK.VkPipelineLayoutCreateInfo as NativeVkPipelineLayoutCreateInfo
import platform.MoltenVK.VkInstanceCreateInfo as NativeVkInstanceCreateInfo
import platform.MoltenVK.VkLayerProperties as NativeVkLayerProperties
import platform.MoltenVK.VkPhysicalDeviceFeatures as NativeVkPhysicalDeviceFeatures
import platform.MoltenVK.VkPipelineCacheCreateInfo as NativeVkPipelineCacheCreateInfo
import platform.MoltenVK.VkQueueFamilyProperties as NativeVkQueueFamilyProperties
import platform.MoltenVK.VkSemaphoreCreateInfo as NativeVkSemaphoreCreateInfo
import platform.MoltenVK.VkShaderModuleCreateInfo as NativeVkShaderModuleCreateInfo
import platform.MoltenVK.VkViewport as NativeVkViewport
import platform.MoltenVK.VkRect2D as NativeVkRect2D
import platform.MoltenVK.VkSurfaceCapabilitiesKHR as NativeVkSurfaceCapabilitiesKHR
import platform.MoltenVK.VkSurfaceFormatKHR as NativeVkSurfaceFormatKHR

// One field per VkPhysicalDeviceFeatures member (55 VkBool32 flags, no nesting) -- shared by
// vkGetPhysicalDeviceFeatures (native -> Kotlin) below.
@OptIn(ExperimentalForeignApi::class)
private fun NativeVkPhysicalDeviceFeatures.toKotlinModel(): VkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures(
    robustBufferAccess = robustBufferAccess != 0u,
    fullDrawIndexUint32 = fullDrawIndexUint32 != 0u,
    imageCubeArray = imageCubeArray != 0u,
    independentBlend = independentBlend != 0u,
    geometryShader = geometryShader != 0u,
    tessellationShader = tessellationShader != 0u,
    sampleRateShading = sampleRateShading != 0u,
    dualSrcBlend = dualSrcBlend != 0u,
    logicOp = logicOp != 0u,
    multiDrawIndirect = multiDrawIndirect != 0u,
    drawIndirectFirstInstance = drawIndirectFirstInstance != 0u,
    depthClamp = depthClamp != 0u,
    depthBiasClamp = depthBiasClamp != 0u,
    fillModeNonSolid = fillModeNonSolid != 0u,
    depthBounds = depthBounds != 0u,
    wideLines = wideLines != 0u,
    largePoints = largePoints != 0u,
    alphaToOne = alphaToOne != 0u,
    multiViewport = multiViewport != 0u,
    samplerAnisotropy = samplerAnisotropy != 0u,
    textureCompressionETC2 = textureCompressionETC2 != 0u,
    textureCompressionASTC_LDR = textureCompressionASTC_LDR != 0u,
    textureCompressionBC = textureCompressionBC != 0u,
    occlusionQueryPrecise = occlusionQueryPrecise != 0u,
    pipelineStatisticsQuery = pipelineStatisticsQuery != 0u,
    vertexPipelineStoresAndAtomics = vertexPipelineStoresAndAtomics != 0u,
    fragmentStoresAndAtomics = fragmentStoresAndAtomics != 0u,
    shaderTessellationAndGeometryPointSize = shaderTessellationAndGeometryPointSize != 0u,
    shaderImageGatherExtended = shaderImageGatherExtended != 0u,
    shaderStorageImageExtendedFormats = shaderStorageImageExtendedFormats != 0u,
    shaderStorageImageMultisample = shaderStorageImageMultisample != 0u,
    shaderStorageImageReadWithoutFormat = shaderStorageImageReadWithoutFormat != 0u,
    shaderStorageImageWriteWithoutFormat = shaderStorageImageWriteWithoutFormat != 0u,
    shaderUniformBufferArrayDynamicIndexing = shaderUniformBufferArrayDynamicIndexing != 0u,
    shaderSampledImageArrayDynamicIndexing = shaderSampledImageArrayDynamicIndexing != 0u,
    shaderStorageBufferArrayDynamicIndexing = shaderStorageBufferArrayDynamicIndexing != 0u,
    shaderStorageImageArrayDynamicIndexing = shaderStorageImageArrayDynamicIndexing != 0u,
    shaderClipDistance = shaderClipDistance != 0u,
    shaderCullDistance = shaderCullDistance != 0u,
    shaderFloat64 = shaderFloat64 != 0u,
    shaderInt64 = shaderInt64 != 0u,
    shaderInt16 = shaderInt16 != 0u,
    shaderResourceResidency = shaderResourceResidency != 0u,
    shaderResourceMinLod = shaderResourceMinLod != 0u,
    sparseBinding = sparseBinding != 0u,
    sparseResidencyBuffer = sparseResidencyBuffer != 0u,
    sparseResidencyImage2D = sparseResidencyImage2D != 0u,
    sparseResidencyImage3D = sparseResidencyImage3D != 0u,
    sparseResidency2Samples = sparseResidency2Samples != 0u,
    sparseResidency4Samples = sparseResidency4Samples != 0u,
    sparseResidency8Samples = sparseResidency8Samples != 0u,
    sparseResidency16Samples = sparseResidency16Samples != 0u,
    sparseResidencyAliased = sparseResidencyAliased != 0u,
    variableMultisampleRate = variableMultisampleRate != 0u,
    inheritedQueries = inheritedQueries != 0u
)

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

    actual fun vkEnumerateInstanceLayerProperties(): Array<VkLayerProperties> = memScoped {
        val countVar = alloc<UIntVar>()
        nativeVkEnumerateInstanceLayerProperties(countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped emptyArray()
        val nativeArray = allocArray<NativeVkLayerProperties>(count)
        nativeVkEnumerateInstanceLayerProperties(countVar.ptr, nativeArray)
        Array(count) { i ->
            val native = nativeArray[i]
            VkLayerProperties(
                layerName = native.layerName.toKString(),
                specVersion = native.specVersion.toInt(),
                implementationVersion = native.implementationVersion.toInt(),
                description = native.description.toKString()
            )
        }
    }

    actual fun vkEnumerateInstanceExtensionProperties(layerName: String?): Array<VkExtensionProperties> =
        memScoped {
            val countVar = alloc<UIntVar>()
            nativeVkEnumerateInstanceExtensionProperties(layerName, countVar.ptr, null)
            val count = countVar.value.toInt()
            if (count == 0) return@memScoped emptyArray()
            val nativeArray = allocArray<NativeVkExtensionProperties>(count)
            nativeVkEnumerateInstanceExtensionProperties(layerName, countVar.ptr, nativeArray)
            Array(count) { i ->
                val native = nativeArray[i]
                VkExtensionProperties(
                    extensionName = native.extensionName.toKString(),
                    specVersion = native.specVersion.toInt()
                )
            }
        }

    actual fun vkEnumerateDeviceExtensionProperties(
        physicalDevice: Long,
        layerName: String?
    ): Array<VkExtensionProperties> = memScoped {
        val nativePhysicalDevice = physicalDevice.toCPointer<VkPhysicalDevice_T>()
        val countVar = alloc<UIntVar>()
        nativeVkEnumerateDeviceExtensionProperties(nativePhysicalDevice, layerName, countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped emptyArray()
        val nativeArray = allocArray<NativeVkExtensionProperties>(count)
        nativeVkEnumerateDeviceExtensionProperties(nativePhysicalDevice, layerName, countVar.ptr, nativeArray)
        Array(count) { i ->
            val native = nativeArray[i]
            VkExtensionProperties(
                extensionName = native.extensionName.toKString(),
                specVersion = native.specVersion.toInt()
            )
        }
    }

    actual fun vkEnumeratePhysicalDevices(instance: Long): LongArray = memScoped {
        val countVar = alloc<UIntVar>()
        nativeVkEnumeratePhysicalDevices(instance.toCPointer(), countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped LongArray(0)
        val nativeArray = allocArray<CPointerVar<VkPhysicalDevice_T>>(count)
        nativeVkEnumeratePhysicalDevices(instance.toCPointer(), countVar.ptr, nativeArray)
        LongArray(count) { i -> nativeArray[i]!!.rawValue.toLong() }
    }

    actual fun vkGetPhysicalDeviceProperties(physicalDevice: Long): VkPhysicalDeviceProperties {
        TODO("Not yet implemented")
    }

    actual fun vkGetPhysicalDeviceFeatures(physicalDevice: Long): VkPhysicalDeviceFeatures = memScoped {
        val nativeFeatures = alloc<NativeVkPhysicalDeviceFeatures>()
        nativeVkGetPhysicalDeviceFeatures(physicalDevice.toCPointer(), nativeFeatures.ptr)
        nativeFeatures.toKotlinModel()
    }

    actual fun vkGetPhysicalDeviceQueueFamilyProperties(
        physicalDevice: Long
    ): Array<VkQueueFamilyProperties> = memScoped {
        val nativePhysicalDevice = physicalDevice.toCPointer<VkPhysicalDevice_T>()
        val countVar = alloc<UIntVar>()
        nativeVkGetPhysicalDeviceQueueFamilyProperties(nativePhysicalDevice, countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped emptyArray()
        val nativeArray = allocArray<NativeVkQueueFamilyProperties>(count)
        nativeVkGetPhysicalDeviceQueueFamilyProperties(nativePhysicalDevice, countVar.ptr, nativeArray)
        Array(count) { i ->
            val native = nativeArray[i]
            VkQueueFamilyProperties(
                queueFlags = native.queueFlags.toInt(),
                queueCount = native.queueCount,
                timestampValidBits = native.timestampValidBits,
                minImageTransferGranularity = native.minImageTransferGranularity.let {
                    VkExtent3D(width = it.width.toInt(), height = it.height.toInt(), depth = it.depth.toInt())
                }
            )
        }
    }

    actual fun vkGetSwapchainImagesKHR(device: Long, swapchain: Long): LongArray = memScoped {
        val nativeDevice = device.toCPointer<cnames.structs.VkDevice_T>()
        val nativeSwapchain = swapchain.toCPointer<VkSwapchainKHR_T>()
        val countVar = alloc<UIntVar>()
        nativeVkGetSwapchainImagesKHR(nativeDevice, nativeSwapchain, countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped LongArray(0)
        val nativeArray = allocArray<CPointerVar<VkImage_T>>(count)
        nativeVkGetSwapchainImagesKHR(nativeDevice, nativeSwapchain, countVar.ptr, nativeArray)
        LongArray(count) { i -> nativeArray[i]!!.rawValue.toLong() }
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
    ): VkSurfaceCapabilitiesKHR = memScoped {
        val nativeCaps = alloc<NativeVkSurfaceCapabilitiesKHR>()
        nativeVkGetPhysicalDeviceSurfaceCapabilitiesKHR(
            physicalDevice.toCPointer(),
            surface.toCPointer<VkSurfaceKHR_T>(),
            nativeCaps.ptr
        )
        VkSurfaceCapabilitiesKHR(
            minImageCount = nativeCaps.minImageCount.toInt(),
            maxImageCount = nativeCaps.maxImageCount.toInt(),
            currentExtent = VkExtent2D(
                width = nativeCaps.currentExtent.width.toInt(),
                height = nativeCaps.currentExtent.height.toInt()
            ),
            minImageExtent = VkExtent2D(
                width = nativeCaps.minImageExtent.width.toInt(),
                height = nativeCaps.minImageExtent.height.toInt()
            ),
            maxImageExtent = VkExtent2D(
                width = nativeCaps.maxImageExtent.width.toInt(),
                height = nativeCaps.maxImageExtent.height.toInt()
            ),
            maxImageArrayLayers = nativeCaps.maxImageArrayLayers.toInt(),
            supportedTransforms = nativeCaps.supportedTransforms.toInt(),
            currentTransform = VkSurfaceTransformFlagBitsKHR.entries.first {
                it.value.toUInt() == nativeCaps.currentTransform
            },
            supportedCompositeAlpha = nativeCaps.supportedCompositeAlpha.toInt(),
            supportedUsageFlags = nativeCaps.supportedUsageFlags.toInt()
        )
    }

    actual fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkSurfaceFormatKHR> = memScoped {
        val nativePhysicalDevice = physicalDevice.toCPointer<VkPhysicalDevice_T>()
        val nativeSurface = surface.toCPointer<VkSurfaceKHR_T>()
        val countVar = alloc<UIntVar>()
        nativeVkGetPhysicalDeviceSurfaceFormatsKHR(nativePhysicalDevice, nativeSurface, countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped emptyArray()
        val nativeArray = allocArray<NativeVkSurfaceFormatKHR>(count)
        nativeVkGetPhysicalDeviceSurfaceFormatsKHR(nativePhysicalDevice, nativeSurface, countVar.ptr, nativeArray)
        Array(count) { i ->
            val native = nativeArray[i]
            VkSurfaceFormatKHR(
                format = VkFormat.entries.first { it.value.toUInt() == native.format },
                colorSpace = VkColorSpaceKHR.entries.first { it.value.toUInt() == native.colorSpace }
            )
        }
    }

    actual fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        physicalDevice: Long,
        surface: Long
    ): Array<VkPresentModeKHR> = memScoped {
        val nativePhysicalDevice = physicalDevice.toCPointer<VkPhysicalDevice_T>()
        val nativeSurface = surface.toCPointer<VkSurfaceKHR_T>()
        val countVar = alloc<UIntVar>()
        nativeVkGetPhysicalDeviceSurfacePresentModesKHR(nativePhysicalDevice, nativeSurface, countVar.ptr, null)
        val count = countVar.value.toInt()
        if (count == 0) return@memScoped emptyArray()
        val nativeArray = allocArray<UIntVar>(count)
        nativeVkGetPhysicalDeviceSurfacePresentModesKHR(nativePhysicalDevice, nativeSurface, countVar.ptr, nativeArray)
        Array(count) { i -> VkPresentModeKHR.entries.first { it.value.toUInt() == nativeArray[i] } }
    }

    actual fun vkCreateSwapchainKHR(device: Long, createInfoKHR: VkSwapchainCreateInfoKHR): Long {
        TODO("Not yet implemented")
    }

    actual fun vkDestroySwapchainKHR(device: Long, swapchainKHR: Long) {
        nativeVkDestroySwapchainKHR(device.toCPointer(), swapchainKHR.toCPointer<VkSwapchainKHR_T>(), null)
    }

    actual fun vkCreateImageView(device: Long, createInfo: VkImageViewCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkImageViewCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            image = createInfo.image.toCPointer()
            viewType = createInfo.viewType.value.toUInt()
            format = createInfo.format.value.toUInt()
            components.apply {
                r = createInfo.components.r.value.toUInt()
                g = createInfo.components.g.value.toUInt()
                b = createInfo.components.b.value.toUInt()
                a = createInfo.components.a.value.toUInt()
            }
            subresourceRange.apply {
                aspectMask = createInfo.subresourceRange.aspectMask.toUInt()
                baseMipLevel = createInfo.subresourceRange.baseMipLevel.toUInt()
                levelCount = createInfo.subresourceRange.levelCount.toUInt()
                baseArrayLayer = createInfo.subresourceRange.baseArrayLayer.toUInt()
                layerCount = createInfo.subresourceRange.layerCount.toUInt()
            }
        }
        val imageViewVar = alloc<VkImageViewVar>()
        val result = nativeVkCreateImageView(device.toCPointer(), nativeCreateInfo.ptr, null, imageViewVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateImageView failed: $result" }
        imageViewVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyImageView(device: Long, imageView: Long) {
        nativeVkDestroyImageView(device.toCPointer(), imageView.toCPointer<VkImageView_T>(), null)
    }

    actual fun vkCreateShaderModule(device: Long, createInfo: VkShaderModuleCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkShaderModuleCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            codeSize = (createInfo.pCode.size * Int.SIZE_BYTES).toULong()
            pCode = allocArray(createInfo.pCode.size) { index -> value = createInfo.pCode[index].toUInt() }
        }
        val shaderModuleVar = alloc<VkShaderModuleVar>()
        val result = nativeVkCreateShaderModule(device.toCPointer(), nativeCreateInfo.ptr, null, shaderModuleVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateShaderModule failed: $result" }
        shaderModuleVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyShaderModule(device: Long, shaderModule: Long) {
        nativeVkDestroyShaderModule(device.toCPointer(), shaderModule.toCPointer<VkShaderModule_T>(), null)
    }

    actual fun vkCreatePipelineCache(device: Long, createInfo: VkPipelineCacheCreateInfo): Long = memScoped {
        // pInitialData is unused by every call site in this codebase today (always null) --
        // not marshalled here; revisit if a real pipeline-cache-warm-start use case appears.
        check(createInfo.pInitialData == null) { "vkCreatePipelineCache: pInitialData not yet supported on iOS" }
        val nativeCreateInfo = alloc<NativeVkPipelineCacheCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            initialDataSize = 0u
            pInitialData = null
        }
        val pipelineCacheVar = alloc<VkPipelineCacheVar>()
        val result =
            nativeVkCreatePipelineCache(device.toCPointer(), nativeCreateInfo.ptr, null, pipelineCacheVar.ptr)
        check(result == VK_SUCCESS) { "vkCreatePipelineCache failed: $result" }
        pipelineCacheVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyPipelineCache(device: Long, pipelineCache: Long) {
        nativeVkDestroyPipelineCache(device.toCPointer(), pipelineCache.toCPointer<VkPipelineCache_T>(), null)
    }

    actual fun vkCreatePipelineLayout(device: Long, createInfo: VkPipelineLayoutCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkPipelineLayoutCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            val setLayouts = createInfo.pSetLayouts
            setLayoutCount = (setLayouts?.size ?: 0).toUInt()
            pSetLayouts = setLayouts?.let { layouts ->
                allocArray<CPointerVar<VkDescriptorSetLayout_T>>(layouts.size) { index ->
                    value = layouts[index].toCPointer()
                }
            }
            val pushConstantRanges = createInfo.pPushConstantRanges
            pushConstantRangeCount = (pushConstantRanges?.size ?: 0).toUInt()
            pPushConstantRanges = pushConstantRanges?.let { ranges ->
                allocArray(ranges.size) { index ->
                    stageFlags = ranges[index].stageFlags.toUInt()
                    offset = ranges[index].offset.toUInt()
                    size = ranges[index].size.toUInt()
                }
            }
        }
        val pipelineLayoutVar = alloc<VkPipelineLayoutVar>()
        val result =
            nativeVkCreatePipelineLayout(device.toCPointer(), nativeCreateInfo.ptr, null, pipelineLayoutVar.ptr)
        check(result == VK_SUCCESS) { "vkCreatePipelineLayout failed: $result" }
        pipelineLayoutVar.value!!.rawValue.toLong()
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

    actual fun vkCreateFramebuffer(device: Long, framebufferInfo: VkFramebufferCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkFramebufferCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO
            pNext = null
            flags = framebufferInfo.flags.toUInt()
            renderPass = framebufferInfo.renderPass.toCPointer()
            attachmentCount = framebufferInfo.pAttachments.size.toUInt()
            pAttachments = allocArray<CPointerVar<VkImageView_T>>(framebufferInfo.pAttachments.size) { index ->
                value = framebufferInfo.pAttachments[index].toCPointer()
            }
            width = framebufferInfo.width.toUInt()
            height = framebufferInfo.height.toUInt()
            layers = framebufferInfo.layers.toUInt()
        }
        val framebufferVar = alloc<VkFramebufferVar>()
        val result = nativeVkCreateFramebuffer(device.toCPointer(), nativeCreateInfo.ptr, null, framebufferVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateFramebuffer failed: $result" }
        framebufferVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyFramebuffer(device: Long, framebuffer: Long) {
        nativeVkDestroyFramebuffer(device.toCPointer(), framebuffer.toCPointer<VkFramebuffer_T>(), null)
    }

    actual fun vkAllocateCommandBuffers(device: Long, createInfo: VkCommandBufferAllocateInfo): Long = memScoped {
        // Return type is a single Long -- matches this codebase's Android/desktop actuals,
        // which only ever allocate one primary command buffer at a time (commandBufferCount
        // is expected to be 1 here, not a real batch-allocate).
        val nativeCreateInfo = alloc<NativeVkCommandBufferAllocateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO
            pNext = null
            commandPool = createInfo.commandPool.toCPointer()
            level = createInfo.level.value.toUInt()
            commandBufferCount = createInfo.commandBufferCount.toUInt()
        }
        val commandBufferVar = alloc<VkCommandBufferVar>()
        val result = nativeVkAllocateCommandBuffers(device.toCPointer(), nativeCreateInfo.ptr, commandBufferVar.ptr)
        check(result == VK_SUCCESS) { "vkAllocateCommandBuffers failed: $result" }
        commandBufferVar.value!!.rawValue.toLong()
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

    actual fun vkCreateCommandPool(device: Long, createInfo: VkCommandPoolCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkCommandPoolCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            queueFamilyIndex = createInfo.queueFamilyIndex.toUInt()
        }
        val commandPoolVar = alloc<VkCommandPoolVar>()
        val result = nativeVkCreateCommandPool(device.toCPointer(), nativeCreateInfo.ptr, null, commandPoolVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateCommandPool failed: $result" }
        commandPoolVar.value!!.rawValue.toLong()
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

    actual fun vkCmdSetViewport(commandBuffer: Long, firstViewport: Int, viewports: Array<VkViewport>) = memScoped {
        val nativeViewports = allocArray<NativeVkViewport>(viewports.size) { index ->
            x = viewports[index].x
            y = viewports[index].y
            width = viewports[index].width
            height = viewports[index].height
            minDepth = viewports[index].minDepth
            maxDepth = viewports[index].maxDepth
        }
        nativeVkCmdSetViewport(commandBuffer.toCPointer(), firstViewport.toUInt(), viewports.size.toUInt(), nativeViewports)
        Unit
    }

    actual fun vkCmdSetScissor(commandBuffer: Long, firstScissor: Int, scissors: Array<VkRect2D>) = memScoped {
        val nativeScissors = allocArray<NativeVkRect2D>(scissors.size) { index ->
            offset.apply {
                x = scissors[index].offset.x
                y = scissors[index].offset.y
            }
            extent.apply {
                width = scissors[index].extent.width.toUInt()
                height = scissors[index].extent.height.toUInt()
            }
        }
        nativeVkCmdSetScissor(commandBuffer.toCPointer(), firstScissor.toUInt(), scissors.size.toUInt(), nativeScissors)
        Unit
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

    actual fun vkCreateSemaphore(device: Long, createInfo: VkSemaphoreCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkSemaphoreCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
        }
        val semaphoreVar = alloc<VkSemaphoreVar>()
        val result = nativeVkCreateSemaphore(device.toCPointer(), nativeCreateInfo.ptr, null, semaphoreVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateSemaphore failed: $result" }
        semaphoreVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroySemaphore(device: Long, semaphore: Long) {
        nativeVkDestroySemaphore(device.toCPointer(), semaphore.toCPointer<VkSemaphore_T>(), null)
    }

    actual fun vkCreateFence(device: Long, createInfo: VkFenceCreateInfo): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkFenceCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
        }
        val fenceVar = alloc<VkFenceVar>()
        val result = nativeVkCreateFence(device.toCPointer(), nativeCreateInfo.ptr, null, fenceVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateFence failed: $result" }
        fenceVar.value!!.rawValue.toLong()
    }

    actual fun vkDestroyFence(device: Long, fence: Long) {
        nativeVkDestroyFence(device.toCPointer(), fence.toCPointer<VkFence_T>(), null)
    }

    actual fun vkWaitForFences(device: Long, fences: LongArray, waitAll: Boolean, timeout: Long) = memScoped {
        val nativeFences = allocArray<CPointerVar<VkFence_T>>(fences.size) { index ->
            value = fences[index].toCPointer()
        }
        nativeVkWaitForFences(
            device.toCPointer(),
            fences.size.toUInt(),
            nativeFences,
            if (waitAll) 1u else 0u,
            timeout.toULong()
        )
        Unit
    }

    actual fun vkResetFences(device: Long, fences: LongArray) = memScoped {
        val nativeFences = allocArray<CPointerVar<VkFence_T>>(fences.size) { index ->
            value = fences[index].toCPointer()
        }
        nativeVkResetFences(device.toCPointer(), fences.size.toUInt(), nativeFences)
        Unit
    }

    actual fun vkAcquireNextImageKHR(
        device: Long,
        swapchain: Long,
        timeout: Long,
        semaphore: Long,
        fence: Long
    ): Int = memScoped {
        val imageIndexVar = alloc<UIntVar>()
        nativeVkAcquireNextImageKHR(
            device.toCPointer(),
            swapchain.toCPointer<VkSwapchainKHR_T>(),
            timeout.toULong(),
            semaphore.toCPointer<VkSemaphore_T>(),
            fence.toCPointer<VkFence_T>(),
            imageIndexVar.ptr
        )
        imageIndexVar.value.toInt()
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
