/*
 *  VkPipelineLayoutCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineLayoutCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINELAYOUTCREATEINFOACCESSOR_H
#define VKPIPELINELAYOUTCREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkPushConstantRangeAccessor.h>

class VkPipelineLayoutCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pSetLayoutsField;
    jfieldID pushConstantRangeCountField;
    jfieldID pPushConstantRangesField;
public:
    VkPipelineLayoutCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    uint32_t getpushConstantRangeCount();

    void getpPushConstantRanges(VkPipelineLayoutCreateInfo &clazzInfo);

    void fromObject(VkPipelineLayoutCreateInfo &clazzInfo);

    void getpNext(VkPipelineLayoutCreateInfo &clazzInfo);

    void getpSetLayouts(VkPipelineLayoutCreateInfo &clazzInfo);

    ~VkPipelineLayoutCreateInfoAccessor();
};
#endif // VKPIPELINELAYOUTCREATEINFOACCESSOR_H