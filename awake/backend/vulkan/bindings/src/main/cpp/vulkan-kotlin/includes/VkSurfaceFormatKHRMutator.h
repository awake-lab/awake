/*
 *  VkSurfaceFormatKHRMutator.h
 *  Vulkan mutator for VkSurfaceFormatKHR
 *  Created by Ron June Valdoz */

#ifndef VKSURFACEFORMATKHRMUTATOR_H
#define VKSURFACEFORMATKHRMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSurfaceFormatKHRMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID formatField;
    jfieldID colorSpaceField;
public:
    VkSurfaceFormatKHRMutator(JNIEnv *env);

    jobject toObject(VkSurfaceFormatKHR source);
    ~VkSurfaceFormatKHRMutator();
};
#endif // VKSURFACEFORMATKHRMUTATOR_H