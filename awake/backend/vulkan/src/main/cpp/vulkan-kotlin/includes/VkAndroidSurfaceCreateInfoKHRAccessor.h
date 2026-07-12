/*
 *  VkAndroidSurfaceCreateInfoKHRAccessor.h
 *  Vulkan accessor for VkAndroidSurfaceCreateInfoKHR
 *  Created by Ron June Valdoz */

#ifndef VKANDROIDSURFACECREATEINFOKHRACCESSOR_H
#define VKANDROIDSURFACECREATEINFOKHRACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <android/native_window_jni.h>

class VkAndroidSurfaceCreateInfoKHRAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID windowField;
public:
    VkAndroidSurfaceCreateInfoKHRAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void fromObject(VkAndroidSurfaceCreateInfoKHR &clazzInfo);

    void getwindow(VkAndroidSurfaceCreateInfoKHR &clazzInfo);

    void getpNext(VkAndroidSurfaceCreateInfoKHR &clazzInfo);

    ~VkAndroidSurfaceCreateInfoKHRAccessor();
};
#endif // VKANDROIDSURFACECREATEINFOKHRACCESSOR_H