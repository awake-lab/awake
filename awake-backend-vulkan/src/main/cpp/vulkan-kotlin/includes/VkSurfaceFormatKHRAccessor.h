/*
 *  VkSurfaceFormatKHRAccessor.h
 *  Vulkan accessor for VkSurfaceFormatKHR
 *  Created by Ron June Valdoz */

#ifndef VKSURFACEFORMATKHRACCESSOR_H
#define VKSURFACEFORMATKHRACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSurfaceFormatKHRAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID formatField;
    jfieldID colorSpaceField;
public:
    VkSurfaceFormatKHRAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkSurfaceFormatKHR &clazzInfo);

    VkColorSpaceKHR getcolorSpace();

    VkFormat getformat();

    ~VkSurfaceFormatKHRAccessor();
};
#endif // VKSURFACEFORMATKHRACCESSOR_H