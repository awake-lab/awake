#include "jni.h"
#include "includes/VulkanUtils.h"


extern "C" {
JNIEXPORT jlong
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateInstance(JNIEnv *env, jobject thiz,
                                                                  jobject createInfo) {
    return awake::createInstance(env, createInfo);
}

JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateDebugUtilsMessengerEXT(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jlong instance,
                                                                                jobject create_info) {
    return awake::createDebugUtilsMessengerEXT(env, instance, create_info);
}
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyDebugUtilsMessengerEXT(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jlong instance,
                                                                                 jlong debug_utils_messenger) {
    awake::destroyDebugUtilsMessengerEXT(instance, debug_utils_messenger);
}
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyInstance(JNIEnv *env, jobject thiz,
                                                                   jlong instance) {
    awake::destroyInstance(instance);
}

JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyDevice(JNIEnv *env, jobject thiz,
                                                                 jlong device) {
    awake::destroyDevice(device);
}
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetDeviceQueue(JNIEnv *env, jobject thiz,
                                                                  jlong device,
                                                                  jint queue_family_index,
                                                                  jint queue_index) {
    return awake::getDeviceQueue(env, device, queue_family_index, queue_index);
}
// Vulkan wrapper

JNIEXPORT jlongArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkEnumeratePhysicalDevices(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong vkInstance) {
    return awake::enumeratePhysicalDevices(env, vkInstance);
}

JNIEXPORT jobject JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceProperties(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong physical_device) {


    return awake::getPhysicalDeviceProperties(env,
                                              physical_device);
}

JNIEXPORT jobject JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceFeatures(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong physical_device) {

    return awake::getPhysicalDeviceFeatures(env,
                                            physical_device);
}

JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceQueueFamilyProperties(
        JNIEnv *env, jobject thiz, jlong physical_device) {
    return awake::getPhysicalDeviceQueueFamilyProperties(env, physical_device);
}
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateDevice(JNIEnv *env, jobject thiz,
                                                                jlong physical_device,
                                                                jobject device_info) {
    return awake::createDevice(env, physical_device, device_info);
}

#ifdef __ANDROID__
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateAndroidSurfaceKHR(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong pInstance,
                                                                           jobject surface_info) {
    return awake::createAndroidSurfaceKHR(env, pInstance, surface_info);
}
#endif // __ANDROID__

JNIEXPORT jboolean JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceSurfaceSupportKHR(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jlong pDevice,
                                                                                      jint queueFamilyIndex,
                                                                                      jlong pSurface) {
    return awake::getPhysicalDeviceSurfaceSupportKHR(env, pDevice, queueFamilyIndex,
                                                     pSurface);
}
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroySurfaceKHR(JNIEnv *env, jobject thiz,
                                                                     jlong pInstance,
                                                                     jlong pSurface) {
    awake::destroySurfaceKHR(pInstance, pSurface);
}

JNIEXPORT jobject JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
        JNIEnv *env, jobject thiz, jlong pPhysicalDevice, jlong pSurface) {
    return awake::getPhysicalDeviceSurfaceCapabilitiesKHR(env, pPhysicalDevice, pSurface);
}
JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceSurfaceFormatsKHR(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jlong pPhysicalDevice,
                                                                                      jlong pSurface) {
    return awake::getPhysicalDeviceSurfaceFormatsKHR(env, pPhysicalDevice, pSurface);
}
JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetPhysicalDeviceSurfacePresentModesKHR(
        JNIEnv *env, jobject thiz, jlong pPhysicalDevice, jlong pSurface) {
    return awake::getPhysicalDeviceSurfacePresentModesKHR(env, pPhysicalDevice, pSurface);
}

JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateSwapchainKHR(JNIEnv *env, jobject thiz,
                                                                      jlong pDevice,
                                                                      jobject create_info_khr) {
    return awake::createSwapchainKHR(env, pDevice, create_info_khr);
}
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroySwapchainKHR(JNIEnv *env, jobject thiz,
                                                                       jlong pDevice,
                                                                       jlong swapchain_khr) {
    awake::destroySwapchainKHR(pDevice, swapchain_khr);
}

JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkEnumerateInstanceExtensionProperties(JNIEnv *env,
                                                                                        jobject thiz,
                                                                                        jstring layer_name) {
    return awake::enumerateInstanceExtensionProperties(env, layer_name);
}

JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkEnumerateDeviceExtensionProperties(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jlong physical_device,
                                                                                      jstring layer_name) {
    return awake::enumerateDeviceExtensionProperties(env, physical_device, layer_name);
}
JNIEXPORT jlongArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkGetSwapchainImagesKHR(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jlong device,
                                                                         jlong swapchain) {
    return awake::getSwapchainImagesKHR(env, device, swapchain);
}

JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateImageView(
        JNIEnv *env, jobject thiz, jlong pDevice, jobject pCreateInfo) {
    return awake::createImageView(env, pDevice, pCreateInfo);
}

JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyImageView(JNIEnv *env, jobject thiz,
                                                                    jlong pDevice,
                                                                    jlong pImageView) {
    awake::destroyImageView(pDevice, pImageView);
}

JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateShaderModule(JNIEnv *env, jobject thiz,
                                                                      jlong device,
                                                                      jobject create_info) {
    return awake::createShaderModule(env, device, create_info);
}
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyShaderModule(JNIEnv *env, jobject thiz,
                                                                       jlong device,
                                                                       jlong shader_module) {
    return awake::destroyShaderModule(device, shader_module);
}
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreatePipelineCache(JNIEnv *env, jobject thiz,
                                                                       jlong device,
                                                                       jobject create_info) {
    return awake::createPipelineCache(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyPipelineCache(JNIEnv *env, jobject thiz,
                                                                        jlong device,
                                                                        jlong pipeline_cache) {
    awake::destroyPipelineCache(device, pipeline_cache);
}
extern "C"
JNIEXPORT jlongArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateGraphicsPipelines(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong device,
                                                                           jlong pipeline_cache,
                                                                           jobjectArray create_infos) {
    return awake::createGraphicsPipelines(env, device, pipeline_cache, create_infos);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyPipeline(JNIEnv *env, jobject thiz,
                                                                   jlong device, jlong pipeline) {
    awake::destroyPipeline(device, pipeline);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreatePipelineLayout(JNIEnv *env, jobject thiz,
                                                                        jlong device,
                                                                        jobject create_info) {
    return awake::createPipelineLayout(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyPipelineLayout(JNIEnv *env, jobject thiz,
                                                                         jlong device,
                                                                         jlong pipeline_layout) {
    awake::destroyPipelineLayout(device, pipeline_layout);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateRenderPass(JNIEnv *env, jobject thiz,
                                                                    jlong device,
                                                                    jobject create_info) {
    return awake::createRenderPass(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyRenderPass(JNIEnv *env, jobject thiz,
                                                                     jlong device,
                                                                     jlong render_pass) {
    awake::destroyRenderPass(device, render_pass);
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkEnumerateInstanceLayerProperties(JNIEnv *env,
                                                                                    jobject thiz) {
    return awake::enumerateInstanceLayerProperties(env);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateFramebuffer(JNIEnv *env, jobject thiz,
                                                                     jlong device,
                                                                     jobject framebuffer_info) {
    return awake::createFramebuffer(env, device, framebuffer_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyFramebuffer(JNIEnv *env, jobject thiz,
                                                                      jlong device,
                                                                      jlong framebuffer) {
    awake::destroyFramebuffer(device, framebuffer);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkAllocateCommandBuffers(JNIEnv *env, jobject thiz,
                                                                          jlong device,
                                                                          jobject create_info) {
    return awake::allocateCommandBuffers(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkBeginCommandBuffer(JNIEnv *env, jobject thiz,
                                                                      jlong command_buffer,
                                                                      jobject begin_info) {
    awake::beginCommandBuffer(env, command_buffer, begin_info);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateCommandPool(JNIEnv *env, jobject thiz,
                                                                     jlong device,
                                                                     jobject create_info) {
    return awake::createCommandPool(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyCommandPool(JNIEnv *env, jobject thiz,
                                                                      jlong device,
                                                                      jlong command_pool) {
    awake::destroyCommandPool(device, command_pool);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdBindPipeline(JNIEnv *env, jobject thiz,
                                                                   jlong command_buffer,
                                                                   jobject pipeline_bind_point,
                                                                   jlong graphics_pipeline) {
    awake::cmdBindPipeline(env, command_buffer, pipeline_bind_point, graphics_pipeline);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdSetViewport(JNIEnv *env, jobject thiz,
                                                                  jlong command_buffer,
                                                                  jint first_viewport,
                                                                  jobjectArray viewports) {
    awake::cmdSetViewport(env, command_buffer, first_viewport, viewports);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdSetScissor(JNIEnv *env, jobject thiz,
                                                                 jlong command_buffer,
                                                                 jint first_scissor,
                                                                 jobjectArray scissors) {
    awake::cmdSetScissor(env, command_buffer, first_scissor, scissors);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdDraw(JNIEnv *env, jobject thiz,
                                                           jlong command_buffer, jint vertex_count,
                                                           jint instance_count, jint first_vertex,
                                                           jint first_instance) {
    awake::cmdDraw(env, command_buffer, vertex_count, instance_count, first_vertex, first_instance);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdEndRenderPass(JNIEnv *env, jobject thiz,
                                                                    jlong command_buffer) {
    awake::cmdEndRenderPass(env, command_buffer);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkEndCommandBuffer(JNIEnv *env, jobject thiz,
                                                                    jlong command_buffer) {
    awake::endCommandBuffer(env, command_buffer);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCmdBeginRenderPass(JNIEnv *env, jobject thiz,
                                                                      jlong command_buffer,
                                                                      jobject render_pass_info,
                                                                      jobject contents) {
    awake::cmdBeginRenderPass(env, command_buffer, render_pass_info, contents);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateSemaphore(JNIEnv *env, jobject thiz,
                                                                   jlong device,
                                                                   jobject create_info) {
    return awake::createSemaphore(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroySemaphore(JNIEnv *env, jobject thiz,
                                                                    jlong device, jlong semaphore) {
    awake::destroySemaphore(device, semaphore);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkCreateFence(JNIEnv *env, jobject thiz,
                                                               jlong device, jobject create_info) {
    return awake::createFence(env, device, create_info);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkDestroyFence(JNIEnv *env, jobject thiz,
                                                                jlong device, jlong fence) {
    awake::destroyFence(device, fence);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkWaitForFences(JNIEnv *env, jobject thiz,
                                                                 jlong device, jlongArray fences,
                                                                 jboolean wait_all, jlong timeout) {
    awake::waitForFences(env, device, fences, wait_all, timeout);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkResetFences(JNIEnv *env, jobject thiz,
                                                               jlong device, jlongArray fences) {
    awake::resetFences(env, device, fences);
}
extern "C"
JNIEXPORT jint JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkAcquireNextImageKHR(JNIEnv *env, jobject thiz,
                                                                       jlong device,
                                                                       jlong swapchain,
                                                                       jlong timeout,
                                                                       jlong semaphore,
                                                                       jlong fence) {
    return awake::acquireNextImageKHR(env, device, swapchain, timeout, semaphore, fence);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkResetCommandBuffer(JNIEnv *env, jobject thiz,
                                                                      jlong command_buffer,
                                                                      jint flags) {
    return awake::resetCommandBuffer(env, command_buffer, flags);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkQueueSubmit(JNIEnv *env, jobject thiz,
                                                               jlong queue, jobjectArray p_submits,
                                                               jlong fence) {
    awake::queueSubmit(env, queue, p_submits, fence);
}
extern "C"
JNIEXPORT void JNICALL
Java_io_github_ronjunevaldoz_awake_vulkan_Vulkan_vkQueuePresentKHR(JNIEnv *env, jobject thiz,
                                                                   jlong queue,
                                                                   jobject p_present_info_khr) {
    awake::queuePresentKHR(env, queue, p_present_info_khr);
}