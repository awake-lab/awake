/*
 *  VkSurfaceCapabilitiesKHRAccessor.h
 *  Vulkan accessor for VkSurfaceCapabilitiesKHR
 *  Created by Ron June Valdoz */

#ifndef VKSURFACECAPABILITIESKHRACCESSOR_H
#define VKSURFACECAPABILITIESKHRACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkExtent2DAccessor.h>

class VkSurfaceCapabilitiesKHRAccessor {
private:
    JNIEnv *env;
    jobject obj;
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
    VkSurfaceCapabilitiesKHRAccessor(JNIEnv *env, jobject obj);

    void getcurrentExtent(VkSurfaceCapabilitiesKHR &clazzInfo);

    uint32_t getmaxImageCount();

    void getminImageExtent(VkSurfaceCapabilitiesKHR &clazzInfo);

    uint32_t getsupportedCompositeAlpha();

    uint32_t getsupportedTransforms();

    uint32_t getmaxImageArrayLayers();

    VkSurfaceTransformFlagBitsKHR getcurrentTransform();

    void getmaxImageExtent(VkSurfaceCapabilitiesKHR &clazzInfo);

    uint32_t getminImageCount();

    uint32_t getsupportedUsageFlags();

    void fromObject(VkSurfaceCapabilitiesKHR &clazzInfo);

    ~VkSurfaceCapabilitiesKHRAccessor();
};
#endif // VKSURFACECAPABILITIESKHRACCESSOR_H