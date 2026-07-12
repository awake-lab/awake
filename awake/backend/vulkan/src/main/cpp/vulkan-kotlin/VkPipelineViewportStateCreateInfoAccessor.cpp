/*
 *  VkPipelineViewportStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineViewportStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineViewportStateCreateInfoAccessor.h>

VkPipelineViewportStateCreateInfoAccessor::VkPipelineViewportStateCreateInfoAccessor(JNIEnv *env,
                                                                                     jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pViewportsField = env->GetFieldID(clazz, "pViewports",
                                      "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkViewport;");
    pScissorsField = env->GetFieldID(clazz, "pScissors",
                                     "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkRect2D;");
}

VkStructureType
VkPipelineViewportStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineViewportStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkPipelineViewportStateCreateInfoAccessor::getpNext(VkPipelineViewportStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPipelineViewportStateCreateInfoAccessor::fromObject(
        VkPipelineViewportStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpViewports(clazzInfo);  // VkViewport Object Array
    getpScissors(clazzInfo);  // VkRect2D Object Array
}

void
VkPipelineViewportStateCreateInfoAccessor::getpViewports(
        VkPipelineViewportStateCreateInfo &clazzInfo) {
    auto pViewportsArray = (jobjectArray) env->GetObjectField(obj, pViewportsField);
    if (pViewportsArray == nullptr) {
        clazzInfo.viewportCount = 0;
        clazzInfo.pViewports = nullptr;
        env->DeleteLocalRef(pViewportsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pViewportsArray);
    std::vector<VkViewport> pViewports;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pViewportsArray,
                                                            i); // actual type is VkViewport[];
        // experimental optimize accessor
        VkViewportAccessor accessor(env, element);
        VkViewport ref{};
        accessor.fromObject(ref);
        pViewports.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto viewportCount = static_cast<uint32_t>(pViewports.size());
    clazzInfo.viewportCount = viewportCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkViewport[size];
    std::copy(pViewports.begin(), pViewports.end(), copy);
    clazzInfo.pViewports = copy;
    env->DeleteLocalRef(pViewportsArray); // release reference
}

void
VkPipelineViewportStateCreateInfoAccessor::getpScissors(
        VkPipelineViewportStateCreateInfo &clazzInfo) {
    auto pScissorsArray = (jobjectArray) env->GetObjectField(obj, pScissorsField);
    if (pScissorsArray == nullptr) {
        clazzInfo.scissorCount = 0;
        clazzInfo.pScissors = nullptr;
        env->DeleteLocalRef(pScissorsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pScissorsArray);
    std::vector<VkRect2D> pScissors;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pScissorsArray,
                                                            i); // actual type is VkRect2D[];
        // experimental optimize accessor
        VkRect2DAccessor accessor(env, element);
        VkRect2D ref{};
        accessor.fromObject(ref);
        pScissors.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto scissorCount = static_cast<uint32_t>(pScissors.size());
    clazzInfo.scissorCount = scissorCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkRect2D[size];
    std::copy(pScissors.begin(), pScissors.end(), copy);
    clazzInfo.pScissors = copy;
    env->DeleteLocalRef(pScissorsArray); // release reference
}

VkPipelineViewportStateCreateInfoAccessor::~VkPipelineViewportStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

