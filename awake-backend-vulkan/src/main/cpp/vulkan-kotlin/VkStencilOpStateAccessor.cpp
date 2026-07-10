/*
 *  VkStencilOpStateAccessor.cpp
 *  Vulkan accessor for VkStencilOpState
 *  Created by Ron June Valdoz */

#include <includes/VkStencilOpStateAccessor.h>

VkStencilOpStateAccessor::VkStencilOpStateAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    failOpField = env->GetFieldID(clazz, "failOp",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStencilOp;");
    passOpField = env->GetFieldID(clazz, "passOp",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStencilOp;");
    depthFailOpField = env->GetFieldID(clazz, "depthFailOp",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStencilOp;");
    compareOpField = env->GetFieldID(clazz, "compareOp",
                                     "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkCompareOp;");
    compareMaskField = env->GetFieldID(clazz, "compareMask", "I");
    writeMaskField = env->GetFieldID(clazz, "writeMask", "I");
    referenceField = env->GetFieldID(clazz, "reference", "I");
}

VkStencilOp
VkStencilOpStateAccessor::getfailOp() {
    auto failOpEnum = (jobject) env->GetObjectField(obj, failOpField);
    auto enumValue = (VkStencilOp) enum_utils::getEnumFromObject(env, failOpEnum);
    env->DeleteLocalRef(failOpEnum); // release enum reference
    return enumValue;
}

uint32_t
VkStencilOpStateAccessor::getcompareMask() {
    return (uint32_t) (jint) env->GetIntField(obj, compareMaskField); // primitive
}

uint32_t
VkStencilOpStateAccessor::getreference() {
    return (uint32_t) (jint) env->GetIntField(obj, referenceField); // primitive
}

VkCompareOp
VkStencilOpStateAccessor::getcompareOp() {
    auto compareOpEnum = (jobject) env->GetObjectField(obj, compareOpField);
    auto enumValue = (VkCompareOp) enum_utils::getEnumFromObject(env, compareOpEnum);
    env->DeleteLocalRef(compareOpEnum); // release enum reference
    return enumValue;
}

uint32_t
VkStencilOpStateAccessor::getwriteMask() {
    return (uint32_t) (jint) env->GetIntField(obj, writeMaskField); // primitive
}

void
VkStencilOpStateAccessor::fromObject(VkStencilOpState &clazzInfo) {
    clazzInfo.failOp = getfailOp(); // Enum VkStencilOp
    clazzInfo.passOp = getpassOp(); // Enum VkStencilOp
    clazzInfo.depthFailOp = getdepthFailOp(); // Enum VkStencilOp
    clazzInfo.compareOp = getcompareOp(); // Enum VkCompareOp
    clazzInfo.compareMask = getcompareMask(); // Primitive uint32_t
    clazzInfo.writeMask = getwriteMask(); // Primitive uint32_t
    clazzInfo.reference = getreference(); // Primitive uint32_t
}

VkStencilOp
VkStencilOpStateAccessor::getpassOp() {
    auto passOpEnum = (jobject) env->GetObjectField(obj, passOpField);
    auto enumValue = (VkStencilOp) enum_utils::getEnumFromObject(env, passOpEnum);
    env->DeleteLocalRef(passOpEnum); // release enum reference
    return enumValue;
}

VkStencilOp
VkStencilOpStateAccessor::getdepthFailOp() {
    auto depthFailOpEnum = (jobject) env->GetObjectField(obj, depthFailOpField);
    auto enumValue = (VkStencilOp) enum_utils::getEnumFromObject(env, depthFailOpEnum);
    env->DeleteLocalRef(depthFailOpEnum); // release enum reference
    return enumValue;
}

VkStencilOpStateAccessor::~VkStencilOpStateAccessor() {
    env->DeleteLocalRef(clazz);
}

