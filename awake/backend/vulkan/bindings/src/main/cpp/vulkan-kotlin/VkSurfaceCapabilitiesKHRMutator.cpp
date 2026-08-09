/*
 *  VkSurfaceCapabilitiesKHRMutator.cpp
 *  Vulkan mutator for VkSurfaceCapabilitiesKHR
 *  Created by Ron June Valdoz */

#include <includes/VkSurfaceCapabilitiesKHRMutator.h>

VkSurfaceCapabilitiesKHRMutator::VkSurfaceCapabilitiesKHRMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkSurfaceCapabilitiesKHR");
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

jobject
VkSurfaceCapabilitiesKHRMutator::toObject(VkSurfaceCapabilitiesKHR source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetIntField(newObj, minImageCountField, static_cast<jint>(source.minImageCount));
    env->SetIntField(newObj, maxImageCountField, static_cast<jint>(source.maxImageCount));
    VkExtent2DMutator currentExtentMutator(env);
    auto currentExtent = currentExtentMutator.toObject(source.currentExtent);
    env->SetObjectField(newObj, currentExtentField, currentExtent);
    env->DeleteLocalRef(currentExtent);
    VkExtent2DMutator minImageExtentMutator(env);
    auto minImageExtent = minImageExtentMutator.toObject(source.minImageExtent);
    env->SetObjectField(newObj, minImageExtentField, minImageExtent);
    env->DeleteLocalRef(minImageExtent);
    VkExtent2DMutator maxImageExtentMutator(env);
    auto maxImageExtent = maxImageExtentMutator.toObject(source.maxImageExtent);
    env->SetObjectField(newObj, maxImageExtentField, maxImageExtent);
    env->DeleteLocalRef(maxImageExtent);
    env->SetIntField(newObj, maxImageArrayLayersField,
                     static_cast<jint>(source.maxImageArrayLayers));
    env->SetIntField(newObj, supportedTransformsField,
                     static_cast<jint>(source.supportedTransforms));
    auto currentTransform = enum_utils::setEnumFromVulkan(env,
                                                          static_cast<jint>(source.currentTransform),
                                                          "io/github/ronjunevaldoz/awake/vulkan/enums/VkSurfaceTransformFlagBitsKHR");
    env->SetObjectField(newObj, currentTransformField, currentTransform);
    env->DeleteLocalRef(currentTransform);
    env->SetIntField(newObj, supportedCompositeAlphaField,
                     static_cast<jint>(source.supportedCompositeAlpha));
    env->SetIntField(newObj, supportedUsageFlagsField,
                     static_cast<jint>(source.supportedUsageFlags));
    return newObj;
}

VkSurfaceCapabilitiesKHRMutator::~VkSurfaceCapabilitiesKHRMutator() {
    // env->DeleteGlobalRef(clazz);
}

