/*
 *  VkSurfaceFormatKHRAccessor.cpp
 *  Vulkan accessor for VkSurfaceFormatKHR
 *  Created by Ron June Valdoz */

#include <includes/VkSurfaceFormatKHRAccessor.h>

VkSurfaceFormatKHRAccessor::VkSurfaceFormatKHRAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                   obj(obj) {
    clazz = env->GetObjectClass(obj);
    formatField = env->GetFieldID(clazz, "format",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    colorSpaceField = env->GetFieldID(clazz, "colorSpace",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkColorSpaceKHR;");
}

void
VkSurfaceFormatKHRAccessor::fromObject(VkSurfaceFormatKHR &clazzInfo) {
    clazzInfo.format = getformat(); // Enum VkFormat
    clazzInfo.colorSpace = getcolorSpace(); // Enum VkColorSpaceKHR
}

VkColorSpaceKHR
VkSurfaceFormatKHRAccessor::getcolorSpace() {
    auto colorSpaceEnum = (jobject) env->GetObjectField(obj, colorSpaceField);
    auto enumValue = (VkColorSpaceKHR) enum_utils::getEnumFromObject(env, colorSpaceEnum);
    env->DeleteLocalRef(colorSpaceEnum); // release enum reference
    return enumValue;
}

VkFormat
VkSurfaceFormatKHRAccessor::getformat() {
    auto formatEnum = (jobject) env->GetObjectField(obj, formatField);
    auto enumValue = (VkFormat) enum_utils::getEnumFromObject(env, formatEnum);
    env->DeleteLocalRef(formatEnum); // release enum reference
    return enumValue;
}

VkSurfaceFormatKHRAccessor::~VkSurfaceFormatKHRAccessor() {
    env->DeleteLocalRef(clazz);
}

