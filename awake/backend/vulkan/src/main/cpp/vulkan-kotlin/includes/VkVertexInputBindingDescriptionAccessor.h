/*
 *  VkVertexInputBindingDescriptionAccessor.h
 *  Vulkan accessor for VkVertexInputBindingDescription
 *  Created by Ron June Valdoz */

#ifndef VKVERTEXINPUTBINDINGDESCRIPTIONACCESSOR_H
#define VKVERTEXINPUTBINDINGDESCRIPTIONACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkVertexInputBindingDescriptionAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID bindingField;
    jfieldID strideField;
    jfieldID inputRateField;
public:
    VkVertexInputBindingDescriptionAccessor(JNIEnv *env, jobject obj);

    uint32_t getbinding();
    VkVertexInputRate getinputRate();

    uint32_t getstride();

    void fromObject(VkVertexInputBindingDescription &clazzInfo);

    ~VkVertexInputBindingDescriptionAccessor();
};
#endif // VKVERTEXINPUTBINDINGDESCRIPTIONACCESSOR_H