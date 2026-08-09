/*
 *  VkPipelineTessellationStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineTessellationStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineTessellationStateCreateInfoAccessor.h>

VkPipelineTessellationStateCreateInfoAccessor::VkPipelineTessellationStateCreateInfoAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    patchControlPointsField = env->GetFieldID(clazz, "patchControlPoints", "I");
}

VkStructureType
VkPipelineTessellationStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineTessellationStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkPipelineTessellationStateCreateInfoAccessor::getpNext(
        VkPipelineTessellationStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPipelineTessellationStateCreateInfoAccessor::fromObject(
        VkPipelineTessellationStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.patchControlPoints = getpatchControlPoints(); // Primitive uint32_t
}

uint32_t
VkPipelineTessellationStateCreateInfoAccessor::getpatchControlPoints() {
    return (uint32_t) (jint) env->GetIntField(obj, patchControlPointsField); // primitive
}

VkPipelineTessellationStateCreateInfoAccessor::~VkPipelineTessellationStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

