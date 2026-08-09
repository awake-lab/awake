/*
 *  VkQueueFamilyPropertiesAccessor.h
 *  Vulkan accessor for VkQueueFamilyProperties
 *  Created by Ron June Valdoz */

#ifndef VKQUEUEFAMILYPROPERTIESACCESSOR_H
#define VKQUEUEFAMILYPROPERTIESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkExtent3DAccessor.h>

class VkQueueFamilyPropertiesAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID queueFlagsField;
    jfieldID queueCountField;
    jfieldID timestampValidBitsField;
    jfieldID minImageTransferGranularityField;
public:
    VkQueueFamilyPropertiesAccessor(JNIEnv *env, jobject obj);

    uint32_t getqueueCount();

    void fromObject(VkQueueFamilyProperties &clazzInfo);

    uint32_t getqueueFlags();

    void getminImageTransferGranularity(VkQueueFamilyProperties &clazzInfo);

    uint32_t gettimestampValidBits();
    ~VkQueueFamilyPropertiesAccessor();
};
#endif // VKQUEUEFAMILYPROPERTIESACCESSOR_H