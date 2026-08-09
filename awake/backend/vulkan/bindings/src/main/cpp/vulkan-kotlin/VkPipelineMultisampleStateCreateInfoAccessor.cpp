/*
 *  VkPipelineMultisampleStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineMultisampleStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineMultisampleStateCreateInfoAccessor.h>

VkPipelineMultisampleStateCreateInfoAccessor::VkPipelineMultisampleStateCreateInfoAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    rasterizationSamplesField = env->GetFieldID(clazz, "rasterizationSamples",
                                                "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkSampleCountFlagBits;");
    sampleShadingEnableField = env->GetFieldID(clazz, "sampleShadingEnable", "Z");
    minSampleShadingField = env->GetFieldID(clazz, "minSampleShading", "F");
    pSampleMaskField = env->GetFieldID(clazz, "pSampleMask", "I");
    alphaToCoverageEnableField = env->GetFieldID(clazz, "alphaToCoverageEnable", "Z");
    alphaToOneEnableField = env->GetFieldID(clazz, "alphaToOneEnable", "Z");
}

VkStructureType
VkPipelineMultisampleStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineMultisampleStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

VkBool32
VkPipelineMultisampleStateCreateInfoAccessor::getsampleShadingEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, sampleShadingEnableField); // primitive
}

void
VkPipelineMultisampleStateCreateInfoAccessor::fromObject(
        VkPipelineMultisampleStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.rasterizationSamples = getrasterizationSamples(); // Enum VkSampleCountFlagBits
    clazzInfo.sampleShadingEnable = getsampleShadingEnable(); // Primitive VkBool32
    clazzInfo.minSampleShading = getminSampleShading(); // Primitive float
    getpSampleMask(clazzInfo); // Pointer
    clazzInfo.alphaToCoverageEnable = getalphaToCoverageEnable(); // Primitive VkBool32
    clazzInfo.alphaToOneEnable = getalphaToOneEnable(); // Primitive VkBool32
}

void
VkPipelineMultisampleStateCreateInfoAccessor::getpSampleMask(
        VkPipelineMultisampleStateCreateInfo &clazzInfo) {
    auto ptr = (uint32_t) env->GetIntField(obj, pSampleMaskField); // Primitive Pointer
    if (ptr != 0) {
        clazzInfo.pSampleMask = &ptr; // Primitive Pointer
    }
}

VkBool32
VkPipelineMultisampleStateCreateInfoAccessor::getalphaToCoverageEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, alphaToCoverageEnableField); // primitive
}

float
VkPipelineMultisampleStateCreateInfoAccessor::getminSampleShading() {
    return (float) (jfloat) env->GetFloatField(obj, minSampleShadingField); // primitive
}

VkSampleCountFlagBits
VkPipelineMultisampleStateCreateInfoAccessor::getrasterizationSamples() {
    auto rasterizationSamplesEnum = (jobject) env->GetObjectField(obj, rasterizationSamplesField);
    auto enumValue = (VkSampleCountFlagBits) enum_utils::getEnumFromObject(env,
                                                                           rasterizationSamplesEnum);
    env->DeleteLocalRef(rasterizationSamplesEnum); // release enum reference
    return enumValue;
}

VkBool32
VkPipelineMultisampleStateCreateInfoAccessor::getalphaToOneEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, alphaToOneEnableField); // primitive
}

void
VkPipelineMultisampleStateCreateInfoAccessor::getpNext(
        VkPipelineMultisampleStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkPipelineMultisampleStateCreateInfoAccessor::~VkPipelineMultisampleStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

