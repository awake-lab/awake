/*
 *  VkCommandBufferAllocateInfoAccessor.cpp
 *  Vulkan accessor for VkCommandBufferAllocateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkCommandBufferAllocateInfoAccessor.h>

VkCommandBufferAllocateInfoAccessor::VkCommandBufferAllocateInfoAccessor(JNIEnv *env, jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    commandPoolField = env->GetFieldID(clazz, "commandPool", "J");
    levelField = env->GetFieldID(clazz, "level",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkCommandBufferLevel;");
    commandBufferCountField = env->GetFieldID(clazz, "commandBufferCount", "I");
}

VkStructureType
VkCommandBufferAllocateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkCommandBufferAllocateInfoAccessor::fromObject(VkCommandBufferAllocateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.commandPool = getcommandPool(); // VkHandle
    clazzInfo.level = getlevel(); // Enum VkCommandBufferLevel
    clazzInfo.commandBufferCount = getcommandBufferCount(); // Primitive uint32_t
}

VkCommandPool
VkCommandBufferAllocateInfoAccessor::getcommandPool() {
    auto value = env->GetLongField(obj, commandPoolField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkCommandPool>(value); // VkHandle
}

VkCommandBufferLevel
VkCommandBufferAllocateInfoAccessor::getlevel() {
    auto levelEnum = (jobject) env->GetObjectField(obj, levelField);
    auto enumValue = (VkCommandBufferLevel) enum_utils::getEnumFromObject(env, levelEnum);
    env->DeleteLocalRef(levelEnum); // release enum reference
    return enumValue;
}

void
VkCommandBufferAllocateInfoAccessor::getpNext(VkCommandBufferAllocateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

uint32_t
VkCommandBufferAllocateInfoAccessor::getcommandBufferCount() {
    return (uint32_t) (jint) env->GetIntField(obj, commandBufferCountField); // primitive
}

VkCommandBufferAllocateInfoAccessor::~VkCommandBufferAllocateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

