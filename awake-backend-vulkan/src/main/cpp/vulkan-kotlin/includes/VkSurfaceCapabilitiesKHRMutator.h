/*
 *  VkSurfaceCapabilitiesKHRMutator.h
 *  Vulkan mutator for VkSurfaceCapabilitiesKHR
 *  Created by Ron June Valdoz */

#ifndef VKSURFACECAPABILITIESKHRMUTATOR_H
#define VKSURFACECAPABILITIESKHRMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkExtent2DMutator.h>

class VkSurfaceCapabilitiesKHRMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID minImageCountField;
    jfieldID maxImageCountField;
    jfieldID currentExtentField;
    jfieldID minImageExtentField;
    jfieldID maxImageExtentField;
    jfieldID maxImageArrayLayersField;
    jfieldID supportedTransformsField;
    jfieldID currentTransformField;
    jfieldID supportedCompositeAlphaField;
    jfieldID supportedUsageFlagsField;
public:
    VkSurfaceCapabilitiesKHRMutator(JNIEnv *env);

    jobject toObject(VkSurfaceCapabilitiesKHR source);
    ~VkSurfaceCapabilitiesKHRMutator();
};
#endif // VKSURFACECAPABILITIESKHRMUTATOR_H