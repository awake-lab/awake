/*
 *  VkDebugUtilsLabelEXTMutator.h
 *  Vulkan mutator for VkDebugUtilsLabelEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSLABELEXTMUTATOR_H
#define VKDEBUGUTILSLABELEXTMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDebugUtilsLabelEXTMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID pLabelNameField;
    jfieldID colorField;
public:
    VkDebugUtilsLabelEXTMutator(JNIEnv *env);

    jobject toObject(VkDebugUtilsLabelEXT source);
    ~VkDebugUtilsLabelEXTMutator();
};
#endif // VKDEBUGUTILSLABELEXTMUTATOR_H