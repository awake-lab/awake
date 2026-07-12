/*
 *  VkPipelineDynamicStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineDynamicStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineDynamicStateCreateInfoAccessor.h>

VkPipelineDynamicStateCreateInfoAccessor::VkPipelineDynamicStateCreateInfoAccessor(JNIEnv *env,
                                                                                   jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pDynamicStatesField = env->GetFieldID(clazz, "pDynamicStates",
                                          "[Lio/github/ronjunevaldoz/awake/vulkan/enums/VkDynamicState;");
}

VkStructureType
VkPipelineDynamicStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineDynamicStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkPipelineDynamicStateCreateInfoAccessor::getpNext(VkPipelineDynamicStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPipelineDynamicStateCreateInfoAccessor::getpDynamicStates(
        VkPipelineDynamicStateCreateInfo &clazzInfo) {
    auto pDynamicStatesArray = (jobjectArray) env->GetObjectField(obj, pDynamicStatesField);
    if (pDynamicStatesArray == nullptr) {
        clazzInfo.dynamicStateCount = 0;
        clazzInfo.pDynamicStates = nullptr;
        env->DeleteLocalRef(pDynamicStatesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pDynamicStatesArray);
    std::vector<VkDynamicState> pDynamicStates;
    for (int i = 0; i < size; ++i) {
        auto element = (jobjectArray) env->GetObjectArrayElement(pDynamicStatesArray,
                                                                 i); // actual type is VkDynamicState[];
        pDynamicStates.push_back(static_cast<VkDynamicState>(enum_utils::getEnumFromObject(env,
                                                                                           element))); // type is enum
    }
    // processing array data
    auto dynamicStateCount = static_cast<uint32_t>(pDynamicStates.size());
    clazzInfo.dynamicStateCount = dynamicStateCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkDynamicState[size];
    std::copy(pDynamicStates.begin(), pDynamicStates.end(), copy);
    clazzInfo.pDynamicStates = copy;
    env->DeleteLocalRef(pDynamicStatesArray); // release reference
}

void
VkPipelineDynamicStateCreateInfoAccessor::fromObject(VkPipelineDynamicStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpDynamicStates(clazzInfo);  // VkDynamicState Object Array
}

VkPipelineDynamicStateCreateInfoAccessor::~VkPipelineDynamicStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

