/*
 *  VkDebugUtilsMessengerCallbackDataEXTAccessor.h
 *  Vulkan accessor for VkDebugUtilsMessengerCallbackDataEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSMESSENGERCALLBACKDATAEXTACCESSOR_H
#define VKDEBUGUTILSMESSENGERCALLBACKDATAEXTACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkDebugUtilsLabelEXTAccessor.h>
#include <includes/VkDebugUtilsObjectNameInfoEXTAccessor.h>

class VkDebugUtilsMessengerCallbackDataEXTAccessor {
private:
    JNIEnv *env;
    jobject obj;
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
    VkDebugUtilsMessengerCallbackDataEXTAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    void getpMessage(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    void getpQueueLabels(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    uint32_t getmessageIdNumber();

    void getpCmdBufLabels(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    void getpMessageIdName(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    void fromObject(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    void getpObjects(VkDebugUtilsMessengerCallbackDataEXT &clazzInfo);

    ~VkDebugUtilsMessengerCallbackDataEXTAccessor();
};
#endif // VKDEBUGUTILSMESSENGERCALLBACKDATAEXTACCESSOR_H