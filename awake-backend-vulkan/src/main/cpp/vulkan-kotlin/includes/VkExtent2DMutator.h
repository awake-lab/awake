/*
 *  VkExtent2DMutator.h
 *  Vulkan mutator for VkExtent2D
 *  Created by Ron June Valdoz */

#ifndef VKEXTENT2DMUTATOR_H
#define VKEXTENT2DMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtent2DMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID widthField;
    jfieldID heightField;
public:
    VkExtent2DMutator(JNIEnv *env);

    jobject toObject(VkExtent2D source);
    ~VkExtent2DMutator();
};
#endif // VKEXTENT2DMUTATOR_H