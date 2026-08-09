/*
 *  VkPhysicalDevicePropertiesMutator.h
 *  Vulkan mutator for VkPhysicalDeviceProperties
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICEPROPERTIESMUTATOR_H
#define VKPHYSICALDEVICEPROPERTIESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkPhysicalDeviceLimitsMutator.h>
#include <includes/VkPhysicalDeviceSparsePropertiesMutator.h>

class VkPhysicalDevicePropertiesMutator {
private:
    JNIEnv *env;
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
    VkPhysicalDevicePropertiesMutator(JNIEnv *env);

    jobject toObject(VkPhysicalDeviceProperties source);
    ~VkPhysicalDevicePropertiesMutator();
};
#endif // VKPHYSICALDEVICEPROPERTIESMUTATOR_H