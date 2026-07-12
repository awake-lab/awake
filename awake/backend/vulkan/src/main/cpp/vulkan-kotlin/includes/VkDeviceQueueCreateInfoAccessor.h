/*
 *  VkDeviceQueueCreateInfoAccessor.h
 *  Vulkan accessor for VkDeviceQueueCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKDEVICEQUEUECREATEINFOACCESSOR_H
#define VKDEVICEQUEUECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDeviceQueueCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID queueFamilyIndexField;
    jfieldID queueCountField;
    jfieldID pQueuePrioritiesField;
public:
    VkDeviceQueueCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getqueueFamilyIndex();

    uint32_t getflags();

    void getpQueuePriorities(VkDeviceQueueCreateInfo &clazzInfo);

    uint32_t getqueueCount();

    void fromObject(VkDeviceQueueCreateInfo &clazzInfo);

    void getpNext(VkDeviceQueueCreateInfo &clazzInfo);

    ~VkDeviceQueueCreateInfoAccessor();
};
#endif // VKDEVICEQUEUECREATEINFOACCESSOR_H