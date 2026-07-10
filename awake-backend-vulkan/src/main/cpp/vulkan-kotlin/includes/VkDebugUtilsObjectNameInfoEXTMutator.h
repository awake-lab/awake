/*
 *  VkDebugUtilsObjectNameInfoEXTMutator.h
 *  Vulkan mutator for VkDebugUtilsObjectNameInfoEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSOBJECTNAMEINFOEXTMUTATOR_H
#define VKDEBUGUTILSOBJECTNAMEINFOEXTMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDebugUtilsObjectNameInfoEXTMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID objectTypeField;
    jfieldID objectHandleField;
    jfieldID pObjectNameField;
public:
    VkDebugUtilsObjectNameInfoEXTMutator(JNIEnv *env);

    jobject toObject(VkDebugUtilsObjectNameInfoEXT source);
    ~VkDebugUtilsObjectNameInfoEXTMutator();
};
#endif // VKDEBUGUTILSOBJECTNAMEINFOEXTMUTATOR_H