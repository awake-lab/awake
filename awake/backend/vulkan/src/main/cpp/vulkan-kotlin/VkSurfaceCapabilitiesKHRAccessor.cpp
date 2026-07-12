/*
 *  VkSurfaceCapabilitiesKHRAccessor.cpp
 *  Vulkan accessor for VkSurfaceCapabilitiesKHR
 *  Created by Ron June Valdoz */

#include <includes/VkSurfaceCapabilitiesKHRAccessor.h>

VkSurfaceCapabilitiesKHRAccessor::VkSurfaceCapabilitiesKHRAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    minImageCountField = env->GetFieldID(clazz, "minImageCount", "I");
    maxImageCountField = env->GetFieldID(clazz, "maxImageCount", "I");
    currentExtentField = env->GetFieldID(clazz, "currentExtent",
                                         "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D;");
    minImageExtentField = env->GetFieldID(clazz, "minImageExtent",
                                          "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D;");
    maxImageExtentField = env->GetFieldID(clazz, "maxImageExtent",
                                          "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D;");
    maxImageArrayLayersField = env->GetFieldID(clazz, "maxImageArrayLayers", "I");
    supportedTransformsField = env->GetFieldID(clazz, "supportedTransforms", "I");
    currentTransformField = env->GetFieldID(clazz, "currentTransform",
                                            "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkSurfaceTransformFlagBitsKHR;");
    supportedCompositeAlphaField = env->GetFieldID(clazz, "supportedCompositeAlpha", "I");
    supportedUsageFlagsField = env->GetFieldID(clazz, "supportedUsageFlags", "I");
}

void
VkSurfaceCapabilitiesKHRAccessor::getcurrentExtent(VkSurfaceCapabilitiesKHR &clazzInfo) {
    auto currentExtentObj = (jobject) env->GetObjectField(obj, currentExtentField);
    if (currentExtentObj == nullptr) {
        env->DeleteLocalRef(currentExtentObj); // Delete null object reference
        return;
    }
    VkExtent2DAccessor accessor(env, currentExtentObj);
    VkExtent2D ref{};
    accessor.fromObject(ref);
    clazzInfo.currentExtent = ref;
    env->DeleteLocalRef(currentExtentObj); // Delete object reference
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getmaxImageCount() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageCountField); // primitive
}

void
VkSurfaceCapabilitiesKHRAccessor::getminImageExtent(VkSurfaceCapabilitiesKHR &clazzInfo) {
    auto minImageExtentObj = (jobject) env->GetObjectField(obj, minImageExtentField);
    if (minImageExtentObj == nullptr) {
        env->DeleteLocalRef(minImageExtentObj); // Delete null object reference
        return;
    }
    VkExtent2DAccessor accessor(env, minImageExtentObj);
    VkExtent2D ref{};
    accessor.fromObject(ref);
    clazzInfo.minImageExtent = ref;
    env->DeleteLocalRef(minImageExtentObj); // Delete object reference
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getsupportedCompositeAlpha() {
    return (uint32_t) (jint) env->GetIntField(obj, supportedCompositeAlphaField); // primitive
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getsupportedTransforms() {
    return (uint32_t) (jint) env->GetIntField(obj, supportedTransformsField); // primitive
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getmaxImageArrayLayers() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageArrayLayersField); // primitive
}

VkSurfaceTransformFlagBitsKHR
VkSurfaceCapabilitiesKHRAccessor::getcurrentTransform() {
    auto currentTransformEnum = (jobject) env->GetObjectField(obj, currentTransformField);
    auto enumValue = (VkSurfaceTransformFlagBitsKHR) enum_utils::getEnumFromObject(env,
                                                                                   currentTransformEnum);
    env->DeleteLocalRef(currentTransformEnum); // release enum reference
    return enumValue;
}

void
VkSurfaceCapabilitiesKHRAccessor::getmaxImageExtent(VkSurfaceCapabilitiesKHR &clazzInfo) {
    auto maxImageExtentObj = (jobject) env->GetObjectField(obj, maxImageExtentField);
    if (maxImageExtentObj == nullptr) {
        env->DeleteLocalRef(maxImageExtentObj); // Delete null object reference
        return;
    }
    VkExtent2DAccessor accessor(env, maxImageExtentObj);
    VkExtent2D ref{};
    accessor.fromObject(ref);
    clazzInfo.maxImageExtent = ref;
    env->DeleteLocalRef(maxImageExtentObj); // Delete object reference
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getminImageCount() {
    return (uint32_t) (jint) env->GetIntField(obj, minImageCountField); // primitive
}

uint32_t
VkSurfaceCapabilitiesKHRAccessor::getsupportedUsageFlags() {
    return (uint32_t) (jint) env->GetIntField(obj, supportedUsageFlagsField); // primitive
}

void
VkSurfaceCapabilitiesKHRAccessor::fromObject(VkSurfaceCapabilitiesKHR &clazzInfo) {
    clazzInfo.minImageCount = getminImageCount(); // Primitive uint32_t
    clazzInfo.maxImageCount = getmaxImageCount(); // Primitive uint32_t
    getcurrentExtent(clazzInfo); // Other VkExtent2D
    getminImageExtent(clazzInfo); // Other VkExtent2D
    getmaxImageExtent(clazzInfo); // Other VkExtent2D
    clazzInfo.maxImageArrayLayers = getmaxImageArrayLayers(); // Primitive uint32_t
    clazzInfo.supportedTransforms = getsupportedTransforms(); // Primitive uint32_t
    clazzInfo.currentTransform = getcurrentTransform(); // Enum VkSurfaceTransformFlagBitsKHR
    clazzInfo.supportedCompositeAlpha = getsupportedCompositeAlpha(); // Primitive uint32_t
    clazzInfo.supportedUsageFlags = getsupportedUsageFlags(); // Primitive uint32_t
}

VkSurfaceCapabilitiesKHRAccessor::~VkSurfaceCapabilitiesKHRAccessor() {
    env->DeleteLocalRef(clazz);
}

