/*
 *  VkSemaphoreCreateInfoAccessor.cpp
 *  Vulkan accessor for VkSemaphoreCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkSemaphoreCreateInfoAccessor.h>

VkSemaphoreCreateInfoAccessor::VkSemaphoreCreateInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                         obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
}

VkStructureType
VkSemaphoreCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkSemaphoreCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkSemaphoreCreateInfoAccessor::getpNext(VkSemaphoreCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkSemaphoreCreateInfoAccessor::fromObject(VkSemaphoreCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
}

VkSemaphoreCreateInfoAccessor::~VkSemaphoreCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

