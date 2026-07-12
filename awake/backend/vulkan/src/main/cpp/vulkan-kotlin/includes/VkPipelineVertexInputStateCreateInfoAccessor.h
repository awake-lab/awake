/*
 *  VkPipelineVertexInputStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineVertexInputStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINEVERTEXINPUTSTATECREATEINFOACCESSOR_H
#define VKPIPELINEVERTEXINPUTSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkVertexInputBindingDescriptionAccessor.h>
#include <includes/VkVertexInputAttributeDescriptionAccessor.h>

class VkPipelineVertexInputStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pVertexBindingDescriptionsField;
    jfieldID pVertexAttributeDescriptionsField;
public:
    VkPipelineVertexInputStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void fromObject(VkPipelineVertexInputStateCreateInfo &clazzInfo);

    void getpNext(VkPipelineVertexInputStateCreateInfo &clazzInfo);

    void getpVertexAttributeDescriptions(VkPipelineVertexInputStateCreateInfo &clazzInfo);

    void getpVertexBindingDescriptions(VkPipelineVertexInputStateCreateInfo &clazzInfo);

    ~VkPipelineVertexInputStateCreateInfoAccessor();
};
#endif // VKPIPELINEVERTEXINPUTSTATECREATEINFOACCESSOR_H