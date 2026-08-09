/*
 *  VulkanUtils.h
 *  Vulkan utils for Vulkan
 *  Created by Ron June Valdoz */

#ifndef VULKANUTILS_H
#define VULKANUTILS_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <stdexcept>
#include <vector>
#include <enum_utils.h>
#include <exception_utils.h>
#include <includes/VkRenderPassBeginInfoAccessor.h>
#include <includes/VkPipelineCacheCreateInfoAccessor.h>
#include <includes/VkPipelineLayoutCreateInfoAccessor.h>
#include <includes/VkCommandPoolCreateInfoAccessor.h>
#include <includes/VkPhysicalDeviceFeaturesMutator.h>
#include <includes/VkPhysicalDevicePropertiesMutator.h>
#include <includes/VkGraphicsPipelineCreateInfoAccessor.h>
#include <includes/VkShaderModuleCreateInfoAccessor.h>
#ifdef __ANDROID__
#include <includes/VkAndroidSurfaceCreateInfoKHRAccessor.h>
#endif // __ANDROID__
#include <includes/VkCommandBufferBeginInfoAccessor.h>
#include <includes/VkDebugUtilsMessengerCreateInfoEXTAccessor.h>
#include <includes/VkLayerPropertiesMutator.h>
#include <includes/VkSwapchainCreateInfoKHRAccessor.h>
#include <includes/VkFramebufferCreateInfoAccessor.h>
#include <includes/VkCommandBufferAllocateInfoAccessor.h>
#include <includes/VkDeviceCreateInfoAccessor.h>
#include <includes/VkImageViewCreateInfoAccessor.h>
#include <includes/VkInstanceCreateInfoAccessor.h>
#include <includes/VkRect2DAccessor.h>
#include <includes/VkRenderPassCreateInfoAccessor.h>
#include <includes/VkViewportAccessor.h>
#include <includes/VkSubmitInfoAccessor.h>
#include <includes/VkPresentInfoKHRAccessor.h>
#include <includes/VkSemaphoreCreateInfoAccessor.h>
#include <includes/VkFenceCreateInfoAccessor.h>
#include <includes/VkQueueFamilyPropertiesMutator.h>
#include <includes/VkExtensionPropertiesMutator.h>
#include <includes/VkSurfaceCapabilitiesKHRMutator.h>
#include <includes/VkSurfaceFormatKHRMutator.h>

namespace awake {
    jobjectArray getPhysicalDeviceQueueFamilyProperties(JNIEnv *env, jlong arg0);

    void destroyFramebuffer(jlong arg0, jlong arg1);

    jlongArray createGraphicsPipelines(JNIEnv *env, jlong arg0, jlong arg1, jobjectArray arg2);

    void destroySurfaceKHR(jlong arg0, jlong arg1);

    jlong createDebugUtilsMessengerEXT(JNIEnv *env, jlong arg0, jobject arg1);

    void cmdBeginRenderPass(JNIEnv *env, jlong arg0, jobject arg1, jobject arg2);

    void cmdEndRenderPass(JNIEnv *env, jlong arg0);

    jlong allocateCommandBuffers(JNIEnv *env, jlong arg0, jobject arg1);

    jlong getDeviceQueue(JNIEnv *env, jlong arg0, jint arg1, jint arg2);

    jobject getPhysicalDeviceSurfaceCapabilitiesKHR(JNIEnv *env, jlong arg0, jlong arg1);

    jobject getPhysicalDeviceProperties(JNIEnv *env, jlong arg0);

    void destroyPipelineCache(jlong arg0, jlong arg1);

    void cmdBindPipeline(JNIEnv *env, jlong arg0, jobject arg1, jlong arg2);

    void destroyPipelineLayout(jlong arg0, jlong arg1);

    jobjectArray enumerateDeviceExtensionProperties(JNIEnv *env, jlong arg0, jstring arg1);

    jobject getPhysicalDeviceFeatures(JNIEnv *env, jlong arg0);

    void destroySemaphore(jlong arg0, jlong arg1);

    void queuePresentKHR(JNIEnv *env, jlong arg0, jobject arg1);

    void destroyImageView(jlong arg0, jlong arg1);

    void queueSubmit(JNIEnv *env, jlong arg0, jobjectArray arg1, jlong arg2);

    jlong createSemaphore(JNIEnv *env, jlong arg0, jobject arg1);

    jlong createFramebuffer(JNIEnv *env, jlong arg0, jobject arg1);

    jlong createRenderPass(JNIEnv *env, jlong arg0, jobject arg1);

    jlongArray enumeratePhysicalDevices(JNIEnv *env, jlong arg0);

    jlong createDevice(JNIEnv *env, jlong arg0, jobject arg1);

    void destroySwapchainKHR(jlong arg0, jlong arg1);

    jobjectArray enumerateInstanceExtensionProperties(JNIEnv *env, jstring arg0);

    jlong createSwapchainKHR(JNIEnv *env, jlong arg0, jobject arg1);

    void endCommandBuffer(JNIEnv *env, jlong arg0);

    void destroyDebugUtilsMessengerEXT(jlong arg0, jlong arg1);

    void destroyDevice(jlong arg0);

    void cmdSetViewport(JNIEnv *env, jlong arg0, jint arg1, jobjectArray arg2);

    jlong createImageView(JNIEnv *env, jlong arg0, jobject arg1);

    void destroyInstance(jlong arg0);

    void resetFences(JNIEnv *env, jlong arg0, jlongArray arg1);

    jlong createPipelineLayout(JNIEnv *env, jlong arg0, jobject arg1);

    void destroyShaderModule(jlong arg0, jlong arg1);

    jlongArray getSwapchainImagesKHR(JNIEnv *env, jlong arg0, jlong arg1);

    jlong createFence(JNIEnv *env, jlong arg0, jobject arg1);

#ifdef __ANDROID__
    jlong createAndroidSurfaceKHR(JNIEnv *env, jlong arg0, jobject arg1);
#endif // __ANDROID__

    jobjectArray enumerateInstanceLayerProperties(JNIEnv *env);

    jlong createInstance(JNIEnv *env, jobject arg0);

    void waitForFences(JNIEnv *env, jlong arg0, jlongArray arg1, jboolean arg2, jlong arg3);

    void cmdSetScissor(JNIEnv *env, jlong arg0, jint arg1, jobjectArray arg2);

    void destroyFence(jlong arg0, jlong arg1);

    void beginCommandBuffer(JNIEnv *env, jlong arg0, jobject arg1);

    void destroyRenderPass(jlong arg0, jlong arg1);

    jint
    acquireNextImageKHR(JNIEnv *env, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4);

    void resetCommandBuffer(JNIEnv *env, jlong arg0, jint arg1);

    jlong createShaderModule(JNIEnv *env, jlong arg0, jobject arg1);

    jboolean getPhysicalDeviceSurfaceSupportKHR(JNIEnv *env, jlong arg0, jint arg1, jlong arg2);

    jlong createPipelineCache(JNIEnv *env, jlong arg0, jobject arg1);

    jobjectArray getPhysicalDeviceSurfacePresentModesKHR(JNIEnv *env, jlong arg0, jlong arg1);

    void destroyCommandPool(jlong arg0, jlong arg1);

    void cmdDraw(JNIEnv *env, jlong arg0, jint arg1, jint arg2, jint arg3, jint arg4);

    void destroyPipeline(jlong arg0, jlong arg1);

    jlong createCommandPool(JNIEnv *env, jlong arg0, jobject arg1);

    jobjectArray getPhysicalDeviceSurfaceFormatsKHR(JNIEnv *env, jlong arg0, jlong arg1);
}
#endif // VULKANUTILS_H