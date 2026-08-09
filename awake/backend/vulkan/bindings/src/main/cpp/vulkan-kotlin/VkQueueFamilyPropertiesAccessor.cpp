/*
 *  VkQueueFamilyPropertiesAccessor.cpp
 *  Vulkan accessor for VkQueueFamilyProperties
 *  Created by Ron June Valdoz */

#include <includes/VkQueueFamilyPropertiesAccessor.h>

VkQueueFamilyPropertiesAccessor::VkQueueFamilyPropertiesAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    queueFlagsField = env->GetFieldID(clazz, "queueFlags", "I");
    queueCountField = env->GetFieldID(clazz, "queueCount", "I");
    timestampValidBitsField = env->GetFieldID(clazz, "timestampValidBits", "I");
    minImageTransferGranularityField = env->GetFieldID(clazz, "minImageTransferGranularity",
                                                       "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent3D;");
}

uint32_t
VkQueueFamilyPropertiesAccessor::getqueueCount() {
    return (uint32_t) (jint) env->GetIntField(obj, queueCountField); // primitive
}

void
VkQueueFamilyPropertiesAccessor::fromObject(VkQueueFamilyProperties &clazzInfo) {
    clazzInfo.queueFlags = getqueueFlags(); // Primitive uint32_t
    clazzInfo.queueCount = getqueueCount(); // Primitive uint32_t
    clazzInfo.timestampValidBits = gettimestampValidBits(); // Primitive uint32_t
    getminImageTransferGranularity(clazzInfo); // Other VkExtent3D
}

uint32_t
VkQueueFamilyPropertiesAccessor::getqueueFlags() {
    return (uint32_t) (jint) env->GetIntField(obj, queueFlagsField); // primitive
}

void
VkQueueFamilyPropertiesAccessor::getminImageTransferGranularity(
        VkQueueFamilyProperties &clazzInfo) {
    auto minImageTransferGranularityObj = (jobject) env->GetObjectField(obj,
                                                                        minImageTransferGranularityField);
    if (minImageTransferGranularityObj == nullptr) {
        env->DeleteLocalRef(minImageTransferGranularityObj); // Delete null object reference
        return;
    }
    VkExtent3DAccessor accessor(env, minImageTransferGranularityObj);
    VkExtent3D ref{};
    accessor.fromObject(ref);
    clazzInfo.minImageTransferGranularity = ref;
    env->DeleteLocalRef(minImageTransferGranularityObj); // Delete object reference
}

uint32_t
VkQueueFamilyPropertiesAccessor::gettimestampValidBits() {
    return (uint32_t) (jint) env->GetIntField(obj, timestampValidBitsField); // primitive
}

VkQueueFamilyPropertiesAccessor::~VkQueueFamilyPropertiesAccessor() {
    env->DeleteLocalRef(clazz);
}

