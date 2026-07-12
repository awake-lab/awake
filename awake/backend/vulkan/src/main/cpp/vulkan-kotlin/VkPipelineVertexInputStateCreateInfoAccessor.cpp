/*
 *  VkPipelineVertexInputStateCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineVertexInputStateCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineVertexInputStateCreateInfoAccessor.h>

VkPipelineVertexInputStateCreateInfoAccessor::VkPipelineVertexInputStateCreateInfoAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pVertexBindingDescriptionsField = env->GetFieldID(clazz, "pVertexBindingDescriptions",
                                                      "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/pipeline/VkVertexInputBindingDescription;");
    pVertexAttributeDescriptionsField = env->GetFieldID(clazz, "pVertexAttributeDescriptions",
                                                        "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/pipeline/VkVertexInputAttributeDescription;");
}

VkStructureType
VkPipelineVertexInputStateCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineVertexInputStateCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkPipelineVertexInputStateCreateInfoAccessor::fromObject(
        VkPipelineVertexInputStateCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpVertexBindingDescriptions(clazzInfo);  // VkVertexInputBindingDescription Object Array
    getpVertexAttributeDescriptions(clazzInfo);  // VkVertexInputAttributeDescription Object Array
}

void
VkPipelineVertexInputStateCreateInfoAccessor::getpNext(
        VkPipelineVertexInputStateCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPipelineVertexInputStateCreateInfoAccessor::getpVertexAttributeDescriptions(
        VkPipelineVertexInputStateCreateInfo &clazzInfo) {
    auto pVertexAttributeDescriptionsArray = (jobjectArray) env->GetObjectField(obj,
                                                                                pVertexAttributeDescriptionsField);
    if (pVertexAttributeDescriptionsArray == nullptr) {
        clazzInfo.vertexAttributeDescriptionCount = 0;
        clazzInfo.pVertexAttributeDescriptions = nullptr;
        env->DeleteLocalRef(pVertexAttributeDescriptionsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pVertexAttributeDescriptionsArray);
    std::vector<VkVertexInputAttributeDescription> pVertexAttributeDescriptions;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pVertexAttributeDescriptionsArray,
                                                            i); // actual type is VkVertexInputAttributeDescription[];
        // experimental optimize accessor
        VkVertexInputAttributeDescriptionAccessor accessor(env, element);
        VkVertexInputAttributeDescription ref{};
        accessor.fromObject(ref);
        pVertexAttributeDescriptions.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto vertexAttributeDescriptionCount = static_cast<uint32_t>(pVertexAttributeDescriptions.size());
    clazzInfo.vertexAttributeDescriptionCount = vertexAttributeDescriptionCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkVertexInputAttributeDescription[size];
    std::copy(pVertexAttributeDescriptions.begin(), pVertexAttributeDescriptions.end(), copy);
    clazzInfo.pVertexAttributeDescriptions = copy;
    env->DeleteLocalRef(pVertexAttributeDescriptionsArray); // release reference
}

void
VkPipelineVertexInputStateCreateInfoAccessor::getpVertexBindingDescriptions(
        VkPipelineVertexInputStateCreateInfo &clazzInfo) {
    auto pVertexBindingDescriptionsArray = (jobjectArray) env->GetObjectField(obj,
                                                                              pVertexBindingDescriptionsField);
    if (pVertexBindingDescriptionsArray == nullptr) {
        clazzInfo.vertexBindingDescriptionCount = 0;
        clazzInfo.pVertexBindingDescriptions = nullptr;
        env->DeleteLocalRef(pVertexBindingDescriptionsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pVertexBindingDescriptionsArray);
    std::vector<VkVertexInputBindingDescription> pVertexBindingDescriptions;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pVertexBindingDescriptionsArray,
                                                            i); // actual type is VkVertexInputBindingDescription[];
        // experimental optimize accessor
        VkVertexInputBindingDescriptionAccessor accessor(env, element);
        VkVertexInputBindingDescription ref{};
        accessor.fromObject(ref);
        pVertexBindingDescriptions.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto vertexBindingDescriptionCount = static_cast<uint32_t>(pVertexBindingDescriptions.size());
    clazzInfo.vertexBindingDescriptionCount = vertexBindingDescriptionCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkVertexInputBindingDescription[size];
    std::copy(pVertexBindingDescriptions.begin(), pVertexBindingDescriptions.end(), copy);
    clazzInfo.pVertexBindingDescriptions = copy;
    env->DeleteLocalRef(pVertexBindingDescriptionsArray); // release reference
}

VkPipelineVertexInputStateCreateInfoAccessor::~VkPipelineVertexInputStateCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

