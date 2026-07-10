/*
 *  VkSemaphoreCreateInfoAccessor.h
 *  Vulkan accessor for VkSemaphoreCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKSEMAPHORECREATEINFOACCESSOR_H
#define VKSEMAPHORECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSemaphoreCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
public:
    VkSemaphoreCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkSemaphoreCreateInfo &clazzInfo);

    void fromObject(VkSemaphoreCreateInfo &clazzInfo);

    ~VkSemaphoreCreateInfoAccessor();
};
#endif // VKSEMAPHORECREATEINFOACCESSOR_H