/*
 *  VkDeviceCreateInfoAccessor.h
 *  Vulkan accessor for VkDeviceCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKDEVICECREATEINFOACCESSOR_H
#define VKDEVICECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkDeviceQueueCreateInfoAccessor.h>
#include <includes/VkPhysicalDeviceFeaturesAccessor.h>

class VkDeviceCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pQueueCreateInfosField;
    jfieldID ppEnabledLayerNamesField;
    jfieldID ppEnabledExtensionNamesField;
    jfieldID pEnabledFeaturesField;
public:
    VkDeviceCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void fromObject(VkDeviceCreateInfo &clazzInfo);

    void getpNext(VkDeviceCreateInfo &clazzInfo);

    void getppEnabledLayerNames(VkDeviceCreateInfo &clazzInfo);

    void getppEnabledExtensionNames(VkDeviceCreateInfo &clazzInfo);

    void getpEnabledFeatures(VkDeviceCreateInfo &clazzInfo);

    void getpQueueCreateInfos(VkDeviceCreateInfo &clazzInfo);

    ~VkDeviceCreateInfoAccessor();
};
#endif // VKDEVICECREATEINFOACCESSOR_H