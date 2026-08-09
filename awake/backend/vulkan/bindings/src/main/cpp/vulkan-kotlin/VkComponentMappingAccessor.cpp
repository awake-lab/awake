/*
 *  VkComponentMappingAccessor.cpp
 *  Vulkan accessor for VkComponentMapping
 *  Created by Ron June Valdoz */

#include <includes/VkComponentMappingAccessor.h>

VkComponentMappingAccessor::VkComponentMappingAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                   obj(obj) {
    clazz = env->GetObjectClass(obj);
    rField = env->GetFieldID(clazz, "r",
                             "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkComponentSwizzle;");
    gField = env->GetFieldID(clazz, "g",
                             "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkComponentSwizzle;");
    bField = env->GetFieldID(clazz, "b",
                             "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkComponentSwizzle;");
    aField = env->GetFieldID(clazz, "a",
                             "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkComponentSwizzle;");
}

VkComponentSwizzle
VkComponentMappingAccessor::getr() {
    auto rEnum = (jobject) env->GetObjectField(obj, rField);
    auto enumValue = (VkComponentSwizzle) enum_utils::getEnumFromObject(env, rEnum);
    env->DeleteLocalRef(rEnum); // release enum reference
    return enumValue;
}

void
VkComponentMappingAccessor::fromObject(VkComponentMapping &clazzInfo) {
    clazzInfo.r = getr(); // Enum VkComponentSwizzle
    clazzInfo.g = getg(); // Enum VkComponentSwizzle
    clazzInfo.b = getb(); // Enum VkComponentSwizzle
    clazzInfo.a = geta(); // Enum VkComponentSwizzle
}

VkComponentSwizzle
VkComponentMappingAccessor::getb() {
    auto bEnum = (jobject) env->GetObjectField(obj, bField);
    auto enumValue = (VkComponentSwizzle) enum_utils::getEnumFromObject(env, bEnum);
    env->DeleteLocalRef(bEnum); // release enum reference
    return enumValue;
}

VkComponentSwizzle
VkComponentMappingAccessor::geta() {
    auto aEnum = (jobject) env->GetObjectField(obj, aField);
    auto enumValue = (VkComponentSwizzle) enum_utils::getEnumFromObject(env, aEnum);
    env->DeleteLocalRef(aEnum); // release enum reference
    return enumValue;
}

VkComponentSwizzle
VkComponentMappingAccessor::getg() {
    auto gEnum = (jobject) env->GetObjectField(obj, gField);
    auto enumValue = (VkComponentSwizzle) enum_utils::getEnumFromObject(env, gEnum);
    env->DeleteLocalRef(gEnum); // release enum reference
    return enumValue;
}

VkComponentMappingAccessor::~VkComponentMappingAccessor() {
    env->DeleteLocalRef(clazz);
}

