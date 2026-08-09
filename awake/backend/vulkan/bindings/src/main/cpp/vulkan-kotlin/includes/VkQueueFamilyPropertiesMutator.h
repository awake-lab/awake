/*
 *  VkQueueFamilyPropertiesMutator.h
 *  Vulkan mutator for VkQueueFamilyProperties
 *  Created by Ron June Valdoz */

#ifndef VKQUEUEFAMILYPROPERTIESMUTATOR_H
#define VKQUEUEFAMILYPROPERTIESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkExtent3DMutator.h>

class VkQueueFamilyPropertiesMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID queueFlagsField;
    jfieldID queueCountField;
    jfieldID timestampValidBitsField;
    jfieldID minImageTransferGranularityField;
public:
    VkQueueFamilyPropertiesMutator(JNIEnv *env);

    jobject toObject(VkQueueFamilyProperties source);
    ~VkQueueFamilyPropertiesMutator();
};
#endif // VKQUEUEFAMILYPROPERTIESMUTATOR_H