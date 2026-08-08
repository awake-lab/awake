// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan

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
import cnames.structs.VkQueue_T
import cnames.structs.VkRenderPass_T
import cnames.structs.VkSemaphore_T
import cnames.structs.VkShaderModule_T
import cnames.structs.VkSurfaceKHR_T
import cnames.structs.VkSwapchainKHR_T
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorSpaceKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPhysicalDeviceType
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPresentModeKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSurfaceTransformFlagBitsKHR
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkDebugUtilsMessageSeverityFlagBitsEXT
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearColorValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkClearDepthStencilValue
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtensionProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent3D
import io.github.ronjunevaldoz.awake.vulkan.models.VkLayerProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkQueueFamilyProperties
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkStencilOpState
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
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.PFN_vkDebugUtilsMessengerCallbackEXT
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCallbackDataEXT
import io.github.ronjunevaldoz.awake.vulkan.models.info.debug.VkDebugUtilsMessengerCreateInfoEXT
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceFeatures
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceLimits
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceProperties
import io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice.VkPhysicalDeviceSparseProperties
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.MoltenVK.VK_STRUCTURE_TYPE_APPLICATION_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT
import platform.MoltenVK.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR
import platform.MoltenVK.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SUBMIT_INFO
import platform.MoltenVK.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR
import platform.MoltenVK.VK_SUCCESS
import platform.MoltenVK.VkCommandBufferVar
import platform.MoltenVK.VkCommandPoolVar
import platform.MoltenVK.VkDebugUtilsMessengerEXTVar
import platform.MoltenVK.VkDeviceVar
import platform.MoltenVK.VkFenceVar
import platform.MoltenVK.VkFramebufferVar
import platform.MoltenVK.VkImageViewVar
import platform.MoltenVK.VkInstanceVar
import platform.MoltenVK.VkPipelineCacheVar
import platform.MoltenVK.VkPipelineLayoutVar
import platform.MoltenVK.VkQueueVar
import platform.MoltenVK.VkRenderPassVar
import platform.MoltenVK.VkSemaphoreVar
import platform.MoltenVK.VkShaderModuleVar
import platform.MoltenVK.VkSwapchainKHRVar
import platform.MoltenVK.VkApplicationInfo as NativeVkApplicationInfo
import platform.MoltenVK.VkAttachmentDescription as NativeVkAttachmentDescription
import platform.MoltenVK.VkAttachmentReference as NativeVkAttachmentReference
import platform.MoltenVK.VkClearValue as NativeVkClearValue
import platform.MoltenVK.VkCommandBufferAllocateInfo as NativeVkCommandBufferAllocateInfo
import platform.MoltenVK.VkCommandBufferBeginInfo as NativeVkCommandBufferBeginInfo
import platform.MoltenVK.VkCommandPoolCreateInfo as NativeVkCommandPoolCreateInfo
import platform.MoltenVK.VkDebugUtilsMessengerCallbackDataEXT as NativeVkDebugUtilsMessengerCallbackDataEXT
import platform.MoltenVK.VkDebugUtilsMessengerCreateInfoEXT as NativeVkDebugUtilsMessengerCreateInfoEXT
import platform.MoltenVK.VkDeviceCreateInfo as NativeVkDeviceCreateInfo
import platform.MoltenVK.VkDeviceQueueCreateInfo as NativeVkDeviceQueueCreateInfo
import platform.MoltenVK.VkExtensionProperties as NativeVkExtensionProperties
import platform.MoltenVK.VkFenceCreateInfo as NativeVkFenceCreateInfo
import platform.MoltenVK.VkFramebufferCreateInfo as NativeVkFramebufferCreateInfo
import platform.MoltenVK.VkGraphicsPipelineCreateInfo as NativeVkGraphicsPipelineCreateInfo
import platform.MoltenVK.VkImageViewCreateInfo as NativeVkImageViewCreateInfo
import platform.MoltenVK.VkInstanceCreateInfo as NativeVkInstanceCreateInfo
import platform.MoltenVK.VkLayerProperties as NativeVkLayerProperties
import platform.MoltenVK.VkPhysicalDeviceFeatures as NativeVkPhysicalDeviceFeatures
import platform.MoltenVK.VkPhysicalDeviceProperties as NativeVkPhysicalDeviceProperties
import platform.MoltenVK.VkPipelineCacheCreateInfo as NativeVkPipelineCacheCreateInfo
import platform.MoltenVK.VkPipelineColorBlendAttachmentState as NativeVkPipelineColorBlendAttachmentState
import platform.MoltenVK.VkPipelineColorBlendStateCreateInfo as NativeVkPipelineColorBlendStateCreateInfo
import platform.MoltenVK.VkPipelineDepthStencilStateCreateInfo as NativeVkPipelineDepthStencilStateCreateInfo
import platform.MoltenVK.VkPipelineDynamicStateCreateInfo as NativeVkPipelineDynamicStateCreateInfo
import platform.MoltenVK.VkPipelineInputAssemblyStateCreateInfo as NativeVkPipelineInputAssemblyStateCreateInfo
import platform.MoltenVK.VkPipelineLayoutCreateInfo as NativeVkPipelineLayoutCreateInfo
import platform.MoltenVK.VkPipelineMultisampleStateCreateInfo as NativeVkPipelineMultisampleStateCreateInfo
import platform.MoltenVK.VkPipelineRasterizationStateCreateInfo as NativeVkPipelineRasterizationStateCreateInfo
import platform.MoltenVK.VkPipelineShaderStageCreateInfo as NativeVkPipelineShaderStageCreateInfo
import platform.MoltenVK.VkPipelineTessellationStateCreateInfo as NativeVkPipelineTessellationStateCreateInfo
import platform.MoltenVK.VkPipelineVertexInputStateCreateInfo as NativeVkPipelineVertexInputStateCreateInfo
import platform.MoltenVK.VkPipelineViewportStateCreateInfo as NativeVkPipelineViewportStateCreateInfo
import platform.MoltenVK.VkPresentInfoKHR as NativeVkPresentInfoKHR
import platform.MoltenVK.VkQueueFamilyProperties as NativeVkQueueFamilyProperties
import platform.MoltenVK.VkRect2D as NativeVkRect2D
import platform.MoltenVK.VkRenderPassBeginInfo as NativeVkRenderPassBeginInfo
import platform.MoltenVK.VkRenderPassCreateInfo as NativeVkRenderPassCreateInfo
import platform.MoltenVK.VkSemaphoreCreateInfo as NativeVkSemaphoreCreateInfo
import platform.MoltenVK.VkShaderModuleCreateInfo as NativeVkShaderModuleCreateInfo
import platform.MoltenVK.VkStencilOpState as NativeVkStencilOpState
import platform.MoltenVK.VkSubmitInfo as NativeVkSubmitInfo
import platform.MoltenVK.VkSubpassDependency as NativeVkSubpassDependency
import platform.MoltenVK.VkSubpassDescription as NativeVkSubpassDescription
import platform.MoltenVK.VkSurfaceCapabilitiesKHR as NativeVkSurfaceCapabilitiesKHR
import platform.MoltenVK.VkSurfaceFormatKHR as NativeVkSurfaceFormatKHR
import platform.MoltenVK.VkSwapchainCreateInfoKHR as NativeVkSwapchainCreateInfoKHR
import platform.MoltenVK.VkVertexInputAttributeDescription as NativeVkVertexInputAttributeDescription
import platform.MoltenVK.VkVertexInputBindingDescription as NativeVkVertexInputBindingDescription
import platform.MoltenVK.VkViewport as NativeVkViewport
import platform.MoltenVK.vkAcquireNextImageKHR as nativeVkAcquireNextImageKHR
import platform.MoltenVK.vkAllocateCommandBuffers as nativeVkAllocateCommandBuffers
import platform.MoltenVK.vkBeginCommandBuffer as nativeVkBeginCommandBuffer
import platform.MoltenVK.vkCmdBeginRenderPass as nativeVkCmdBeginRenderPass
import platform.MoltenVK.vkCmdBindPipeline as nativeVkCmdBindPipeline
import platform.MoltenVK.vkCmdDraw as nativeVkCmdDraw
import platform.MoltenVK.vkCmdEndRenderPass as nativeVkCmdEndRenderPass
import platform.MoltenVK.vkCmdSetScissor as nativeVkCmdSetScissor
import platform.MoltenVK.vkCmdSetViewport as nativeVkCmdSetViewport
import platform.MoltenVK.vkCreateCommandPool as nativeVkCreateCommandPool
import platform.MoltenVK.vkCreateDebugUtilsMessengerEXT as nativeVkCreateDebugUtilsMessengerEXT
import platform.MoltenVK.vkCreateDevice as nativeVkCreateDevice
import platform.MoltenVK.vkCreateFence as nativeVkCreateFence
import platform.MoltenVK.vkCreateFramebuffer as nativeVkCreateFramebuffer
import platform.MoltenVK.vkCreateGraphicsPipelines as nativeVkCreateGraphicsPipelines
import platform.MoltenVK.vkCreateImageView as nativeVkCreateImageView
import platform.MoltenVK.vkCreateInstance as nativeVkCreateInstance
import platform.MoltenVK.vkCreatePipelineCache as nativeVkCreatePipelineCache
import platform.MoltenVK.vkCreatePipelineLayout as nativeVkCreatePipelineLayout
import platform.MoltenVK.vkCreateRenderPass as nativeVkCreateRenderPass
import platform.MoltenVK.vkCreateSemaphore as nativeVkCreateSemaphore
import platform.MoltenVK.vkCreateShaderModule as nativeVkCreateShaderModule
import platform.MoltenVK.vkCreateSwapchainKHR as nativeVkCreateSwapchainKHR
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
import platform.MoltenVK.vkGetPhysicalDeviceProperties as nativeVkGetPhysicalDeviceProperties
import platform.MoltenVK.vkGetPhysicalDeviceQueueFamilyProperties as nativeVkGetPhysicalDeviceQueueFamilyProperties
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceCapabilitiesKHR as nativeVkGetPhysicalDeviceSurfaceCapabilitiesKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceFormatsKHR as nativeVkGetPhysicalDeviceSurfaceFormatsKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfacePresentModesKHR as nativeVkGetPhysicalDeviceSurfacePresentModesKHR
import platform.MoltenVK.vkGetPhysicalDeviceSurfaceSupportKHR as nativeVkGetPhysicalDeviceSurfaceSupportKHR
import platform.MoltenVK.vkGetSwapchainImagesKHR as nativeVkGetSwapchainImagesKHR
import platform.MoltenVK.vkQueuePresentKHR as nativeVkQueuePresentKHR
import platform.MoltenVK.vkQueueSubmit as nativeVkQueueSubmit
import platform.MoltenVK.vkResetCommandBuffer as nativeVkResetCommandBuffer
import platform.MoltenVK.vkResetFences as nativeVkResetFences
import platform.MoltenVK.vkWaitForFences as nativeVkWaitForFences

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
    inheritedQueries = inheritedQueries != 0u,
)

// Reverse of NativeVkPhysicalDeviceFeatures.toKotlinModel() above -- used by vkCreateDevice's
// pEnabledFeatures marshalling.
@OptIn(ExperimentalForeignApi::class)
private fun NativeVkPhysicalDeviceFeatures.fromKotlinModel(model: VkPhysicalDeviceFeatures) {
    robustBufferAccess = if (model.robustBufferAccess) 1u else 0u
    fullDrawIndexUint32 = if (model.fullDrawIndexUint32) 1u else 0u
    imageCubeArray = if (model.imageCubeArray) 1u else 0u
    independentBlend = if (model.independentBlend) 1u else 0u
    geometryShader = if (model.geometryShader) 1u else 0u
    tessellationShader = if (model.tessellationShader) 1u else 0u
    sampleRateShading = if (model.sampleRateShading) 1u else 0u
    dualSrcBlend = if (model.dualSrcBlend) 1u else 0u
    logicOp = if (model.logicOp) 1u else 0u
    multiDrawIndirect = if (model.multiDrawIndirect) 1u else 0u
    drawIndirectFirstInstance = if (model.drawIndirectFirstInstance) 1u else 0u
    depthClamp = if (model.depthClamp) 1u else 0u
    depthBiasClamp = if (model.depthBiasClamp) 1u else 0u
    fillModeNonSolid = if (model.fillModeNonSolid) 1u else 0u
    depthBounds = if (model.depthBounds) 1u else 0u
    wideLines = if (model.wideLines) 1u else 0u
    largePoints = if (model.largePoints) 1u else 0u
    alphaToOne = if (model.alphaToOne) 1u else 0u
    multiViewport = if (model.multiViewport) 1u else 0u
    samplerAnisotropy = if (model.samplerAnisotropy) 1u else 0u
    textureCompressionETC2 = if (model.textureCompressionETC2) 1u else 0u
    textureCompressionASTC_LDR = if (model.textureCompressionASTC_LDR) 1u else 0u
    textureCompressionBC = if (model.textureCompressionBC) 1u else 0u
    occlusionQueryPrecise = if (model.occlusionQueryPrecise) 1u else 0u
    pipelineStatisticsQuery = if (model.pipelineStatisticsQuery) 1u else 0u
    vertexPipelineStoresAndAtomics = if (model.vertexPipelineStoresAndAtomics) 1u else 0u
    fragmentStoresAndAtomics = if (model.fragmentStoresAndAtomics) 1u else 0u
    shaderTessellationAndGeometryPointSize = if (model.shaderTessellationAndGeometryPointSize) 1u else 0u
    shaderImageGatherExtended = if (model.shaderImageGatherExtended) 1u else 0u
    shaderStorageImageExtendedFormats = if (model.shaderStorageImageExtendedFormats) 1u else 0u
    shaderStorageImageMultisample = if (model.shaderStorageImageMultisample) 1u else 0u
    shaderStorageImageReadWithoutFormat = if (model.shaderStorageImageReadWithoutFormat) 1u else 0u
    shaderStorageImageWriteWithoutFormat = if (model.shaderStorageImageWriteWithoutFormat) 1u else 0u
    shaderUniformBufferArrayDynamicIndexing = if (model.shaderUniformBufferArrayDynamicIndexing) 1u else 0u
    shaderSampledImageArrayDynamicIndexing = if (model.shaderSampledImageArrayDynamicIndexing) 1u else 0u
    shaderStorageBufferArrayDynamicIndexing = if (model.shaderStorageBufferArrayDynamicIndexing) 1u else 0u
    shaderStorageImageArrayDynamicIndexing = if (model.shaderStorageImageArrayDynamicIndexing) 1u else 0u
    shaderClipDistance = if (model.shaderClipDistance) 1u else 0u
    shaderCullDistance = if (model.shaderCullDistance) 1u else 0u
    shaderFloat64 = if (model.shaderFloat64) 1u else 0u
    shaderInt64 = if (model.shaderInt64) 1u else 0u
    shaderInt16 = if (model.shaderInt16) 1u else 0u
    shaderResourceResidency = if (model.shaderResourceResidency) 1u else 0u
    shaderResourceMinLod = if (model.shaderResourceMinLod) 1u else 0u
    sparseBinding = if (model.sparseBinding) 1u else 0u
    sparseResidencyBuffer = if (model.sparseResidencyBuffer) 1u else 0u
    sparseResidencyImage2D = if (model.sparseResidencyImage2D) 1u else 0u
    sparseResidencyImage3D = if (model.sparseResidencyImage3D) 1u else 0u
    sparseResidency2Samples = if (model.sparseResidency2Samples) 1u else 0u
    sparseResidency4Samples = if (model.sparseResidency4Samples) 1u else 0u
    sparseResidency8Samples = if (model.sparseResidency8Samples) 1u else 0u
    sparseResidency16Samples = if (model.sparseResidency16Samples) 1u else 0u
    sparseResidencyAliased = if (model.sparseResidencyAliased) 1u else 0u
    variableMultisampleRate = if (model.variableMultisampleRate) 1u else 0u
    inheritedQueries = if (model.inheritedQueries) 1u else 0u
}

// One field per VkPhysicalDeviceLimits member (~100 fields, no further nesting beyond a few
// fixed-size 2/3-element arrays) -- shared by vkGetPhysicalDeviceProperties below.
@OptIn(ExperimentalForeignApi::class)
private fun platform.MoltenVK.VkPhysicalDeviceLimits.toKotlinModel(): VkPhysicalDeviceLimits = VkPhysicalDeviceLimits(
    maxImageDimension1D = maxImageDimension1D,
    maxImageDimension2D = maxImageDimension2D,
    maxImageDimension3D = maxImageDimension3D,
    maxImageDimensionCube = maxImageDimensionCube,
    maxImageArrayLayers = maxImageArrayLayers,
    maxTexelBufferElements = maxTexelBufferElements,
    maxUniformBufferRange = maxUniformBufferRange,
    maxStorageBufferRange = maxStorageBufferRange,
    maxPushConstantsSize = maxPushConstantsSize,
    maxMemoryAllocationCount = maxMemoryAllocationCount,
    maxSamplerAllocationCount = maxSamplerAllocationCount,
    bufferImageGranularity = bufferImageGranularity.toLong(),
    sparseAddressSpaceSize = sparseAddressSpaceSize.toLong(),
    maxBoundDescriptorSets = maxBoundDescriptorSets,
    maxPerStageDescriptorSamplers = maxPerStageDescriptorSamplers,
    maxPerStageDescriptorUniformBuffers = maxPerStageDescriptorUniformBuffers,
    maxPerStageDescriptorStorageBuffers = maxPerStageDescriptorStorageBuffers,
    maxPerStageDescriptorSampledImages = maxPerStageDescriptorSampledImages,
    maxPerStageDescriptorStorageImages = maxPerStageDescriptorStorageImages,
    maxPerStageDescriptorInputAttachments = maxPerStageDescriptorInputAttachments,
    maxPerStageResources = maxPerStageResources,
    maxDescriptorSetSamplers = maxDescriptorSetSamplers,
    maxDescriptorSetUniformBuffers = maxDescriptorSetUniformBuffers,
    maxDescriptorSetUniformBuffersDynamic = maxDescriptorSetUniformBuffersDynamic,
    maxDescriptorSetStorageBuffers = maxDescriptorSetStorageBuffers,
    maxDescriptorSetStorageBuffersDynamic = maxDescriptorSetStorageBuffersDynamic,
    maxDescriptorSetSampledImages = maxDescriptorSetSampledImages,
    maxDescriptorSetStorageImages = maxDescriptorSetStorageImages,
    maxDescriptorSetInputAttachments = maxDescriptorSetInputAttachments,
    maxVertexInputAttributes = maxVertexInputAttributes,
    maxVertexInputBindings = maxVertexInputBindings,
    maxVertexInputAttributeOffset = maxVertexInputAttributeOffset,
    maxVertexInputBindingStride = maxVertexInputBindingStride,
    maxVertexOutputComponents = maxVertexOutputComponents,
    maxTessellationGenerationLevel = maxTessellationGenerationLevel,
    maxTessellationPatchSize = maxTessellationPatchSize,
    maxTessellationControlPerVertexInputComponents = maxTessellationControlPerVertexInputComponents,
    maxTessellationControlPerVertexOutputComponents = maxTessellationControlPerVertexOutputComponents,
    maxTessellationControlPerPatchOutputComponents = maxTessellationControlPerPatchOutputComponents,
    maxTessellationControlTotalOutputComponents = maxTessellationControlTotalOutputComponents,
    maxTessellationEvaluationInputComponents = maxTessellationEvaluationInputComponents,
    maxTessellationEvaluationOutputComponents = maxTessellationEvaluationOutputComponents,
    maxGeometryShaderInvocations = maxGeometryShaderInvocations,
    maxGeometryInputComponents = maxGeometryInputComponents,
    maxGeometryOutputComponents = maxGeometryOutputComponents,
    maxGeometryOutputVertices = maxGeometryOutputVertices,
    maxGeometryTotalOutputComponents = maxGeometryTotalOutputComponents,
    maxFragmentInputComponents = maxFragmentInputComponents,
    maxFragmentOutputAttachments = maxFragmentOutputAttachments,
    maxFragmentDualSrcAttachments = maxFragmentDualSrcAttachments,
    maxFragmentCombinedOutputResources = maxFragmentCombinedOutputResources,
    maxComputeSharedMemorySize = maxComputeSharedMemorySize,
    maxComputeWorkGroupCount = IntArray(3) { i -> maxComputeWorkGroupCount[i].toInt() },
    maxComputeWorkGroupInvocations = maxComputeWorkGroupInvocations,
    maxComputeWorkGroupSize = IntArray(3) { i -> maxComputeWorkGroupSize[i].toInt() },
    subPixelPrecisionBits = subPixelPrecisionBits,
    subTexelPrecisionBits = subTexelPrecisionBits,
    mipmapPrecisionBits = mipmapPrecisionBits,
    maxDrawIndexedIndexValue = maxDrawIndexedIndexValue,
    maxDrawIndirectCount = maxDrawIndirectCount,
    maxSamplerLodBias = maxSamplerLodBias,
    maxSamplerAnisotropy = maxSamplerAnisotropy,
    maxViewports = maxViewports,
    maxViewportDimensions = IntArray(2) { i -> maxViewportDimensions[i].toInt() },
    viewportBoundsRange = FloatArray(2) { i -> viewportBoundsRange[i] },
    viewportSubPixelBits = viewportSubPixelBits,
    minMemoryMapAlignment = minMemoryMapAlignment,
    minTexelBufferOffsetAlignment = minTexelBufferOffsetAlignment.toLong(),
    minUniformBufferOffsetAlignment = minUniformBufferOffsetAlignment.toLong(),
    minStorageBufferOffsetAlignment = minStorageBufferOffsetAlignment.toLong(),
    minTexelOffset = minTexelOffset,
    maxTexelOffset = maxTexelOffset,
    minTexelGatherOffset = minTexelGatherOffset,
    maxTexelGatherOffset = maxTexelGatherOffset,
    minInterpolationOffset = minInterpolationOffset,
    maxInterpolationOffset = maxInterpolationOffset,
    subPixelInterpolationOffsetBits = subPixelInterpolationOffsetBits,
    maxFramebufferWidth = maxFramebufferWidth,
    maxFramebufferHeight = maxFramebufferHeight,
    maxFramebufferLayers = maxFramebufferLayers,
    framebufferColorSampleCounts = framebufferColorSampleCounts.toInt(),
    framebufferDepthSampleCounts = framebufferDepthSampleCounts.toInt(),
    framebufferStencilSampleCounts = framebufferStencilSampleCounts.toInt(),
    framebufferNoAttachmentsSampleCounts = framebufferNoAttachmentsSampleCounts.toInt(),
    maxColorAttachments = maxColorAttachments,
    sampledImageColorSampleCounts = sampledImageColorSampleCounts.toInt(),
    sampledImageIntegerSampleCounts = sampledImageIntegerSampleCounts.toInt(),
    sampledImageDepthSampleCounts = sampledImageDepthSampleCounts.toInt(),
    sampledImageStencilSampleCounts = sampledImageStencilSampleCounts.toInt(),
    storageImageSampleCounts = storageImageSampleCounts.toInt(),
    maxSampleMaskWords = maxSampleMaskWords,
    timestampComputeAndGraphics = timestampComputeAndGraphics != 0u,
    timestampPeriod = timestampPeriod,
    maxClipDistances = maxClipDistances,
    maxCullDistances = maxCullDistances,
    maxCombinedClipAndCullDistances = maxCombinedClipAndCullDistances,
    discreteQueuePriorities = discreteQueuePriorities,
    pointSizeRange = FloatArray(2) { i -> pointSizeRange[i] },
    lineWidthRange = FloatArray(2) { i -> lineWidthRange[i] },
    pointSizeGranularity = pointSizeGranularity,
    lineWidthGranularity = lineWidthGranularity,
    strictLines = strictLines != 0u,
    standardSampleLocations = standardSampleLocations != 0u,
    optimalBufferCopyOffsetAlignment = optimalBufferCopyOffsetAlignment.toLong(),
    optimalBufferCopyRowPitchAlignment = optimalBufferCopyRowPitchAlignment.toLong(),
    nonCoherentAtomSize = nonCoherentAtomSize.toLong(),
)

// Used by vkCreateGraphicsPipelines for VkPipelineDepthStencilStateCreateInfo's front/back.
@OptIn(ExperimentalForeignApi::class)
private fun NativeVkStencilOpState.fromKotlinModel(model: VkStencilOpState) {
    failOp = model.failOp.value.toUInt()
    passOp = model.passOp.value.toUInt()
    depthFailOp = model.depthFailOp.value.toUInt()
    compareOp = model.compareOp.value.toUInt()
    compareMask = model.compareMask.toUInt()
    writeMask = model.writeMask.toUInt()
    reference = model.reference.toUInt()
}

@OptIn(ExperimentalForeignApi::class)
private fun platform.MoltenVK.VkPhysicalDeviceSparseProperties.toKotlinModel(): VkPhysicalDeviceSparseProperties =
    VkPhysicalDeviceSparseProperties(
        residencyStandard2DBlockShape = residencyStandard2DBlockShape != 0u,
        residencyStandard2DMultisampleBlockShape = residencyStandard2DMultisampleBlockShape != 0u,
        residencyStandard3DBlockShape = residencyStandard3DBlockShape != 0u,
        residencyAlignedMipSize = residencyAlignedMipSize != 0u,
        residencyNonResidentStrict = residencyNonResidentStrict != 0u,
    )

// Shared by vkCreateRenderPass's pInputAttachments/pColorAttachments/pResolveAttachments.
@OptIn(ExperimentalForeignApi::class)
private fun Array<VkAttachmentReference>.toNativeAttachmentRefArray(
    scope: NativePlacement,
): CPointer<NativeVkAttachmentReference> = scope.allocArray(size) { index ->
    attachment = this@toNativeAttachmentRefArray[index].attachment.toUInt()
    layout = this@toNativeAttachmentRefArray[index].layout.value.toUInt()
}

// staticCFunction can't capture state, so the Kotlin callback travels through pUserData as a
// StableRef; this map lets vkDestroyDebugUtilsMessengerEXT dispose that StableRef by handle.
@OptIn(ExperimentalForeignApi::class)
private val debugMessengerCallbacks = mutableMapOf<Long, StableRef<PFN_vkDebugUtilsMessengerCallbackEXT>>()

@OptIn(ExperimentalForeignApi::class)
private val debugMessengerTrampoline = staticCFunction {
        severity: UInt,
        types: UInt,
        pCallbackData: CPointer<NativeVkDebugUtilsMessengerCallbackDataEXT>?,
        pUserData: COpaquePointer?,
    ->
    val callback = pUserData!!.asStableRef<PFN_vkDebugUtilsMessengerCallbackEXT>().get()
    val native = pCallbackData!!.pointed
    val kotlinCallbackData = VkDebugUtilsMessengerCallbackDataEXT(
        pMessageIdName = native.pMessageIdName?.toKString(),
        messageIdNumber = native.messageIdNumber,
        pMessage = native.pMessage?.toKString() ?: "",
    )
    val severityBits = VkDebugUtilsMessageSeverityFlagBitsEXT.entries.first { it.value.toUInt() == severity }
    val handled = callback(severityBits, types.toInt(), kotlinCallbackData, null)
    if (handled) 1u else 0u
}

// Phase 6 (MoltenVK cinterop) is in progress -- see docs/MVP_PLAN.md. Non-TODO() functions link
// against the vendored MoltenVK.xcframework but are compiled-only, not yet hardware-verified.
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
                description = native.description.toKString(),
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
                    specVersion = native.specVersion.toInt(),
                )
            }
        }

    actual fun vkEnumerateDeviceExtensionProperties(
        physicalDevice: Long,
        layerName: String?,
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
                specVersion = native.specVersion.toInt(),
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

    actual fun vkGetPhysicalDeviceProperties(physicalDevice: Long): VkPhysicalDeviceProperties = memScoped {
        val native = alloc<NativeVkPhysicalDeviceProperties>()
        nativeVkGetPhysicalDeviceProperties(physicalDevice.toCPointer(), native.ptr)
        VkPhysicalDeviceProperties(
            apiVersion = native.apiVersion.toInt(),
            driverVersion = native.driverVersion.toInt(),
            vendorID = native.vendorID.toInt(),
            deviceID = native.deviceID.toInt(),
            deviceType = VkPhysicalDeviceType.entries.first { it.value.toUInt() == native.deviceType },
            deviceName = CharArray(VK_MAX_PHYSICAL_DEVICE_NAME_SIZE) { i -> native.deviceName[i].toInt().toChar() },
            pipelineCacheUUID = ByteArray(VK_UUID_SIZE) { i -> native.pipelineCacheUUID[i].toByte() },
            limits = native.limits.toKotlinModel(),
            sparseProperties = native.sparseProperties.toKotlinModel(),
        )
    }

    actual fun vkGetPhysicalDeviceFeatures(physicalDevice: Long): VkPhysicalDeviceFeatures = memScoped {
        val nativeFeatures = alloc<NativeVkPhysicalDeviceFeatures>()
        nativeVkGetPhysicalDeviceFeatures(physicalDevice.toCPointer(), nativeFeatures.ptr)
        nativeFeatures.toKotlinModel()
    }

    actual fun vkGetPhysicalDeviceQueueFamilyProperties(
        physicalDevice: Long,
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
                },
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

    actual fun vkCreateDevice(physicalDevice: Long, deviceInfo: VkDeviceCreateInfo): Long = memScoped {
        val nativeQueueCreateInfos = allocArray<NativeVkDeviceQueueCreateInfo>(deviceInfo.pQueueCreateInfos.size) { index ->
            val queueInfo = deviceInfo.pQueueCreateInfos[index]
            sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO
            pNext = null
            flags = queueInfo.flags.toUInt()
            queueFamilyIndex = queueInfo.queueFamilyIndex.toUInt()
            queueCount = queueInfo.queueCount.toUInt()
            pQueuePriorities = allocArray(queueInfo.pQueuePriorities.size) { i -> value = queueInfo.pQueuePriorities[i] }
        }
        val nativeEnabledFeatures = deviceInfo.pEnabledFeatures.firstOrNull()?.let { features ->
            alloc<NativeVkPhysicalDeviceFeatures>().apply { fromKotlinModel(features) }
        }
        val nativeCreateInfo = alloc<NativeVkDeviceCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO
            pNext = null
            flags = deviceInfo.flags.toUInt()
            queueCreateInfoCount = deviceInfo.pQueueCreateInfos.size.toUInt()
            pQueueCreateInfos = nativeQueueCreateInfos
            val layerNames = deviceInfo.ppEnabledLayerNames
            enabledLayerCount = (layerNames?.size ?: 0).toUInt()
            ppEnabledLayerNames = layerNames?.let { names -> allocArrayOf(names.map { it.cstr.ptr }) }
            val extensionNames = deviceInfo.ppEnabledExtensionNames
            enabledExtensionCount = (extensionNames?.size ?: 0).toUInt()
            ppEnabledExtensionNames =
                extensionNames?.let { names -> allocArrayOf(names.map { it.cstr.ptr }) }
            pEnabledFeatures = nativeEnabledFeatures?.ptr
        }
        val deviceVar = alloc<VkDeviceVar>()
        val result = nativeVkCreateDevice(physicalDevice.toCPointer(), nativeCreateInfo.ptr, null, deviceVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateDevice failed: $result" }
        deviceVar.value!!.rawValue.toLong()
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
        surface: Long,
    ): Boolean = memScoped {
        val supportedVar = alloc<UIntVar>()
        nativeVkGetPhysicalDeviceSurfaceSupportKHR(
            physicalDevice.toCPointer(),
            queueFamilyIndex.toUInt(),
            surface.toCPointer<VkSurfaceKHR_T>(),
            supportedVar.ptr,
        )
        supportedVar.value != 0u
    }

    actual fun vkDestroySurfaceKHR(instance: Long, surface: Long) {
        nativeVkDestroySurfaceKHR(instance.toCPointer(), surface.toCPointer<VkSurfaceKHR_T>(), null)
    }

    actual fun vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        physicalDevice: Long,
        surface: Long,
    ): VkSurfaceCapabilitiesKHR = memScoped {
        val nativeCaps = alloc<NativeVkSurfaceCapabilitiesKHR>()
        nativeVkGetPhysicalDeviceSurfaceCapabilitiesKHR(
            physicalDevice.toCPointer(),
            surface.toCPointer<VkSurfaceKHR_T>(),
            nativeCaps.ptr,
        )
        VkSurfaceCapabilitiesKHR(
            minImageCount = nativeCaps.minImageCount.toInt(),
            maxImageCount = nativeCaps.maxImageCount.toInt(),
            currentExtent = VkExtent2D(
                width = nativeCaps.currentExtent.width.toInt(),
                height = nativeCaps.currentExtent.height.toInt(),
            ),
            minImageExtent = VkExtent2D(
                width = nativeCaps.minImageExtent.width.toInt(),
                height = nativeCaps.minImageExtent.height.toInt(),
            ),
            maxImageExtent = VkExtent2D(
                width = nativeCaps.maxImageExtent.width.toInt(),
                height = nativeCaps.maxImageExtent.height.toInt(),
            ),
            maxImageArrayLayers = nativeCaps.maxImageArrayLayers.toInt(),
            supportedTransforms = nativeCaps.supportedTransforms.toInt(),
            currentTransform = VkSurfaceTransformFlagBitsKHR.entries.first {
                it.value.toUInt() == nativeCaps.currentTransform
            },
            supportedCompositeAlpha = nativeCaps.supportedCompositeAlpha.toInt(),
            supportedUsageFlags = nativeCaps.supportedUsageFlags.toInt(),
        )
    }

    actual fun vkGetPhysicalDeviceSurfaceFormatsKHR(
        physicalDevice: Long,
        surface: Long,
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
                colorSpace = VkColorSpaceKHR.entries.first { it.value.toUInt() == native.colorSpace },
            )
        }
    }

    actual fun vkGetPhysicalDeviceSurfacePresentModesKHR(
        physicalDevice: Long,
        surface: Long,
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

    actual fun vkCreateSwapchainKHR(device: Long, createInfoKHR: VkSwapchainCreateInfoKHR): Long = memScoped {
        val nativeCreateInfo = alloc<NativeVkSwapchainCreateInfoKHR>().apply {
            sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR
            pNext = null
            flags = createInfoKHR.flags.toUInt()
            surface = createInfoKHR.surface.toCPointer()
            minImageCount = createInfoKHR.minImageCount.toUInt()
            imageFormat = createInfoKHR.imageFormat.value.toUInt()
            imageColorSpace = createInfoKHR.imageColorSpace.value.toUInt()
            imageExtent.apply {
                width = createInfoKHR.imageExtent.width.toUInt()
                height = createInfoKHR.imageExtent.height.toUInt()
            }
            imageArrayLayers = createInfoKHR.imageArrayLayers.toUInt()
            imageUsage = createInfoKHR.imageUsage.toUInt()
            imageSharingMode = createInfoKHR.imageSharingMode.value.toUInt()
            val queueFamilyIndices = createInfoKHR.pQueueFamilyIndices
            queueFamilyIndexCount = (queueFamilyIndices?.size ?: 0).toUInt()
            pQueueFamilyIndices = queueFamilyIndices?.let { indices ->
                allocArray(indices.size) { i -> value = indices[i].toUInt() }
            }
            preTransform = createInfoKHR.preTransform.value.toUInt()
            compositeAlpha = createInfoKHR.compositeAlpha.value.toUInt()
            presentMode = createInfoKHR.presentMode.value.toUInt()
            clipped = if (createInfoKHR.clipped) 1u else 0u
            oldSwapchain = createInfoKHR.oldSwapchain.toCPointer()
        }
        val swapchainVar = alloc<VkSwapchainKHRVar>()
        val result = nativeVkCreateSwapchainKHR(device.toCPointer(), nativeCreateInfo.ptr, null, swapchainVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateSwapchainKHR failed: $result" }
        swapchainVar.value!!.rawValue.toLong()
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
        createInfos: Array<VkGraphicsPipelineCreateInfo>,
    ): LongArray = memScoped {
        val nativeCreateInfos = allocArray<NativeVkGraphicsPipelineCreateInfo>(createInfos.size) { index ->
            val info = createInfos[index]
            sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO
            pNext = null
            flags = info.flags.toUInt()
            stageCount = info.pStages.size.toUInt()
            pStages = allocArray<NativeVkPipelineShaderStageCreateInfo>(info.pStages.size) { i ->
                val stage = info.pStages[i]
                sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO
                pNext = null
                flags = stage.flags.toUInt()
                this.stage = stage.stage.value.toUInt()
                module = stage.module.toCPointer()
                pName = (stage.pName ?: "main").cstr.ptr
                // pSpecializationInfo is unused by every call site in this codebase today --
                // revisit if a real specialization-constant use case appears.
                pSpecializationInfo = null
            }
            val vertexInputState = info.pVertexInputState.first()
            pVertexInputState = alloc<NativeVkPipelineVertexInputStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO
                pNext = null
                flags = vertexInputState.flags.toUInt()
                val bindings = vertexInputState.pVertexBindingDescriptions
                vertexBindingDescriptionCount = (bindings?.size ?: 0).toUInt()
                pVertexBindingDescriptions = bindings?.let { descs ->
                    allocArray<NativeVkVertexInputBindingDescription>(descs.size) { i ->
                        binding = descs[i].binding.toUInt()
                        stride = descs[i].stride.toUInt()
                        inputRate = descs[i].inputRate.value.toUInt()
                    }
                }
                val attributes = vertexInputState.pVertexAttributeDescriptions
                vertexAttributeDescriptionCount = (attributes?.size ?: 0).toUInt()
                pVertexAttributeDescriptions = attributes?.let { descs ->
                    allocArray<NativeVkVertexInputAttributeDescription>(descs.size) { i ->
                        location = descs[i].location.toUInt()
                        binding = descs[i].binding.toUInt()
                        format = descs[i].format.value.toUInt()
                        offset = descs[i].offset.toUInt()
                    }
                }
            }.ptr
            val inputAssemblyState = info.pInputAssemblyState.first()
            pInputAssemblyState = alloc<NativeVkPipelineInputAssemblyStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO
                pNext = null
                flags = inputAssemblyState.flags.toUInt()
                topology = inputAssemblyState.topology.value.toUInt()
                primitiveRestartEnable = if (inputAssemblyState.primitiveRestartEnable) 1u else 0u
            }.ptr
            val tessellationState = info.pTessellationState.first()
            pTessellationState = alloc<NativeVkPipelineTessellationStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO
                pNext = null
                flags = tessellationState.flags.toUInt()
                patchControlPoints = tessellationState.patchControlPoints.toUInt()
            }.ptr
            val viewportState = info.pViewportState.first()
            pViewportState = alloc<NativeVkPipelineViewportStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO
                pNext = null
                flags = viewportState.flags.toUInt()
                val viewports = viewportState.pViewports
                viewportCount = (viewports?.size ?: 0).toUInt()
                pViewports = viewports?.let { vps ->
                    allocArray<NativeVkViewport>(vps.size) { i ->
                        x = vps[i].x
                        y = vps[i].y
                        width = vps[i].width
                        height = vps[i].height
                        minDepth = vps[i].minDepth
                        maxDepth = vps[i].maxDepth
                    }
                }
                val scissors = viewportState.pScissors
                scissorCount = (scissors?.size ?: 0).toUInt()
                pScissors = scissors?.let { rects ->
                    allocArray<NativeVkRect2D>(rects.size) { i ->
                        offset.apply {
                            x = rects[i].offset.x
                            y = rects[i].offset.y
                        }
                        extent.apply {
                            width = rects[i].extent.width.toUInt()
                            height = rects[i].extent.height.toUInt()
                        }
                    }
                }
            }.ptr
            val rasterizationState = info.pRasterizationState.first()
            pRasterizationState = alloc<NativeVkPipelineRasterizationStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO
                pNext = null
                flags = rasterizationState.flags.toUInt()
                depthClampEnable = if (rasterizationState.depthClampEnable) 1u else 0u
                rasterizerDiscardEnable = if (rasterizationState.rasterizerDiscardEnable) 1u else 0u
                polygonMode = rasterizationState.polygonMode.value.toUInt()
                cullMode = rasterizationState.cullMode.toUInt()
                frontFace = rasterizationState.frontFace.value.toUInt()
                depthBiasEnable = if (rasterizationState.depthBiasEnable) 1u else 0u
                depthBiasConstantFactor = rasterizationState.depthBiasConstantFactor
                depthBiasClamp = rasterizationState.depthBiasClamp
                depthBiasSlopeFactor = rasterizationState.depthBiasSlopeFactor
                lineWidth = rasterizationState.lineWidth
            }.ptr
            val multisampleState = info.pMultisampleState.first()
            pMultisampleState = alloc<NativeVkPipelineMultisampleStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO
                pNext = null
                flags = multisampleState.flags.toUInt()
                rasterizationSamples = multisampleState.rasterizationSamples.value.toUInt()
                sampleShadingEnable = if (multisampleState.sampleShadingEnable) 1u else 0u
                minSampleShading = multisampleState.minSampleShading
                // pSampleMask (per-sample coverage mask) is unused by every call site in
                // this codebase today -- revisit if real multisampling is wired up.
                pSampleMask = null
                alphaToCoverageEnable = if (multisampleState.alphaToCoverageEnable) 1u else 0u
                alphaToOneEnable = if (multisampleState.alphaToOneEnable) 1u else 0u
            }.ptr
            pDepthStencilState = info.pDepthStencilState?.firstOrNull()?.let { depthStencilState ->
                alloc<NativeVkPipelineDepthStencilStateCreateInfo>().apply {
                    sType = VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO
                    pNext = null
                    flags = depthStencilState.flags.toUInt()
                    depthTestEnable = if (depthStencilState.depthTestEnable) 1u else 0u
                    depthWriteEnable = if (depthStencilState.depthWriteEnable) 1u else 0u
                    depthCompareOp = depthStencilState.depthCompareOp.value.toUInt()
                    depthBoundsTestEnable = if (depthStencilState.depthBoundsTestEnable) 1u else 0u
                    stencilTestEnable = if (depthStencilState.stencilTestEnable) 1u else 0u
                    front.apply { fromKotlinModel(depthStencilState.front) }
                    back.apply { fromKotlinModel(depthStencilState.back) }
                    minDepthBounds = depthStencilState.minDepthBounds
                    maxDepthBounds = depthStencilState.maxDepthBounds
                }.ptr
            }
            val colorBlendState = info.pColorBlendState.first()
            pColorBlendState = alloc<NativeVkPipelineColorBlendStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO
                pNext = null
                flags = colorBlendState.flags.toUInt()
                logicOpEnable = if (colorBlendState.logicOpEnable) 1u else 0u
                logicOp = colorBlendState.logicOp.value.toUInt()
                val attachments = colorBlendState.pAttachments
                attachmentCount = (attachments?.size ?: 0).toUInt()
                pAttachments = attachments?.let { atts ->
                    allocArray<NativeVkPipelineColorBlendAttachmentState>(atts.size) { i ->
                        blendEnable = if (atts[i].blendEnable) 1u else 0u
                        srcColorBlendFactor = atts[i].srcColorBlendFactor.value.toUInt()
                        dstColorBlendFactor = atts[i].dstColorBlendFactor.value.toUInt()
                        colorBlendOp = atts[i].colorBlendOp.value.toUInt()
                        srcAlphaBlendFactor = atts[i].srcAlphaBlendFactor.value.toUInt()
                        dstAlphaBlendFactor = atts[i].dstAlphaBlendFactor.value.toUInt()
                        alphaBlendOp = atts[i].alphaBlendOp.value.toUInt()
                        colorWriteMask = atts[i].colorWriteMask.toUInt()
                    }
                }
                blendConstants.apply {
                    this[0] = colorBlendState.blendConstants[0]
                    this[1] = colorBlendState.blendConstants[1]
                    this[2] = colorBlendState.blendConstants[2]
                    this[3] = colorBlendState.blendConstants[3]
                }
            }.ptr
            val dynamicState = info.pDynamicState.first()
            pDynamicState = alloc<NativeVkPipelineDynamicStateCreateInfo>().apply {
                sType = VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO
                pNext = null
                flags = dynamicState.flags.toUInt()
                dynamicStateCount = dynamicState.pDynamicStates.size.toUInt()
                pDynamicStates = allocArray(dynamicState.pDynamicStates.size) { i ->
                    value = dynamicState.pDynamicStates[i].value.toUInt()
                }
            }.ptr
            layout = info.layout.toCPointer()
            renderPass = info.renderPass.toCPointer()
            subpass = info.subpass.toUInt()
            basePipelineHandle = info.basePipelineHandle.toCPointer()
            basePipelineIndex = info.basePipelineIndex
        }
        val pipelinesArray = allocArray<CPointerVar<VkPipeline_T>>(createInfos.size)
        val result = nativeVkCreateGraphicsPipelines(
            device.toCPointer(),
            pipelineCache.toCPointer<VkPipelineCache_T>(),
            createInfos.size.toUInt(),
            nativeCreateInfos,
            null,
            pipelinesArray,
        )
        check(result == VK_SUCCESS) { "vkCreateGraphicsPipelines failed: $result" }
        LongArray(createInfos.size) { i -> pipelinesArray[i]!!.rawValue.toLong() }
    }

    actual fun vkDestroyPipeline(device: Long, pipeline: Long) {
        nativeVkDestroyPipeline(device.toCPointer(), pipeline.toCPointer<VkPipeline_T>(), null)
    }

    actual fun vkCreateRenderPass(device: Long, createInfo: VkRenderPassCreateInfo): Long = memScoped {
        val nativeAttachments = createInfo.pAttachments?.let { attachments ->
            allocArray<NativeVkAttachmentDescription>(attachments.size) { index ->
                val attachment = attachments[index]
                flags = attachment.flags.toUInt()
                format = attachment.format.value.toUInt()
                samples = attachment.samples.value.toUInt()
                loadOp = attachment.loadOp.value.toUInt()
                storeOp = attachment.storeOp.value.toUInt()
                stencilLoadOp = attachment.stencilLoadOp.value.toUInt()
                stencilStoreOp = attachment.stencilStoreOp.value.toUInt()
                initialLayout = attachment.initialLayout.value.toUInt()
                finalLayout = attachment.finalLayout.value.toUInt()
            }
        }
        val nativeSubpasses = createInfo.pSubpasses?.let { subpasses ->
            allocArray<NativeVkSubpassDescription>(subpasses.size) { index ->
                val subpass = subpasses[index]
                flags = subpass.flags.toUInt()
                pipelineBindPoint = subpass.pipelineBindPoint.value.toUInt()
                val inputAttachments = subpass.pInputAttachments
                inputAttachmentCount = (inputAttachments?.size ?: 0).toUInt()
                pInputAttachments = inputAttachments?.toNativeAttachmentRefArray(this@memScoped)
                val colorAttachments = subpass.pColorAttachments
                colorAttachmentCount = (colorAttachments?.size ?: 0).toUInt()
                pColorAttachments = colorAttachments?.toNativeAttachmentRefArray(this@memScoped)
                pResolveAttachments = subpass.pResolveAttachments?.toNativeAttachmentRefArray(this@memScoped)
                pDepthStencilAttachment = subpass.pDepthStencilAttachment?.firstOrNull()?.let { ref ->
                    alloc<NativeVkAttachmentReference>().apply {
                        attachment = ref.attachment.toUInt()
                        layout = ref.layout.value.toUInt()
                    }.ptr
                }
                val preserveAttachments = subpass.pPreserveAttachments
                preserveAttachmentCount = (preserveAttachments?.size ?: 0).toUInt()
                pPreserveAttachments = preserveAttachments?.let { indices ->
                    allocArray(indices.size) { i -> value = indices[i].toUInt() }
                }
            }
        }
        val nativeDependencies = createInfo.pDependencies?.let { dependencies ->
            allocArray<NativeVkSubpassDependency>(dependencies.size) { index ->
                val dependency = dependencies[index]
                srcSubpass = dependency.srcSubpass.toUInt()
                dstSubpass = dependency.dstSubpass.toUInt()
                srcStageMask = dependency.srcStageMask.toUInt()
                dstStageMask = dependency.dstStageMask.toUInt()
                srcAccessMask = dependency.srcAccessMask.toUInt()
                dstAccessMask = dependency.dstAccessMask.toUInt()
                dependencyFlags = dependency.dependencyFlags.toUInt()
            }
        }
        val nativeCreateInfo = alloc<NativeVkRenderPassCreateInfo>().apply {
            sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO
            pNext = null
            flags = createInfo.flags.toUInt()
            attachmentCount = (createInfo.pAttachments?.size ?: 0).toUInt()
            pAttachments = nativeAttachments
            subpassCount = (createInfo.pSubpasses?.size ?: 0).toUInt()
            pSubpasses = nativeSubpasses
            dependencyCount = (createInfo.pDependencies?.size ?: 0).toUInt()
            pDependencies = nativeDependencies
        }
        val renderPassVar = alloc<VkRenderPassVar>()
        val result = nativeVkCreateRenderPass(device.toCPointer(), nativeCreateInfo.ptr, null, renderPassVar.ptr)
        check(result == VK_SUCCESS) { "vkCreateRenderPass failed: $result" }
        renderPassVar.value!!.rawValue.toLong()
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
        graphicsPipeline: Long,
    ) {
        nativeVkCmdBindPipeline(
            commandBuffer.toCPointer(),
            pipelineBindPoint.value.toUInt(),
            graphicsPipeline.toCPointer<VkPipeline_T>(),
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
        firstInstance: Int,
    ) {
        nativeVkCmdDraw(
            commandBuffer.toCPointer(),
            vertexCount.toUInt(),
            instanceCount.toUInt(),
            firstVertex.toUInt(),
            firstInstance.toUInt(),
        )
    }

    actual fun vkCmdBeginRenderPass(
        commandBuffer: Long,
        renderPassBeginInfo: VkRenderPassBeginInfo,
        contents: VkSubpassContents,
    ) = memScoped {
        val nativeBeginInfo = alloc<NativeVkRenderPassBeginInfo>().apply {
            sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO
            pNext = null
            renderPass = renderPassBeginInfo.renderPass.toCPointer()
            framebuffer = renderPassBeginInfo.framebuffer.toCPointer()
            renderArea.apply {
                offset.apply {
                    x = renderPassBeginInfo.renderArea.offset.x
                    y = renderPassBeginInfo.renderArea.offset.y
                }
                extent.apply {
                    width = renderPassBeginInfo.renderArea.extent.width.toUInt()
                    height = renderPassBeginInfo.renderArea.extent.height.toUInt()
                }
            }
            val clearValues = renderPassBeginInfo.pClearValues
            clearValueCount = (clearValues?.size ?: 0).toUInt()
            pClearValues = clearValues?.let { values ->
                allocArray<NativeVkClearValue>(values.size) { index ->
                    when (val value = values[index]) {
                        is VkClearColorValue.Float32 -> color.float32.apply {
                            this[0] = value.values[0]
                            this[1] = value.values[1]
                            this[2] = value.values[2]
                            this[3] = value.values[3]
                        }
                        is VkClearDepthStencilValue -> depthStencil.apply {
                            depth = value.depth
                            stencil = value.stencil.toUInt()
                        }
                        else -> error(
                            "vkCmdBeginRenderPass: unsupported VkClearValue variant on iOS: $value",
                        )
                    }
                }
            }
        }
        nativeVkCmdBeginRenderPass(commandBuffer.toCPointer(), nativeBeginInfo.ptr, contents.value.toUInt())
        Unit
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
            timeout.toULong(),
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
        fence: Long,
    ): Int = memScoped {
        val imageIndexVar = alloc<UIntVar>()
        nativeVkAcquireNextImageKHR(
            device.toCPointer(),
            swapchain.toCPointer<VkSwapchainKHR_T>(),
            timeout.toULong(),
            semaphore.toCPointer<VkSemaphore_T>(),
            fence.toCPointer<VkFence_T>(),
            imageIndexVar.ptr,
        )
        imageIndexVar.value.toInt()
    }

    actual fun vkResetCommandBuffer(commandBuffer: Long, flags: Int) {
        nativeVkResetCommandBuffer(commandBuffer.toCPointer(), flags.toUInt())
    }

    actual fun vkQueueSubmit(queue: Long, pSubmits: Array<VkSubmitInfo>, fence: Long) = memScoped {
        val nativeSubmits = allocArray<NativeVkSubmitInfo>(pSubmits.size) { index ->
            val submit = pSubmits[index]
            sType = VK_STRUCTURE_TYPE_SUBMIT_INFO
            pNext = null
            val waitSemaphores = submit.pWaitSemaphores
            waitSemaphoreCount = (waitSemaphores?.size ?: 0).toUInt()
            pWaitSemaphores = waitSemaphores?.let { semaphores ->
                allocArray<CPointerVar<VkSemaphore_T>>(semaphores.size) { i -> value = semaphores[i].toCPointer() }
            }
            pWaitDstStageMask = submit.pWaitDstStageMask?.let { masks ->
                allocArray(masks.size) { i -> value = masks[i].toUInt() }
            }
            val commandBuffers = submit.pCommandBuffers
            commandBufferCount = (commandBuffers?.size ?: 0).toUInt()
            pCommandBuffers = commandBuffers?.let { buffers ->
                allocArray<CPointerVar<VkCommandBuffer_T>>(buffers.size) { i -> value = buffers[i].toCPointer() }
            }
            val signalSemaphores = submit.pSignalSemaphores
            signalSemaphoreCount = (signalSemaphores?.size ?: 0).toUInt()
            pSignalSemaphores = signalSemaphores?.let { semaphores ->
                allocArray<CPointerVar<VkSemaphore_T>>(semaphores.size) { i -> value = semaphores[i].toCPointer() }
            }
        }
        val result = nativeVkQueueSubmit(
            queue.toCPointer<VkQueue_T>(),
            pSubmits.size.toUInt(),
            nativeSubmits,
            fence.toCPointer<VkFence_T>(),
        )
        check(result == VK_SUCCESS) { "vkQueueSubmit failed: $result" }
        Unit
    }

    actual fun vkQueuePresentKHR(queue: Long, pPresentInfoKHR: VkPresentInfoKHR) = memScoped {
        val nativePresentInfo = alloc<NativeVkPresentInfoKHR>().apply {
            sType = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR
            pNext = null
            val waitSemaphores = pPresentInfoKHR.pWaitSemaphores
            waitSemaphoreCount = (waitSemaphores?.size ?: 0).toUInt()
            pWaitSemaphores = waitSemaphores?.let { semaphores ->
                allocArray<CPointerVar<VkSemaphore_T>>(semaphores.size) { i -> value = semaphores[i].toCPointer() }
            }
            val swapchains = pPresentInfoKHR.pSwapchains
            swapchainCount = (swapchains?.size ?: 0).toUInt()
            pSwapchains = swapchains?.let { chains ->
                allocArray<CPointerVar<VkSwapchainKHR_T>>(chains.size) { i -> value = chains[i].toCPointer() }
            }
            pImageIndices = pPresentInfoKHR.pImageIndices?.let { indices ->
                allocArray(indices.size) { i -> value = indices[i].toUInt() }
            }
            pResults = null
        }
        val result = nativeVkQueuePresentKHR(queue.toCPointer<VkQueue_T>(), nativePresentInfo.ptr)
        check(result == VK_SUCCESS) { "vkQueuePresentKHR failed: $result" }
        Unit
    }

    actual fun vkCreateDebugUtilsMessengerEXT(
        instance: Long,
        createInfo: VkDebugUtilsMessengerCreateInfoEXT,
    ): Long = memScoped {
        val callbackRef = StableRef.create(createInfo.pfnUserCallback)
        val nativeCreateInfo = alloc<NativeVkDebugUtilsMessengerCreateInfoEXT>().apply {
            sType = VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT
            pNext = null
            flags = createInfo.flags.toUInt()
            messageSeverity = createInfo.messageSeverity.toUInt()
            messageType = createInfo.messageType.toUInt()
            pfnUserCallback = debugMessengerTrampoline
            pUserData = callbackRef.asCPointer()
        }
        val messengerVar = alloc<VkDebugUtilsMessengerEXTVar>()
        val result = nativeVkCreateDebugUtilsMessengerEXT(
            instance.toCPointer(),
            nativeCreateInfo.ptr,
            null,
            messengerVar.ptr,
        )
        check(result == VK_SUCCESS) {
            callbackRef.dispose()
            "vkCreateDebugUtilsMessengerEXT failed: $result"
        }
        val handle = messengerVar.value!!.rawValue.toLong()
        debugMessengerCallbacks[handle] = callbackRef
        handle
    }

    actual fun vkDestroyDebugUtilsMessengerEXT(instance: Long, debugUtilsMessenger: Long) {
        nativeVkDestroyDebugUtilsMessengerEXT(
            instance.toCPointer(),
            debugUtilsMessenger.toCPointer<cnames.structs.VkDebugUtilsMessengerEXT_T>(),
            null,
        )
        debugMessengerCallbacks.remove(debugUtilsMessenger)?.dispose()
    }
}
