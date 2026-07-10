/*
 *  VkClearColorValueAccessor.cpp
 *  
 *  Created by Ron June Valdoz */

#include <includes/VkClearColorValueAccessor.h>

VkClearColorValueAccessor::VkClearColorValueAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                 obj(obj) {
}

void
VkClearColorValueAccessor::fromObject(VkClearColorValue &info) {
    auto Float32 = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/VkClearColorValue$Float32");
    if (env->IsInstanceOf(obj, Float32)) {
        Float32Accessor(env, obj).fromObject(info);
    }
    env->DeleteLocalRef(Float32);
    auto Int32 = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/VkClearColorValue$Int32");
    if (env->IsInstanceOf(obj, Int32)) {
        Int32Accessor(env, obj).fromObject(info);
    }
    env->DeleteLocalRef(Int32);
    auto UInt32 = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/VkClearColorValue$UInt32");
    if (env->IsInstanceOf(obj, UInt32)) {
        UInt32Accessor(env, obj).fromObject(info);
    }
    env->DeleteLocalRef(UInt32);
}

