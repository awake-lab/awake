/*
 *  VkPipelineRasterizationStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineRasterizationStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineRasterizationStateCreateInfoAccessor.h>

VkPipelineRasterizationStateCreateInfoAccessor::VkPipelineRasterizationStateCreateInfoAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    depthClampEnableField = env->GetFieldID(clazz, "depthClampEnable", "Z");
    rasterizerDiscardEnableField = env->GetFieldID(clazz, "rasterizerDiscardEnable", "Z");
    polygonModeField = env->GetFieldID(clazz, "polygonMode",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkPolygonMode;");
    cullModeField = env->GetFieldID(clazz, "cullMode", "I");
    frontFaceField = env->GetFieldID(clazz, "frontFace",
                                     "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFrontFace;");
    depthBiasEnableField = env->GetFieldID(clazz, "depthBiasEnable", "Z");
    depthBiasConstantFactorField = env->GetFieldID(clazz, "depthBiasConstantFactor", "F");
    depthBiasClampField = env->GetFieldID(clazz, "depthBiasClamp", "F");
    depthBiasSlopeFactorField = env->GetFieldID(clazz, "depthBiasSlopeFactor", "F");
    lineWidthField = env->GetFieldID(clazz, "lineWidth", "F");
}

float
VkPipelineRasterizationStateCreateInfoAccessor::getlineWidth() {
    return (float) (jfloat) env->GetFloatField(obj, lineWidthField); // primitive
}

VkPolygonMode
VkPipelineRasterizationStateCreateInfoAccessor::getpolygonMode() {
    auto polygonModeEnum = (jobject) env->GetObjectField(obj, polygonModeField);
    auto enumValue = (VkPolygonMode) enum_utils::getEnumFromObject(env, polygonModeEnum);
    env->DeleteLocalRef(polygonModeEnum); // release enum reference
    return enumValue;
}

float
VkPipelineRasterizationStateCreateInfoAccessor::getdepthBiasConstantFactor() {
    return (float) (jfloat) env->GetFloatField(obj, depthBiasConstantFactorField); // primitive
}

float
VkPipelineRasterizationStateCreateInfoAccessor::getdepthBiasClamp() {
    return (float) (jfloat) env->GetFloatField(obj, depthBiasClampField); // primitive
}

VkFrontFace
VkPipelineRasterizationStateCreateInfoAccessor::getfrontFace() {
    auto frontFaceEnum = (jobject) env->GetObjectField(obj, frontFaceField);
    auto enumValue = (VkFrontFace) enum_utils::getEnumFromObject(env, frontFaceEnum);
    env->DeleteLocalRef(frontFaceEnum); // release enum reference
    return enumValue;
}

VkStructureType
VkPipelineRasterizationStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkPipelineRasterizationStateCreateInfoAccessor::getpNext(
        VkPipelineRasterizationStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

uint32_t
VkPipelineRasterizationStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

float
VkPipelineRasterizationStateCreateInfoAccessor::getdepthBiasSlopeFactor() {
    return (float) (jfloat) env->GetFloatField(obj, depthBiasSlopeFactorField); // primitive
}

VkBool32
VkPipelineRasterizationStateCreateInfoAccessor::getdepthBiasEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, depthBiasEnableField); // primitive
}

VkBool32
VkPipelineRasterizationStateCreateInfoAccessor::getdepthClampEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, depthClampEnableField); // primitive
}

void
VkPipelineRasterizationStateCreateInfoAccessor::fromObject(
        VkPipelineRasterizationStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.depthClampEnable = getdepthClampEnable(); // Primitive VkBool32
    clazzInfo.rasterizerDiscardEnable = getrasterizerDiscardEnable(); // Primitive VkBool32
    clazzInfo.polygonMode = getpolygonMode(); // Enum VkPolygonMode
    clazzInfo.cullMode = getcullMode(); // Primitive uint32_t
    clazzInfo.frontFace = getfrontFace(); // Enum VkFrontFace
    clazzInfo.depthBiasEnable = getdepthBiasEnable(); // Primitive VkBool32
    clazzInfo.depthBiasConstantFactor = getdepthBiasConstantFactor(); // Primitive float
    clazzInfo.depthBiasClamp = getdepthBiasClamp(); // Primitive float
    clazzInfo.depthBiasSlopeFactor = getdepthBiasSlopeFactor(); // Primitive float
    clazzInfo.lineWidth = getlineWidth(); // Primitive float
}

VkBool32
VkPipelineRasterizationStateCreateInfoAccessor::getrasterizerDiscardEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      rasterizerDiscardEnableField); // primitive
}

uint32_t
VkPipelineRasterizationStateCreateInfoAccessor::getcullMode() {
    return (uint32_t) (jint) env->GetIntField(obj, cullModeField); // primitive
}

VkPipelineRasterizationStateCreateInfoAccessor::~VkPipelineRasterizationStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

