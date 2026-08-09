/*
 *  VkSurfaceFormatKHRMutator.cpp
 *  Vulkan mutator for VkSurfaceFormatKHR
 *  Created by Ron June Valdoz */

#include <includes/VkSurfaceFormatKHRMutator.h>

VkSurfaceFormatKHRMutator::VkSurfaceFormatKHRMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkSurfaceFormatKHR");
    formatField = env->GetFieldID(clazz, "format",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    colorSpaceField = env->GetFieldID(clazz, "colorSpace",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkColorSpaceKHR;");
}

jobject
VkSurfaceFormatKHRMutator::toObject(VkSurfaceFormatKHR source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    auto format = enum_utils::setEnumFromVulkan(env, static_cast<jint>(source.format),
                                                "io/github/ronjunevaldoz/awake/vulkan/enums/VkFormat");
    env->SetObjectField(newObj, formatField, format);
    env->DeleteLocalRef(format);
    auto colorSpace = enum_utils::setEnumFromVulkan(env, static_cast<jint>(source.colorSpace),
                                                    "io/github/ronjunevaldoz/awake/vulkan/enums/VkColorSpaceKHR");
    env->SetObjectField(newObj, colorSpaceField, colorSpace);
    env->DeleteLocalRef(colorSpace);
    return newObj;
}

VkSurfaceFormatKHRMutator::~VkSurfaceFormatKHRMutator() {
    // env->DeleteGlobalRef(clazz);
}

