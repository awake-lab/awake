/*
 *  VkPipelineLayoutCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineLayoutCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineLayoutCreateInfoAccessor.h>

VkPipelineLayoutCreateInfoAccessor::VkPipelineLayoutCreateInfoAccessor(JNIEnv *env, jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pSetLayoutsField = env->GetFieldID(clazz, "pSetLayouts", "[Ljava/lang/Long;");
    pushConstantRangeCountField = env->GetFieldID(clazz, "pushConstantRangeCount", "I");
    pPushConstantRangesField = env->GetFieldID(clazz, "pPushConstantRanges",
                                               "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/pipeline/VkPushConstantRange;");
}

VkStructureType
VkPipelineLayoutCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineLayoutCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

uint32_t
VkPipelineLayoutCreateInfoAccessor::getpushConstantRangeCount() {
    return (uint32_t) (jint) env->GetIntField(obj, pushConstantRangeCountField); // primitive
}

void
VkPipelineLayoutCreateInfoAccessor::getpPushConstantRanges(VkPipelineLayoutCreateInfo &clazzInfo) {
    auto pPushConstantRangesArray = (jobjectArray) env->GetObjectField(obj,
                                                                       pPushConstantRangesField);
    if (pPushConstantRangesArray == nullptr) {
        clazzInfo.pPushConstantRanges = nullptr;
        env->DeleteLocalRef(pPushConstantRangesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pPushConstantRangesArray);
    std::vector<VkPushConstantRange> pPushConstantRanges;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pPushConstantRangesArray,
                                                            i); // actual type is VkPushConstantRange[];
        // experimental optimize accessor
        VkPushConstantRangeAccessor accessor(env, element);
        VkPushConstantRange ref{};
        accessor.fromObject(ref);
        pPushConstantRanges.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkPushConstantRange[size];
    std::copy(pPushConstantRanges.begin(), pPushConstantRanges.end(), copy);
    clazzInfo.pPushConstantRanges = copy;
    env->DeleteLocalRef(pPushConstantRangesArray); // release reference
}

void
VkPipelineLayoutCreateInfoAccessor::fromObject(VkPipelineLayoutCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpSetLayouts(clazzInfo);  // Long Object Array
    clazzInfo.pushConstantRangeCount = getpushConstantRangeCount(); // Primitive uint32_t
    getpPushConstantRanges(clazzInfo);  // VkPushConstantRange Object Array
}

void
VkPipelineLayoutCreateInfoAccessor::getpNext(VkPipelineLayoutCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPipelineLayoutCreateInfoAccessor::getpSetLayouts(VkPipelineLayoutCreateInfo &clazzInfo) {
    auto pSetLayoutsArray = (jobjectArray) env->GetObjectField(obj, pSetLayoutsField);
    if (pSetLayoutsArray == nullptr) {
        clazzInfo.setLayoutCount = 0;
        clazzInfo.pSetLayouts = nullptr;
        env->DeleteLocalRef(pSetLayoutsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pSetLayoutsArray);
    // vkhandle array?
    std::vector<VkDescriptorSetLayout> pSetLayouts;
    for (int i = 0; i < size; ++i) {
        auto element = env->GetObjectArrayElement(pSetLayoutsArray, i);
        jmethodID getValueMethod = env->GetMethodID(env->GetObjectClass(element), "longValue",
                                                    "()J");
        jlong value = env->CallLongMethod(element, getValueMethod);
        pSetLayouts.push_back(reinterpret_cast<VkDescriptorSetLayout>(value)); //vkhandle
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto setLayoutCount = static_cast<uint32_t>(pSetLayouts.size());
    clazzInfo.setLayoutCount = setLayoutCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkDescriptorSetLayout[size];
    std::copy(pSetLayouts.begin(), pSetLayouts.end(), copy);
    clazzInfo.pSetLayouts = copy;
    env->DeleteLocalRef(pSetLayoutsArray); // release reference
}

VkPipelineLayoutCreateInfoAccessor::~VkPipelineLayoutCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

