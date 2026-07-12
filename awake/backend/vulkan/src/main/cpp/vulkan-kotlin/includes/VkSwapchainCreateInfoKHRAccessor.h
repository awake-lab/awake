/*
 *  VkSwapchainCreateInfoKHRAccessor.h
 *  Vulkan accessor for VkSwapchainCreateInfoKHR
 *  Created by Ron June Valdoz */

#ifndef VKSWAPCHAINCREATEINFOKHRACCESSOR_H
#define VKSWAPCHAINCREATEINFOKHRACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkExtent2DAccessor.h>

class VkSwapchainCreateInfoKHRAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID surfaceField;
    jfieldID minImageCountField;
    jfieldID imageFormatField;
    jfieldID imageColorSpaceField;
    jfieldID imageExtentField;
    jfieldID imageArrayLayersField;
    jfieldID imageUsageField;
    jfieldID imageSharingModeField;
    jfieldID pQueueFamilyIndicesField;
    jfieldID preTransformField;
    jfieldID compositeAlphaField;
    jfieldID presentModeField;
    jfieldID clippedField;
    jfieldID oldSwapchainField;
public:
    VkSwapchainCreateInfoKHRAccessor(JNIEnv *env, jobject obj);

    void getimageExtent(VkSwapchainCreateInfoKHR &clazzInfo);

    uint32_t getimageUsage();

    void getpQueueFamilyIndices(VkSwapchainCreateInfoKHR &clazzInfo);

    VkSurfaceTransformFlagBitsKHR getpreTransform();

    VkBool32 getclipped();

    VkStructureType getsType();

    uint32_t getflags();

    uint32_t getimageArrayLayers();

    VkPresentModeKHR getpresentMode();

    void getpNext(VkSwapchainCreateInfoKHR &clazzInfo);

    VkColorSpaceKHR getimageColorSpace();
    VkSharingMode getimageSharingMode();

    VkFormat getimageFormat();

    void fromObject(VkSwapchainCreateInfoKHR &clazzInfo);

    uint32_t getminImageCount();
    VkCompositeAlphaFlagBitsKHR getcompositeAlpha();
    VkSwapchainKHR getoldSwapchain();
    VkSurfaceKHR getsurface();
    ~VkSwapchainCreateInfoKHRAccessor();
};
#endif // VKSWAPCHAINCREATEINFOKHRACCESSOR_H