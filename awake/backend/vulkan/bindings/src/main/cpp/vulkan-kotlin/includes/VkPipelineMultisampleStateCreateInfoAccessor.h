/*
 *  VkPipelineMultisampleStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineMultisampleStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINEMULTISAMPLESTATECREATEINFOACCESSOR_H
#define VKPIPELINEMULTISAMPLESTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineMultisampleStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID rasterizationSamplesField;
    jfieldID sampleShadingEnableField;
    jfieldID minSampleShadingField;
    jfieldID pSampleMaskField;
    jfieldID alphaToCoverageEnableField;
    jfieldID alphaToOneEnableField;
public:
    VkPipelineMultisampleStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    VkBool32 getsampleShadingEnable();

    void fromObject(VkPipelineMultisampleStateCreateInfo &clazzInfo);

    void getpSampleMask(VkPipelineMultisampleStateCreateInfo &clazzInfo);

    VkBool32 getalphaToCoverageEnable();

    float getminSampleShading();

    VkSampleCountFlagBits getrasterizationSamples();

    VkBool32 getalphaToOneEnable();

    void getpNext(VkPipelineMultisampleStateCreateInfo &clazzInfo);

    ~VkPipelineMultisampleStateCreateInfoAccessor();
};
#endif // VKPIPELINEMULTISAMPLESTATECREATEINFOACCESSOR_H