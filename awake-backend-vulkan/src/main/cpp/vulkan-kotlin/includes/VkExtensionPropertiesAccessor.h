/*
 *  VkExtensionPropertiesAccessor.h
 *  Vulkan accessor for VkExtensionProperties
 *  Created by Ron June Valdoz */

#ifndef VKEXTENSIONPROPERTIESACCESSOR_H
#define VKEXTENSIONPROPERTIESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtensionPropertiesAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID extensionNameField;
    jfieldID specVersionField;
public:
    VkExtensionPropertiesAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkExtensionProperties &clazzInfo);

    void getextensionName(VkExtensionProperties &clazzInfo);

    uint32_t getspecVersion();

    ~VkExtensionPropertiesAccessor();
};
#endif // VKEXTENSIONPROPERTIESACCESSOR_H