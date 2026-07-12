/*
 *  VkPipelineColorBlendStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineColorBlendStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINECOLORBLENDSTATECREATEINFOACCESSOR_H
#define VKPIPELINECOLORBLENDSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkPipelineColorBlendAttachmentStateAccessor.h>

class VkPipelineColorBlendStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID logicOpEnableField;
    jfieldID logicOpField;
    jfieldID pAttachmentsField;
    jfieldID blendConstantsField;
public:
    VkPipelineColorBlendStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    VkBool32 getlogicOpEnable();

    uint32_t getflags();

    void getpAttachments(VkPipelineColorBlendStateCreateInfo &clazzInfo);

    void fromObject(VkPipelineColorBlendStateCreateInfo &clazzInfo);

    void getblendConstants(VkPipelineColorBlendStateCreateInfo &clazzInfo);

    void getpNext(VkPipelineColorBlendStateCreateInfo &clazzInfo);

    VkLogicOp getlogicOp();

    ~VkPipelineColorBlendStateCreateInfoAccessor();
};
#endif // VKPIPELINECOLORBLENDSTATECREATEINFOACCESSOR_H