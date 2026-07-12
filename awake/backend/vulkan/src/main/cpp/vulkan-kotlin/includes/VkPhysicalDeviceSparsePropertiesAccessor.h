/*
 *  VkPhysicalDeviceSparsePropertiesAccessor.h
 *  Vulkan accessor for VkPhysicalDeviceSparseProperties
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICESPARSEPROPERTIESACCESSOR_H
#define VKPHYSICALDEVICESPARSEPROPERTIESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceSparsePropertiesAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID residencyStandard2DBlockShapeField;
    jfieldID residencyStandard2DMultisampleBlockShapeField;
    jfieldID residencyStandard3DBlockShapeField;
    jfieldID residencyAlignedMipSizeField;
    jfieldID residencyNonResidentStrictField;
public:
    VkPhysicalDeviceSparsePropertiesAccessor(JNIEnv *env, jobject obj);

    VkBool32 getresidencyStandard2DBlockShape();
    VkBool32 getresidencyNonResidentStrict();
    VkBool32 getresidencyStandard3DBlockShape();
    VkBool32 getresidencyStandard2DMultisampleBlockShape();

    VkBool32 getresidencyAlignedMipSize();

    void fromObject(VkPhysicalDeviceSparseProperties &clazzInfo);

    ~VkPhysicalDeviceSparsePropertiesAccessor();
};
#endif // VKPHYSICALDEVICESPARSEPROPERTIESACCESSOR_H