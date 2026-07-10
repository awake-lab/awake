/*
 *  VkQueueFamilyPropertiesMutator.cpp
 *  Vulkan mutator for VkQueueFamilyProperties
 *  Created by Ron June Valdoz */

#include <includes/VkQueueFamilyPropertiesMutator.h>

VkQueueFamilyPropertiesMutator::VkQueueFamilyPropertiesMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkQueueFamilyProperties");
    queueFlagsField = env->GetFieldID(clazz, "queueFlags", "I");
    queueCountField = env->GetFieldID(clazz, "queueCount", "I");
    timestampValidBitsField = env->GetFieldID(clazz, "timestampValidBits", "I");
    minImageTransferGranularityField = env->GetFieldID(clazz, "minImageTransferGranularity",
                                                       "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent3D;");
}

jobject
VkQueueFamilyPropertiesMutator::toObject(VkQueueFamilyProperties source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetIntField(newObj, queueFlagsField, static_cast<jint>(source.queueFlags));
    env->SetIntField(newObj, queueCountField, static_cast<jint>(source.queueCount));
    env->SetIntField(newObj, timestampValidBitsField, static_cast<jint>(source.timestampValidBits));
    VkExtent3DMutator minImageTransferGranularityMutator(env);
    auto minImageTransferGranularity = minImageTransferGranularityMutator.toObject(
            source.minImageTransferGranularity);
    env->SetObjectField(newObj, minImageTransferGranularityField, minImageTransferGranularity);
    env->DeleteLocalRef(minImageTransferGranularity);
    return newObj;
}

VkQueueFamilyPropertiesMutator::~VkQueueFamilyPropertiesMutator() {
    // env->DeleteGlobalRef(clazz);
}

