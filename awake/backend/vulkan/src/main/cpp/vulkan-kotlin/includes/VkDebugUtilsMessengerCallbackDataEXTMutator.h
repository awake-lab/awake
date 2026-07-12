/*
 *  VkDebugUtilsMessengerCallbackDataEXTMutator.h
 *  Vulkan mutator for VkDebugUtilsMessengerCallbackDataEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSMESSENGERCALLBACKDATAEXTMUTATOR_H
#define VKDEBUGUTILSMESSENGERCALLBACKDATAEXTMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkDebugUtilsLabelEXTMutator.h>
#include <includes/VkDebugUtilsObjectNameInfoEXTMutator.h>

class VkDebugUtilsMessengerCallbackDataEXTMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pMessageIdNameField;
    jfieldID messageIdNumberField;
    jfieldID pMessageField;
    jfieldID pQueueLabelsField;
    jfieldID pCmdBufLabelsField;
    jfieldID pObjectsField;
public:
    VkDebugUtilsMessengerCallbackDataEXTMutator(JNIEnv *env);

    jobject toObject(VkDebugUtilsMessengerCallbackDataEXT source);
    ~VkDebugUtilsMessengerCallbackDataEXTMutator();
};
#endif // VKDEBUGUTILSMESSENGERCALLBACKDATAEXTMUTATOR_H