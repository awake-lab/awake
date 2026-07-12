/*
 *  VkFenceCreateInfoAccessor.cpp
 *  Vulkan accessor for VkFenceCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkFenceCreateInfoAccessor.h>

VkFenceCreateInfoAccessor::VkFenceCreateInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                 obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
}

VkStructureType
VkFenceCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkFenceCreateInfoAccessor::getpNext(VkFenceCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

uint32_t
VkFenceCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkFenceCreateInfoAccessor::fromObject(VkFenceCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
}

VkFenceCreateInfoAccessor::~VkFenceCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

