/*
 *  VulkanUtils.cpp
 *  Vulkan utils for Vulkan
 *  Created by Ron June Valdoz */

#include <includes/VulkanUtils.h>

namespace awake {
jobjectArray
getPhysicalDeviceQueueFamilyProperties(JNIEnv *env, jlong arg0) {
    // process parameter??
    auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
    // process get??
    uint32_t count;
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &count, nullptr);
    std::vector<VkQueueFamilyProperties> vkArray(count);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &count, vkArray.data());
    jclass clazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/VkQueueFamilyProperties");
    auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
    env->DeleteLocalRef(clazz);
    for (int i = 0; i < count; ++i) {
        jobject obj = VkQueueFamilyPropertiesMutator(env).toObject(vkArray[i]);
        env->SetObjectArrayElement(jArray, (jint) i, obj);
        env->DeleteLocalRef(obj);
    }
    return jArray;
}

    void
    destroyFramebuffer(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto framebuffer = reinterpret_cast<VkFramebuffer>(arg1);
        // process destroy??
        vkDestroyFramebuffer(device, framebuffer, nullptr);
        // unable to process void
    }

    jlongArray
    createGraphicsPipelines(JNIEnv *env, jlong arg0, jlong arg1, jobjectArray arg2) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto pipelineCache = reinterpret_cast<VkPipelineCache>(arg1);
        // process array
        auto size = env->GetArrayLength(arg2);
        std::vector<VkGraphicsPipelineCreateInfo> vkArray;
        for (int i = 0; i < size; ++i) {
            VkGraphicsPipelineCreateInfo info{};
            auto element = (jobject) env->GetObjectArrayElement(arg2, i);
            VkGraphicsPipelineCreateInfoAccessor(env, element).fromObject(info);
            vkArray.push_back(info);
            env->DeleteLocalRef(element);
        }
        // process create??
        // process handle
        std::vector<VkPipeline> handle(size);
        VkResult result = vkCreateGraphicsPipelines(device, pipelineCache,
                                                    static_cast<uint32_t>(vkArray.size()),
                                                    vkArray.data(), nullptr, handle.data());
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateGraphicsPipelines");
        }
        auto jArray = env->NewLongArray(static_cast<jsize>(vkArray.size()));
        for (int i = 0; i < size; ++i) {
            auto value = reinterpret_cast<jlong>(handle[i]);
            env->SetLongArrayRegion(jArray, i, 1, &value);
        }
        return jArray;
    }

    void
    destroySurfaceKHR(jlong arg0, jlong arg1) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        auto surfaceKHR = reinterpret_cast<VkSurfaceKHR>(arg1);
        // process destroy??
        vkDestroySurfaceKHR(instance, surfaceKHR, nullptr);
        // unable to process void
    }

    jlong
    createDebugUtilsMessengerEXT(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        // object accessor?
        VkDebugUtilsMessengerCreateInfoEXT info{};
        // process singleton
        VkDebugUtilsMessengerCreateInfoEXTAccessor::init(env, arg1);
        VkDebugUtilsMessengerCreateInfoEXTAccessor::getInstance().fromObject(info);
        // process create??
        // process handle
        VkDebugUtilsMessengerEXT handle;
        auto pfnvkCreateDebugUtilsMessengerEXT = (PFN_vkCreateDebugUtilsMessengerEXT) vkGetInstanceProcAddr(
                instance, "vkCreateDebugUtilsMessengerEXT");
        VkResult result = pfnvkCreateDebugUtilsMessengerEXT(instance, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateDebugUtilsMessengerEXT");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    cmdBeginRenderPass(JNIEnv *env, jlong arg0, jobject arg1, jobject arg2) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // object accessor?
        VkRenderPassBeginInfo info{};
        VkRenderPassBeginInfoAccessor(env, arg1).fromObject(info);
        // enum
        auto vkarg2 = enum_utils::getEnumFromObject(env, arg2);
        // process cmd??
        vkCmdBeginRenderPass(commandBuffer, &info, static_cast<VkSubpassContents>(vkarg2));
        // unable to process void
    }

    void
    cmdEndRenderPass(JNIEnv *env, jlong arg0) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process cmd??
        vkCmdEndRenderPass(commandBuffer);
        // unable to process void
    }

    jlong
    allocateCommandBuffers(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkCommandBufferAllocateInfo info{};
        VkCommandBufferAllocateInfoAccessor(env, arg1).fromObject(info);
        // process default??
        VkCommandBuffer handle;
        VkResult result = vkAllocateCommandBuffers(device, &info, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkAllocateCommandBuffers");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jlong
    getDeviceQueue(JNIEnv *env, jlong arg0, jint arg1, jint arg2) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        // process primitive  int??
        uint32_t vkarg2 = arg2;
        // process get??
        VkQueue handle;
        vkGetDeviceQueue(device, vkarg1, vkarg2, &handle);
        return reinterpret_cast<jlong>(handle);
    }

    jobject
    getPhysicalDeviceSurfaceCapabilitiesKHR(JNIEnv *env, jlong arg0, jlong arg1) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        auto surfaceKHR = reinterpret_cast<VkSurfaceKHR>(arg1);
        // process get??
        VkSurfaceCapabilitiesKHR handle;
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surfaceKHR, &handle);
        return VkSurfaceCapabilitiesKHRMutator(env).toObject(handle);
    }

    jobject
    getPhysicalDeviceProperties(JNIEnv *env, jlong arg0) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        // process get??
        VkPhysicalDeviceProperties handle;
        vkGetPhysicalDeviceProperties(physicalDevice, &handle);
        return VkPhysicalDevicePropertiesMutator(env).toObject(handle);
    }

    void
    destroyPipelineCache(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto pipelineCache = reinterpret_cast<VkPipelineCache>(arg1);
        // process destroy??
        vkDestroyPipelineCache(device, pipelineCache, nullptr);
        // unable to process void
    }

    void
    cmdBindPipeline(JNIEnv *env, jlong arg0, jobject arg1, jlong arg2) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // enum
        auto vkarg1 = enum_utils::getEnumFromObject(env, arg1);
        auto pipeline = reinterpret_cast<VkPipeline>(arg2);
        // process cmd??
        vkCmdBindPipeline(commandBuffer, static_cast<VkPipelineBindPoint>(vkarg1), pipeline);
        // unable to process void
    }

    void
    destroyPipelineLayout(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto pipelineLayout = reinterpret_cast<VkPipelineLayout>(arg1);
        // process destroy??
        vkDestroyPipelineLayout(device, pipelineLayout, nullptr);
        // unable to process void
    }

    jobjectArray
    enumerateDeviceExtensionProperties(JNIEnv *env, jlong arg0, jstring arg1) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        const char *vkarg1 = nullptr;
        if (arg1 != nullptr) {
            vkarg1 = env->GetStringUTFChars(arg1, nullptr);
            env->ReleaseStringUTFChars(arg1, vkarg1);
        }
        // process get??
        uint32_t count;
        vkEnumerateDeviceExtensionProperties(physicalDevice, vkarg1, &count, nullptr);
        std::vector<VkExtensionProperties> vkArray(count);
        vkEnumerateDeviceExtensionProperties(physicalDevice, vkarg1, &count, vkArray.data());
        jclass clazz = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkExtensionProperties");
        auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (int i = 0; i < count; ++i) {
            jobject obj = VkExtensionPropertiesMutator(env).toObject(vkArray[i]);
            env->SetObjectArrayElement(jArray, (jint) i, obj);
            env->DeleteLocalRef(obj);
        }
        return jArray;
    }

    jobject
    getPhysicalDeviceFeatures(JNIEnv *env, jlong arg0) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        // process get??
        VkPhysicalDeviceFeatures handle;
        vkGetPhysicalDeviceFeatures(physicalDevice, &handle);
        return VkPhysicalDeviceFeaturesMutator(env).toObject(handle);
    }

    void
    destroySemaphore(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto semaphore = reinterpret_cast<VkSemaphore>(arg1);
        // process destroy??
        vkDestroySemaphore(device, semaphore, nullptr);
        // unable to process void
    }

    void
    queuePresentKHR(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto queue = reinterpret_cast<VkQueue>(arg0);
        // object accessor?
        VkPresentInfoKHR info{};
        VkPresentInfoKHRAccessor(env, arg1).fromObject(info);
        // process default??
        VkResult result = vkQueuePresentKHR(queue, &info);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkQueuePresentKHR");
        }
        // unable to process void
    }

    void
    destroyImageView(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto imageView = reinterpret_cast<VkImageView>(arg1);
        // process destroy??
        vkDestroyImageView(device, imageView, nullptr);
        // unable to process void
    }

    void
    queueSubmit(JNIEnv *env, jlong arg0, jobjectArray arg1, jlong arg2) {
        // process parameter??
        auto queue = reinterpret_cast<VkQueue>(arg0);
        // process array
        auto size = env->GetArrayLength(arg1);
        std::vector<VkSubmitInfo> vkArray;
        for (int i = 0; i < size; ++i) {
            VkSubmitInfo info{};
            auto element = (jobject) env->GetObjectArrayElement(arg1, i);
            VkSubmitInfoAccessor(env, element).fromObject(info);
            vkArray.push_back(info);
            env->DeleteLocalRef(element);
        }
        auto fence = reinterpret_cast<VkFence>(arg2);
        // process default??
        VkResult result = vkQueueSubmit(queue, static_cast<uint32_t>(vkArray.size()),
                                        vkArray.data(), fence);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkQueueSubmit");
        }
        // unable to process void
    }

    jlong
    createSemaphore(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkSemaphoreCreateInfo info{};
        VkSemaphoreCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkSemaphore handle;
        VkResult result = vkCreateSemaphore(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateSemaphore");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jlong
    createFramebuffer(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkFramebufferCreateInfo info{};
        VkFramebufferCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkFramebuffer handle;
        VkResult result = vkCreateFramebuffer(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateFramebuffer");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jlong
    createRenderPass(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkRenderPassCreateInfo info{};
        VkRenderPassCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkRenderPass handle;
        VkResult result = vkCreateRenderPass(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateRenderPass");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jlongArray
    enumeratePhysicalDevices(JNIEnv *env, jlong arg0) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        // process get??
        uint32_t count;
        vkEnumeratePhysicalDevices(instance, &count, nullptr);
        std::vector<VkPhysicalDevice> vkArray(count);
        vkEnumeratePhysicalDevices(instance, &count, vkArray.data());
        auto jArray = env->NewLongArray(static_cast<jsize>(count));
        for (int i = 0; i < count; ++i) {
            auto value = reinterpret_cast<jlong>(vkArray[i]);
            env->SetLongArrayRegion(jArray, i, 1, &value);
        }
        return jArray;
    }

    jlong
    createDevice(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        // object accessor?
        VkDeviceCreateInfo info{};
        VkDeviceCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkDevice handle;
        VkResult result = vkCreateDevice(physicalDevice, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateDevice");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    destroySwapchainKHR(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto swapchainKHR = reinterpret_cast<VkSwapchainKHR>(arg1);
        // process destroy??
        vkDestroySwapchainKHR(device, swapchainKHR, nullptr);
        // unable to process void
    }

    jobjectArray
    enumerateInstanceExtensionProperties(JNIEnv *env, jstring arg0) {
        // process parameter??
        const char *vkarg0 = nullptr;
        if (arg0 != nullptr) {
            vkarg0 = env->GetStringUTFChars(arg0, nullptr);
            env->ReleaseStringUTFChars(arg0, vkarg0);
        }
        // process get??
        uint32_t count;
        vkEnumerateInstanceExtensionProperties(vkarg0, &count, nullptr);
        std::vector<VkExtensionProperties> vkArray(count);
        vkEnumerateInstanceExtensionProperties(vkarg0, &count, vkArray.data());
        jclass clazz = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkExtensionProperties");
        auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (int i = 0; i < count; ++i) {
            jobject obj = VkExtensionPropertiesMutator(env).toObject(vkArray[i]);
            env->SetObjectArrayElement(jArray, (jint) i, obj);
            env->DeleteLocalRef(obj);
        }
        return jArray;
    }

    jlong
    createSwapchainKHR(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkSwapchainCreateInfoKHR info{};
        VkSwapchainCreateInfoKHRAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkSwapchainKHR handle;
        VkResult result = vkCreateSwapchainKHR(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateSwapchainKHR");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    endCommandBuffer(JNIEnv *env, jlong arg0) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process default??
        VkResult result = vkEndCommandBuffer(commandBuffer);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkEndCommandBuffer");
        }
        // unable to process void
    }

    void
    destroyDebugUtilsMessengerEXT(jlong arg0, jlong arg1) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        auto debugUtilsMessengerEXT = reinterpret_cast<VkDebugUtilsMessengerEXT>(arg1);
        // process destroy??
        auto pfnvkDestroyDebugUtilsMessengerEXT = (PFN_vkDestroyDebugUtilsMessengerEXT) vkGetInstanceProcAddr(
                instance, "vkDestroyDebugUtilsMessengerEXT");
        pfnvkDestroyDebugUtilsMessengerEXT(instance, debugUtilsMessengerEXT, nullptr);
        // unable to process void
    }

    void
    destroyDevice(jlong arg0) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // process destroy??
        vkDestroyDevice(device, nullptr);
        // unable to process void
    }

    void
    cmdSetViewport(JNIEnv *env, jlong arg0, jint arg1, jobjectArray arg2) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        // process array
        auto size = env->GetArrayLength(arg2);
        std::vector<VkViewport> vkArray;
        for (int i = 0; i < size; ++i) {
            VkViewport info{};
            auto element = (jobject) env->GetObjectArrayElement(arg2, i);
            VkViewportAccessor(env, element).fromObject(info);
            vkArray.push_back(info);
            env->DeleteLocalRef(element);
        }
        // process cmd??
        vkCmdSetViewport(commandBuffer, vkarg1, static_cast<uint32_t>(vkArray.size()),
                         vkArray.data());
        // unable to process void
    }

    jlong
    createImageView(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkImageViewCreateInfo info{};
        VkImageViewCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkImageView handle;
        VkResult result = vkCreateImageView(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateImageView");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    destroyInstance(jlong arg0) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        // process destroy??
        vkDestroyInstance(instance, nullptr);
        // unable to process void
    }

    void
    resetFences(JNIEnv *env, jlong arg0, jlongArray arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // process array
        auto size = env->GetArrayLength(arg1);
        std::vector<VkFence> vkArray;
        for (int i = 0; i < size; ++i) {
            jlong element;
            env->GetLongArrayRegion(arg1, i, 1, &element);
            auto info = reinterpret_cast<VkFence>(element);
            vkArray.push_back(info);
        }
        // process cmd??
        vkResetFences(device, static_cast<uint32_t>(vkArray.size()), vkArray.data());
        // unable to process void
    }

    jlong
    createPipelineLayout(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkPipelineLayoutCreateInfo info{};
        VkPipelineLayoutCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkPipelineLayout handle;
        VkResult result = vkCreatePipelineLayout(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreatePipelineLayout");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    destroyShaderModule(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto shaderModule = reinterpret_cast<VkShaderModule>(arg1);
        // process destroy??
        vkDestroyShaderModule(device, shaderModule, nullptr);
        // unable to process void
    }

    jlongArray
    getSwapchainImagesKHR(JNIEnv *env, jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto swapchainKHR = reinterpret_cast<VkSwapchainKHR>(arg1);
        // process get??
        uint32_t count;
        vkGetSwapchainImagesKHR(device, swapchainKHR, &count, nullptr);
        std::vector<VkImage> vkArray(count);
        vkGetSwapchainImagesKHR(device, swapchainKHR, &count, vkArray.data());
        auto jArray = env->NewLongArray(static_cast<jsize>(count));
        for (int i = 0; i < count; ++i) {
            auto value = reinterpret_cast<jlong>(vkArray[i]);
            env->SetLongArrayRegion(jArray, i, 1, &value);
        }
        return jArray;
    }

    jlong
    createFence(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkFenceCreateInfo info{};
        VkFenceCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkFence handle;
        VkResult result = vkCreateFence(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateFence");
        }
        return reinterpret_cast<jlong>(handle);
    }

#ifdef __ANDROID__
    jlong
    createAndroidSurfaceKHR(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto instance = reinterpret_cast<VkInstance>(arg0);
        // object accessor?
        VkAndroidSurfaceCreateInfoKHR info{};
        VkAndroidSurfaceCreateInfoKHRAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkSurfaceKHR handle;
        VkResult result = vkCreateAndroidSurfaceKHR(instance, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateAndroidSurfaceKHR");
        }
        return reinterpret_cast<jlong>(handle);
    }
#endif // __ANDROID__

    jobjectArray
    enumerateInstanceLayerProperties(JNIEnv *env) {
        // process parameter??
        // process get??
        uint32_t count;
        vkEnumerateInstanceLayerProperties(&count, nullptr);
        std::vector<VkLayerProperties> vkArray(count);
        vkEnumerateInstanceLayerProperties(&count, vkArray.data());
        jclass clazz = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkLayerProperties");
        auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (int i = 0; i < count; ++i) {
            jobject obj = VkLayerPropertiesMutator(env).toObject(vkArray[i]);
            env->SetObjectArrayElement(jArray, (jint) i, obj);
            env->DeleteLocalRef(obj);
        }
        return jArray;
    }

    jlong
    createInstance(JNIEnv *env, jobject arg0) {
        // process parameter??
        // object accessor?
        VkInstanceCreateInfo info{};
        VkInstanceCreateInfoAccessor(env, arg0).fromObject(info);
        // process create??
        // process handle
        VkInstance handle;
        VkResult result = vkCreateInstance(&info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateInstance");
        }
        return reinterpret_cast<jlong>(handle);
    }

    void
    waitForFences(JNIEnv *env, jlong arg0, jlongArray arg1, jboolean arg2, jlong arg3) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // process array
        auto size = env->GetArrayLength(arg1);
        std::vector<VkFence> vkArray;
        for (int i = 0; i < size; ++i) {
            jlong element;
            env->GetLongArrayRegion(arg1, i, 1, &element);
            auto info = reinterpret_cast<VkFence>(element);
            vkArray.push_back(info);
        }
        // process primitive  boolean??
        VkBool32 vkarg2 = arg2;
        // process primitive  long??
        uint64_t vkarg3 = arg3;
        // process cmd??
        vkWaitForFences(device, static_cast<uint32_t>(vkArray.size()), vkArray.data(), vkarg2,
                        vkarg3);
        // unable to process void
    }

    void
    cmdSetScissor(JNIEnv *env, jlong arg0, jint arg1, jobjectArray arg2) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        // process array
        auto size = env->GetArrayLength(arg2);
        std::vector<VkRect2D> vkArray;
        for (int i = 0; i < size; ++i) {
            VkRect2D info{};
            auto element = (jobject) env->GetObjectArrayElement(arg2, i);
            VkRect2DAccessor(env, element).fromObject(info);
            vkArray.push_back(info);
            env->DeleteLocalRef(element);
        }
        // process cmd??
        vkCmdSetScissor(commandBuffer, vkarg1, static_cast<uint32_t>(vkArray.size()),
                        vkArray.data());
        // unable to process void
    }

    void
    destroyFence(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto fence = reinterpret_cast<VkFence>(arg1);
        // process destroy??
        vkDestroyFence(device, fence, nullptr);
        // unable to process void
    }

    void
    beginCommandBuffer(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // object accessor?
        VkCommandBufferBeginInfo info{};
        VkCommandBufferBeginInfoAccessor(env, arg1).fromObject(info);
        // process default??
        VkResult result = vkBeginCommandBuffer(commandBuffer, &info);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkBeginCommandBuffer");
        }
        // unable to process void
    }

    void
    destroyRenderPass(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto renderPass = reinterpret_cast<VkRenderPass>(arg1);
        // process destroy??
        vkDestroyRenderPass(device, renderPass, nullptr);
        // unable to process void
    }

    jint
    acquireNextImageKHR(JNIEnv *env, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto swapchainKHR = reinterpret_cast<VkSwapchainKHR>(arg1);
        // process primitive  long??
        uint64_t vkarg2 = arg2;
        auto semaphore = reinterpret_cast<VkSemaphore>(arg3);
        auto fence = reinterpret_cast<VkFence>(arg4);
        // process default??
        uint32_t handle;
        VkResult result = vkAcquireNextImageKHR(device, swapchainKHR, vkarg2, semaphore, fence,
                                                &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkAcquireNextImageKHR");
        }
        return static_cast<jint>(handle);
    }

    void
    resetCommandBuffer(JNIEnv *env, jlong arg0, jint arg1) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        // process cmd??
        vkResetCommandBuffer(commandBuffer, vkarg1);
        // unable to process void
    }

    jlong
    createShaderModule(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkShaderModuleCreateInfo info{};
        VkShaderModuleCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkShaderModule handle;
        VkResult result = vkCreateShaderModule(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateShaderModule");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jboolean
    getPhysicalDeviceSurfaceSupportKHR(JNIEnv *env, jlong arg0, jint arg1, jlong arg2) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        auto surfaceKHR = reinterpret_cast<VkSurfaceKHR>(arg2);
        // process get??
        VkBool32 handle;
        vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, vkarg1, surfaceKHR, &handle);
        return static_cast<jboolean>(handle);
    }

    jlong
    createPipelineCache(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkPipelineCacheCreateInfo info{};
        VkPipelineCacheCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkPipelineCache handle;
        VkResult result = vkCreatePipelineCache(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreatePipelineCache");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jobjectArray
    getPhysicalDeviceSurfacePresentModesKHR(JNIEnv *env, jlong arg0, jlong arg1) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        auto surfaceKHR = reinterpret_cast<VkSurfaceKHR>(arg1);
        // process get??
        uint32_t count;
        vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surfaceKHR, &count, nullptr);
        std::vector<VkPresentModeKHR> vkArray(count);
        vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surfaceKHR, &count,
                                                  vkArray.data());
        jclass clazz = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/enums/VkPresentModeKHR");
        auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (int i = 0; i < count; ++i) {
            jobject obj = enum_utils::setEnumFromVulkan(env, vkArray[i],
                                                        "io/github/ronjunevaldoz/awake/vulkan/enums/VkPresentModeKHR");
            env->SetObjectArrayElement(jArray, (jint) i, obj);
            env->DeleteLocalRef(obj);
        }
        return jArray;
    }

    void
    destroyCommandPool(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto commandPool = reinterpret_cast<VkCommandPool>(arg1);
        // process destroy??
        vkDestroyCommandPool(device, commandPool, nullptr);
        // unable to process void
    }

    void
    cmdDraw(JNIEnv *env, jlong arg0, jint arg1, jint arg2, jint arg3, jint arg4) {
        // process parameter??
        auto commandBuffer = reinterpret_cast<VkCommandBuffer>(arg0);
        // process primitive  int??
        uint32_t vkarg1 = arg1;
        // process primitive  int??
        uint32_t vkarg2 = arg2;
        // process primitive  int??
        uint32_t vkarg3 = arg3;
        // process primitive  int??
        uint32_t vkarg4 = arg4;
        // process cmd??
        vkCmdDraw(commandBuffer, vkarg1, vkarg2, vkarg3, vkarg4);
        // unable to process void
    }

    void
    destroyPipeline(jlong arg0, jlong arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        auto pipeline = reinterpret_cast<VkPipeline>(arg1);
        // process destroy??
        vkDestroyPipeline(device, pipeline, nullptr);
        // unable to process void
    }

    jlong
    createCommandPool(JNIEnv *env, jlong arg0, jobject arg1) {
        // process parameter??
        auto device = reinterpret_cast<VkDevice>(arg0);
        // object accessor?
        VkCommandPoolCreateInfo info{};
        VkCommandPoolCreateInfoAccessor(env, arg1).fromObject(info);
        // process create??
        // process handle
        VkCommandPool handle;
        VkResult result = vkCreateCommandPool(device, &info, nullptr, &handle);
        if (result != VK_SUCCESS) {
            exception_utils::resultException(env, result,
                                             "There was a problem executing vkCreateCommandPool");
        }
        return reinterpret_cast<jlong>(handle);
    }

    jobjectArray
    getPhysicalDeviceSurfaceFormatsKHR(JNIEnv *env, jlong arg0, jlong arg1) {
        // process parameter??
        auto physicalDevice = reinterpret_cast<VkPhysicalDevice>(arg0);
        auto surfaceKHR = reinterpret_cast<VkSurfaceKHR>(arg1);
        // process get??
        uint32_t count;
        vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surfaceKHR, &count, nullptr);
        std::vector<VkSurfaceFormatKHR> vkArray(count);
        vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surfaceKHR, &count, vkArray.data());
        jclass clazz = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkSurfaceFormatKHR");
        auto jArray = env->NewObjectArray(static_cast<jsize>(count), clazz, nullptr);
        env->DeleteLocalRef(clazz);
        for (int i = 0; i < count; ++i) {
            jobject obj = VkSurfaceFormatKHRMutator(env).toObject(vkArray[i]);
            env->SetObjectArrayElement(jArray, (jint) i, obj);
            env->DeleteLocalRef(obj);
        }
        return jArray;
    }

}
