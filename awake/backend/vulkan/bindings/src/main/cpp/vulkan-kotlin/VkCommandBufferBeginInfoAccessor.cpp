/*
 *  VkCommandBufferBeginInfoAccessor.cpp
 *  Vulkan accessor for VkCommandBufferBeginInfo
 *  Created by Ron June Valdoz */

#include <includes/VkCommandBufferBeginInfoAccessor.h>

VkCommandBufferBeginInfoAccessor::VkCommandBufferBeginInfoAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pInheritanceInfoField = env->GetFieldID(clazz, "pInheritanceInfo",
                                            "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkCommandBufferInheritanceInfo;");
}

VkStructureType
VkCommandBufferBeginInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkCommandBufferBeginInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkCommandBufferBeginInfoAccessor::getpInheritanceInfo(VkCommandBufferBeginInfo &clazzInfo) {
    auto pInheritanceInfoArray = (jobjectArray) env->GetObjectField(obj, pInheritanceInfoField);
    if (pInheritanceInfoArray == nullptr) {
        clazzInfo.pInheritanceInfo = nullptr;
        env->DeleteLocalRef(pInheritanceInfoArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pInheritanceInfoArray);
    std::vector<VkCommandBufferInheritanceInfo> pInheritanceInfo;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pInheritanceInfoArray,
                                                            i); // actual type is VkCommandBufferInheritanceInfo[];
        // experimental optimize accessor
        VkCommandBufferInheritanceInfoAccessor accessor(env, element);
        VkCommandBufferInheritanceInfo ref{};
        accessor.fromObject(ref);
        pInheritanceInfo.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkCommandBufferInheritanceInfo[size];
    std::copy(pInheritanceInfo.begin(), pInheritanceInfo.end(), copy);
    clazzInfo.pInheritanceInfo = copy;
    env->DeleteLocalRef(pInheritanceInfoArray); // release reference
}

void
VkCommandBufferBeginInfoAccessor::getpNext(VkCommandBufferBeginInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkCommandBufferBeginInfoAccessor::fromObject(VkCommandBufferBeginInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpInheritanceInfo(clazzInfo);  // VkCommandBufferInheritanceInfo Object Array
}

VkCommandBufferBeginInfoAccessor::~VkCommandBufferBeginInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

