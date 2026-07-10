/*
 *  VkCommandPoolCreateInfoAccessor.cpp
 *  Vulkan accessor for VkCommandPoolCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkCommandPoolCreateInfoAccessor.h>

VkCommandPoolCreateInfoAccessor::VkCommandPoolCreateInfoAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    queueFamilyIndexField = env->GetFieldID(clazz, "queueFamilyIndex", "I");
}

VkStructureType
VkCommandPoolCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkCommandPoolCreateInfoAccessor::getqueueFamilyIndex() {
    return (uint32_t) (jint) env->GetIntField(obj, queueFamilyIndexField); // primitive
}

uint32_t
VkCommandPoolCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkCommandPoolCreateInfoAccessor::getpNext(VkCommandPoolCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkCommandPoolCreateInfoAccessor::fromObject(VkCommandPoolCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.queueFamilyIndex = getqueueFamilyIndex(); // Primitive uint32_t
}

VkCommandPoolCreateInfoAccessor::~VkCommandPoolCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

