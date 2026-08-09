/*
 *  VkPipelineColorBlendAttachmentStateAccessor.h
 *  Vulkan accessor for VkPipelineColorBlendAttachmentState
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINECOLORBLENDATTACHMENTSTATEACCESSOR_H
#define VKPIPELINECOLORBLENDATTACHMENTSTATEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineColorBlendAttachmentStateAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID blendEnableField;
    jfieldID srcColorBlendFactorField;
    jfieldID dstColorBlendFactorField;
    jfieldID colorBlendOpField;
    jfieldID srcAlphaBlendFactorField;
    jfieldID dstAlphaBlendFactorField;
    jfieldID alphaBlendOpField;
    jfieldID colorWriteMaskField;
public:
    VkPipelineColorBlendAttachmentStateAccessor(JNIEnv *env, jobject obj);

    VkBlendOp getcolorBlendOp();
    VkBool32 getblendEnable();

    VkBlendFactor getsrcColorBlendFactor();

    void fromObject(VkPipelineColorBlendAttachmentState &clazzInfo);

    uint32_t getcolorWriteMask();
    VkBlendFactor getdstColorBlendFactor();
    VkBlendFactor getsrcAlphaBlendFactor();
    VkBlendOp getalphaBlendOp();
    VkBlendFactor getdstAlphaBlendFactor();
    ~VkPipelineColorBlendAttachmentStateAccessor();
};
#endif // VKPIPELINECOLORBLENDATTACHMENTSTATEACCESSOR_H