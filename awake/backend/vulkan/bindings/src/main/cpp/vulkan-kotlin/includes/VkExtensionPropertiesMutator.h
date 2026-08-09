/*
 *  VkExtensionPropertiesMutator.h
 *  Vulkan mutator for VkExtensionProperties
 *  Created by Ron June Valdoz */

#ifndef VKEXTENSIONPROPERTIESMUTATOR_H
#define VKEXTENSIONPROPERTIESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtensionPropertiesMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID extensionNameField;
    jfieldID specVersionField;
public:
    VkExtensionPropertiesMutator(JNIEnv *env);

    jobject toObject(VkExtensionProperties source);
    ~VkExtensionPropertiesMutator();
};
#endif // VKEXTENSIONPROPERTIESMUTATOR_H