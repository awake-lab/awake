# vulkan-kotlin/CMakeLists.txt

set(VULKAN_KOTLIN_SOURCES
        enum_utils.cpp
        exception_utils.cpp
        Float32Accessor.cpp
        Int32Accessor.cpp
        UInt32Accessor.cpp
        VkApplicationInfoAccessor.cpp
        VkAttachmentDescriptionAccessor.cpp
        VkAttachmentReferenceAccessor.cpp
        VkClearColorValueAccessor.cpp
        VkClearDepthStencilValueAccessor.cpp
        VkCommandBufferAllocateInfoAccessor.cpp
        VkCommandBufferBeginInfoAccessor.cpp
        VkCommandBufferInheritanceInfoAccessor.cpp
        VkCommandPoolCreateInfoAccessor.cpp
        VkComponentMappingAccessor.cpp
        VkDebugUtilsLabelEXTAccessor.cpp
        VkDebugUtilsLabelEXTMutator.cpp
        VkDebugUtilsMessengerCallbackDataEXTAccessor.cpp
        VkDebugUtilsMessengerCallbackDataEXTMutator.cpp
        VkDebugUtilsMessengerCreateInfoEXTAccessor.cpp
        VkDebugUtilsObjectNameInfoEXTAccessor.cpp
        VkDebugUtilsObjectNameInfoEXTMutator.cpp
        VkDeviceCreateInfoAccessor.cpp
        VkDeviceQueueCreateInfoAccessor.cpp
        VkExtensionPropertiesAccessor.cpp
        VkExtensionPropertiesMutator.cpp
        VkExtent2DAccessor.cpp
        VkExtent2DMutator.cpp
        VkExtent3DAccessor.cpp
        VkExtent3DMutator.cpp
        VkFenceCreateInfoAccessor.cpp
        VkFramebufferCreateInfoAccessor.cpp
        VkGraphicsPipelineCreateInfoAccessor.cpp
        VkImageSubresourceRangeAccessor.cpp
        VkImageViewCreateInfoAccessor.cpp
        VkInstanceCreateInfoAccessor.cpp
        VkLayerPropertiesAccessor.cpp
        VkLayerPropertiesMutator.cpp
        VkOffset2DAccessor.cpp
        VkPhysicalDeviceFeaturesAccessor.cpp
        VkPhysicalDeviceFeaturesMutator.cpp
        VkPhysicalDeviceLimitsAccessor.cpp
        VkPhysicalDeviceLimitsMutator.cpp
        VkPhysicalDevicePropertiesAccessor.cpp
        VkPhysicalDevicePropertiesMutator.cpp
        VkPhysicalDeviceSparsePropertiesAccessor.cpp
        VkPhysicalDeviceSparsePropertiesMutator.cpp
        VkPipelineCacheCreateInfoAccessor.cpp
        VkPipelineColorBlendAttachmentStateAccessor.cpp
        VkPipelineColorBlendStateCreateInfoAccessor.cpp
        VkPipelineDepthStencilStateCreateInfoAccessor.cpp
        VkPipelineDynamicStateCreateInfoAccessor.cpp
        VkPipelineInputAssemblyStateCreateInfoAccessor.cpp
        VkPipelineLayoutCreateInfoAccessor.cpp
        VkPipelineMultisampleStateCreateInfoAccessor.cpp
        VkPipelineRasterizationStateCreateInfoAccessor.cpp
        VkPipelineShaderStageCreateInfoAccessor.cpp
        VkPipelineTessellationStateCreateInfoAccessor.cpp
        VkPipelineVertexInputStateCreateInfoAccessor.cpp
        VkPipelineViewportStateCreateInfoAccessor.cpp
        VkPresentInfoKHRAccessor.cpp
        VkPushConstantRangeAccessor.cpp
        VkQueueFamilyPropertiesAccessor.cpp
        VkQueueFamilyPropertiesMutator.cpp
        VkRect2DAccessor.cpp
        VkRenderPassBeginInfoAccessor.cpp
        VkRenderPassCreateInfoAccessor.cpp
        VkSemaphoreCreateInfoAccessor.cpp
        VkShaderModuleCreateInfoAccessor.cpp
        VkSpecializationInfoAccessor.cpp
        VkSpecializationMapEntryAccessor.cpp
        VkStencilOpStateAccessor.cpp
        VkSubmitInfoAccessor.cpp
        VkSubpassDependencyAccessor.cpp
        VkSubpassDescriptionAccessor.cpp
        VkSurfaceCapabilitiesKHRAccessor.cpp
        VkSurfaceCapabilitiesKHRMutator.cpp
        VkSurfaceFormatKHRAccessor.cpp
        VkSurfaceFormatKHRMutator.cpp
        VkSwapchainCreateInfoKHRAccessor.cpp
        VkVertexInputAttributeDescriptionAccessor.cpp
        VkVertexInputBindingDescriptionAccessor.cpp
        VkViewportAccessor.cpp
        VulkanUtils.cpp
        )

# VkAndroidSurfaceCreateInfoKHRAccessor.{cpp,h} includes <android/native_window_jni.h>,
# unavailable outside the Android NDK -- excluded from non-Android builds (its only
# caller, VulkanUtils.cpp's createAndroidSurfaceKHR, is itself `#ifdef __ANDROID__`
# guarded, so nothing references this file when it's left out).
if(ANDROID)
    list(APPEND VULKAN_KOTLIN_SOURCES VkAndroidSurfaceCreateInfoKHRAccessor.cpp)
endif()

# Set the path to your header file directory
include_directories(includes)
add_library(vulkan_kotlin STATIC ${VULKAN_KOTLIN_SOURCES})

target_include_directories(vulkan_kotlin PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
