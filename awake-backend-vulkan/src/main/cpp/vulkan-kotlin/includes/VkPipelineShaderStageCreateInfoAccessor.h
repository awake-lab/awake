/*
 *  VkPipelineShaderStageCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineShaderStageCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINESHADERSTAGECREATEINFOACCESSOR_H
#define VKPIPELINESHADERSTAGECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkSpecializationInfoAccessor.h>

class VkPipelineShaderStageCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID stageField;
    jfieldID moduleField;
    jfieldID pNameField;
    jfieldID pSpecializationInfoField;
public:
    VkPipelineShaderStageCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpSpecializationInfo(VkPipelineShaderStageCreateInfo &clazzInfo);

    void getpName(VkPipelineShaderStageCreateInfo &clazzInfo);

    VkShaderModule getmodule();

    void fromObject(VkPipelineShaderStageCreateInfo &clazzInfo);

    void getpNext(VkPipelineShaderStageCreateInfo &clazzInfo);

    VkShaderStageFlagBits getstage();

    ~VkPipelineShaderStageCreateInfoAccessor();
};
#endif // VKPIPELINESHADERSTAGECREATEINFOACCESSOR_H