/*
 *  VkPhysicalDevicePropertiesAccessor.h
 *  Vulkan accessor for VkPhysicalDeviceProperties
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICEPROPERTIESACCESSOR_H
#define VKPHYSICALDEVICEPROPERTIESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkPhysicalDeviceLimitsAccessor.h>
#include <includes/VkPhysicalDeviceSparsePropertiesAccessor.h>

class VkPhysicalDevicePropertiesAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID apiVersionField;
    jfieldID driverVersionField;
    jfieldID vendorIDField;
    jfieldID deviceIDField;
    jfieldID deviceTypeField;
    jfieldID deviceNameField;
    jfieldID pipelineCacheUUIDField;
    jfieldID limitsField;
    jfieldID sparsePropertiesField;
public:
    VkPhysicalDevicePropertiesAccessor(JNIEnv *env, jobject obj);

    void getdeviceName(VkPhysicalDeviceProperties &clazzInfo);

    void getlimits(VkPhysicalDeviceProperties &clazzInfo);

    void getsparseProperties(VkPhysicalDeviceProperties &clazzInfo);

    uint32_t getdeviceID();

    VkPhysicalDeviceType getdeviceType();

    void fromObject(VkPhysicalDeviceProperties &clazzInfo);

    void getpipelineCacheUUID(VkPhysicalDeviceProperties &clazzInfo);

    uint32_t getvendorID();

    uint32_t getapiVersion();

    uint32_t getdriverVersion();

    ~VkPhysicalDevicePropertiesAccessor();
};
#endif // VKPHYSICALDEVICEPROPERTIESACCESSOR_H