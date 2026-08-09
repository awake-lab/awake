/*
 *  VkPipelineColorBlendAttachmentStateAccessor.cpp
 *  Vulkan accessor for VkPipelineColorBlendAttachmentState
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineColorBlendAttachmentStateAccessor.h>

VkPipelineColorBlendAttachmentStateAccessor::VkPipelineColorBlendAttachmentStateAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    blendEnableField = env->GetFieldID(clazz, "blendEnable", "Z");
    srcColorBlendFactorField = env->GetFieldID(clazz, "srcColorBlendFactor",
                                               "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendFactor;");
    dstColorBlendFactorField = env->GetFieldID(clazz, "dstColorBlendFactor",
                                               "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendFactor;");
    colorBlendOpField = env->GetFieldID(clazz, "colorBlendOp",
                                        "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendOp;");
    srcAlphaBlendFactorField = env->GetFieldID(clazz, "srcAlphaBlendFactor",
                                               "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendFactor;");
    dstAlphaBlendFactorField = env->GetFieldID(clazz, "dstAlphaBlendFactor",
                                               "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendFactor;");
    alphaBlendOpField = env->GetFieldID(clazz, "alphaBlendOp",
                                        "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkBlendOp;");
    colorWriteMaskField = env->GetFieldID(clazz, "colorWriteMask", "I");
}

VkBlendOp
VkPipelineColorBlendAttachmentStateAccessor::getcolorBlendOp() {
    auto colorBlendOpEnum = (jobject) env->GetObjectField(obj, colorBlendOpField);
    auto enumValue = (VkBlendOp) enum_utils::getEnumFromObject(env, colorBlendOpEnum);
    env->DeleteLocalRef(colorBlendOpEnum); // release enum reference
    return enumValue;
}

VkBool32
VkPipelineColorBlendAttachmentStateAccessor::getblendEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, blendEnableField); // primitive
}

VkBlendFactor
VkPipelineColorBlendAttachmentStateAccessor::getsrcColorBlendFactor() {
    auto srcColorBlendFactorEnum = (jobject) env->GetObjectField(obj, srcColorBlendFactorField);
    auto enumValue = (VkBlendFactor) enum_utils::getEnumFromObject(env, srcColorBlendFactorEnum);
    env->DeleteLocalRef(srcColorBlendFactorEnum); // release enum reference
    return enumValue;
}

void
VkPipelineColorBlendAttachmentStateAccessor::fromObject(
        VkPipelineColorBlendAttachmentState &clazzInfo) {
    clazzInfo.blendEnable = getblendEnable(); // Primitive VkBool32
    clazzInfo.srcColorBlendFactor = getsrcColorBlendFactor(); // Enum VkBlendFactor
    clazzInfo.dstColorBlendFactor = getdstColorBlendFactor(); // Enum VkBlendFactor
    clazzInfo.colorBlendOp = getcolorBlendOp(); // Enum VkBlendOp
    clazzInfo.srcAlphaBlendFactor = getsrcAlphaBlendFactor(); // Enum VkBlendFactor
    clazzInfo.dstAlphaBlendFactor = getdstAlphaBlendFactor(); // Enum VkBlendFactor
    clazzInfo.alphaBlendOp = getalphaBlendOp(); // Enum VkBlendOp
    clazzInfo.colorWriteMask = getcolorWriteMask(); // Primitive uint32_t
}

uint32_t
VkPipelineColorBlendAttachmentStateAccessor::getcolorWriteMask() {
    return (uint32_t) (jint) env->GetIntField(obj, colorWriteMaskField); // primitive
}

VkBlendFactor
VkPipelineColorBlendAttachmentStateAccessor::getdstColorBlendFactor() {
    auto dstColorBlendFactorEnum = (jobject) env->GetObjectField(obj, dstColorBlendFactorField);
    auto enumValue = (VkBlendFactor) enum_utils::getEnumFromObject(env, dstColorBlendFactorEnum);
    env->DeleteLocalRef(dstColorBlendFactorEnum); // release enum reference
    return enumValue;
}

VkBlendFactor
VkPipelineColorBlendAttachmentStateAccessor::getsrcAlphaBlendFactor() {
    auto srcAlphaBlendFactorEnum = (jobject) env->GetObjectField(obj, srcAlphaBlendFactorField);
    auto enumValue = (VkBlendFactor) enum_utils::getEnumFromObject(env, srcAlphaBlendFactorEnum);
    env->DeleteLocalRef(srcAlphaBlendFactorEnum); // release enum reference
    return enumValue;
}

VkBlendOp
VkPipelineColorBlendAttachmentStateAccessor::getalphaBlendOp() {
    auto alphaBlendOpEnum = (jobject) env->GetObjectField(obj, alphaBlendOpField);
    auto enumValue = (VkBlendOp) enum_utils::getEnumFromObject(env, alphaBlendOpEnum);
    env->DeleteLocalRef(alphaBlendOpEnum); // release enum reference
    return enumValue;
}

VkBlendFactor
VkPipelineColorBlendAttachmentStateAccessor::getdstAlphaBlendFactor() {
    auto dstAlphaBlendFactorEnum = (jobject) env->GetObjectField(obj, dstAlphaBlendFactorField);
    auto enumValue = (VkBlendFactor) enum_utils::getEnumFromObject(env, dstAlphaBlendFactorEnum);
    env->DeleteLocalRef(dstAlphaBlendFactorEnum); // release enum reference
    return enumValue;
}

VkPipelineColorBlendAttachmentStateAccessor::~VkPipelineColorBlendAttachmentStateAccessor() {
    env->DeleteLocalRef(clazz);
}

