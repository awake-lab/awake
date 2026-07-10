/*
 *  VkVertexInputAttributeDescriptionAccessor.h
 *  Vulkan accessor for VkVertexInputAttributeDescription
 *  Created by Ron June Valdoz */

#ifndef VKVERTEXINPUTATTRIBUTEDESCRIPTIONACCESSOR_H
#define VKVERTEXINPUTATTRIBUTEDESCRIPTIONACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkVertexInputAttributeDescriptionAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID locationField;
    jfieldID bindingField;
    jfieldID formatField;
    jfieldID offsetField;
public:
    VkVertexInputAttributeDescriptionAccessor(JNIEnv *env, jobject obj);

    uint32_t getlocation();

    uint32_t getbinding();

    void fromObject(VkVertexInputAttributeDescription &clazzInfo);

    VkFormat getformat();
    uint32_t getoffset();
    ~VkVertexInputAttributeDescriptionAccessor();
};
#endif // VKVERTEXINPUTATTRIBUTEDESCRIPTIONACCESSOR_H