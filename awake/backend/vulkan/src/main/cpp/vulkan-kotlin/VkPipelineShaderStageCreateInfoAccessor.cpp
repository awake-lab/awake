/*
 *  VkPipelineShaderStageCreateInfoAccessor.cpp
 *  Vulkan accessor for VkPipelineShaderStageCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkPipelineShaderStageCreateInfoAccessor.h>

VkPipelineShaderStageCreateInfoAccessor::VkPipelineShaderStageCreateInfoAccessor(JNIEnv *env,
                                                                                 jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    stageField = env->GetFieldID(clazz, "stage",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkShaderStageFlagBits;");
    moduleField = env->GetFieldID(clazz, "module", "J");
    pNameField = env->GetFieldID(clazz, "pName", "Ljava/lang/String;");
    pSpecializationInfoField = env->GetFieldID(clazz, "pSpecializationInfo",
                                               "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/pipeline/VkSpecializationInfo;");
}

VkStructureType
VkPipelineShaderStageCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkPipelineShaderStageCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkPipelineShaderStageCreateInfoAccessor::getpSpecializationInfo(
        VkPipelineShaderStageCreateInfo &clazzInfo) {
    auto pSpecializationInfoArray = (jobjectArray) env->GetObjectField(obj,
                                                                       pSpecializationInfoField);
    if (pSpecializationInfoArray == nullptr) {
        clazzInfo.pSpecializationInfo = nullptr;
        env->DeleteLocalRef(pSpecializationInfoArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pSpecializationInfoArray);
    std::vector<VkSpecializationInfo> pSpecializationInfo;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pSpecializationInfoArray,
                                                            i); // actual type is VkSpecializationInfo[];
        // experimental optimize accessor
        VkSpecializationInfoAccessor accessor(env, element);
        VkSpecializationInfo ref{};
        accessor.fromObject(ref);
        pSpecializationInfo.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSpecializationInfo[size];
    std::copy(pSpecializationInfo.begin(), pSpecializationInfo.end(), copy);
    clazzInfo.pSpecializationInfo = copy;
    env->DeleteLocalRef(pSpecializationInfoArray); // release reference
}

void
VkPipelineShaderStageCreateInfoAccessor::getpName(VkPipelineShaderStageCreateInfo &clazzInfo) {
    auto pNameObj = (jstring) env->GetObjectField(obj, pNameField);
    auto str = (const char *) env->GetStringUTFChars(pNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(pNameObj, str); // Release the local string reference
    clazzInfo.pName = result;
}

VkShaderModule
VkPipelineShaderStageCreateInfoAccessor::getmodule() {
    auto value = env->GetLongField(obj, moduleField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkShaderModule>(value); // VkHandle
}

void
VkPipelineShaderStageCreateInfoAccessor::fromObject(VkPipelineShaderStageCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.stage = getstage(); // Enum VkShaderStageFlagBits
    clazzInfo.module = getmodule(); // VkHandle
    getpName(clazzInfo); // Other const char*
    getpSpecializationInfo(clazzInfo);  // VkSpecializationInfo Object Array
}

void
VkPipelineShaderStageCreateInfoAccessor::getpNext(VkPipelineShaderStageCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkShaderStageFlagBits
VkPipelineShaderStageCreateInfoAccessor::getstage() {
    auto stageEnum = (jobject) env->GetObjectField(obj, stageField);
    auto enumValue = (VkShaderStageFlagBits) enum_utils::getEnumFromObject(env, stageEnum);
    env->DeleteLocalRef(stageEnum); // release enum reference
    return enumValue;
}

VkPipelineShaderStageCreateInfoAccessor::~VkPipelineShaderStageCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

