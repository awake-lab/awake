/*
 *  VkPhysicalDeviceSparsePropertiesMutator.h
 *  Vulkan mutator for VkPhysicalDeviceSparseProperties
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICESPARSEPROPERTIESMUTATOR_H
#define VKPHYSICALDEVICESPARSEPROPERTIESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceSparsePropertiesMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID residencyStandard2DBlockShapeField;
    jfieldID residencyStandard2DMultisampleBlockShapeField;
    jfieldID residencyStandard3DBlockShapeField;
    jfieldID residencyAlignedMipSizeField;
    jfieldID residencyNonResidentStrictField;
public:
    VkPhysicalDeviceSparsePropertiesMutator(JNIEnv *env);

    jobject toObject(VkPhysicalDeviceSparseProperties source);
    ~VkPhysicalDeviceSparsePropertiesMutator();
};
#endif // VKPHYSICALDEVICESPARSEPROPERTIESMUTATOR_H