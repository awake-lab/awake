/*
 *  VkExtent3DMutator.h
 *  Vulkan mutator for VkExtent3D
 *  Created by Ron June Valdoz */

#ifndef VKEXTENT3DMUTATOR_H
#define VKEXTENT3DMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtent3DMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID widthField;
    jfieldID heightField;
    jfieldID depthField;
public:
    VkExtent3DMutator(JNIEnv *env);

    jobject toObject(VkExtent3D source);
    ~VkExtent3DMutator();
};
#endif // VKEXTENT3DMUTATOR_H