/*
 *  VkAttachmentDescriptionAccessor.cpp
 *  Vulkan accessor for VkAttachmentDescription
 *  Created by Ron June Valdoz */

#include <includes/VkAttachmentDescriptionAccessor.h>

VkAttachmentDescriptionAccessor::VkAttachmentDescriptionAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    flagsField = env->GetFieldID(clazz, "flags", "I");
    formatField = env->GetFieldID(clazz, "format",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    samplesField = env->GetFieldID(clazz, "samples",
                                   "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkSampleCountFlagBits;");
    loadOpField = env->GetFieldID(clazz, "loadOp",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkAttachmentLoadOp;");
    storeOpField = env->GetFieldID(clazz, "storeOp",
                                   "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkAttachmentStoreOp;");
    stencilLoadOpField = env->GetFieldID(clazz, "stencilLoadOp",
                                         "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkAttachmentLoadOp;");
    stencilStoreOpField = env->GetFieldID(clazz, "stencilStoreOp",
                                          "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkAttachmentStoreOp;");
    initialLayoutField = env->GetFieldID(clazz, "initialLayout",
                                         "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkImageLayout;");
    finalLayoutField = env->GetFieldID(clazz, "finalLayout",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkImageLayout;");
}

uint32_t
VkAttachmentDescriptionAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

VkSampleCountFlagBits
VkAttachmentDescriptionAccessor::getsamples() {
    auto samplesEnum = (jobject) env->GetObjectField(obj, samplesField);
    auto enumValue = (VkSampleCountFlagBits) enum_utils::getEnumFromObject(env, samplesEnum);
    env->DeleteLocalRef(samplesEnum); // release enum reference
    return enumValue;
}

VkAttachmentStoreOp
VkAttachmentDescriptionAccessor::getstencilStoreOp() {
    auto stencilStoreOpEnum = (jobject) env->GetObjectField(obj, stencilStoreOpField);
    auto enumValue = (VkAttachmentStoreOp) enum_utils::getEnumFromObject(env, stencilStoreOpEnum);
    env->DeleteLocalRef(stencilStoreOpEnum); // release enum reference
    return enumValue;
}

VkAttachmentLoadOp
VkAttachmentDescriptionAccessor::getloadOp() {
    auto loadOpEnum = (jobject) env->GetObjectField(obj, loadOpField);
    auto enumValue = (VkAttachmentLoadOp) enum_utils::getEnumFromObject(env, loadOpEnum);
    env->DeleteLocalRef(loadOpEnum); // release enum reference
    return enumValue;
}

VkImageLayout
VkAttachmentDescriptionAccessor::getfinalLayout() {
    auto finalLayoutEnum = (jobject) env->GetObjectField(obj, finalLayoutField);
    auto enumValue = (VkImageLayout) enum_utils::getEnumFromObject(env, finalLayoutEnum);
    env->DeleteLocalRef(finalLayoutEnum); // release enum reference
    return enumValue;
}

VkAttachmentStoreOp
VkAttachmentDescriptionAccessor::getstoreOp() {
    auto storeOpEnum = (jobject) env->GetObjectField(obj, storeOpField);
    auto enumValue = (VkAttachmentStoreOp) enum_utils::getEnumFromObject(env, storeOpEnum);
    env->DeleteLocalRef(storeOpEnum); // release enum reference
    return enumValue;
}

VkImageLayout
VkAttachmentDescriptionAccessor::getinitialLayout() {
    auto initialLayoutEnum = (jobject) env->GetObjectField(obj, initialLayoutField);
    auto enumValue = (VkImageLayout) enum_utils::getEnumFromObject(env, initialLayoutEnum);
    env->DeleteLocalRef(initialLayoutEnum); // release enum reference
    return enumValue;
}

VkFormat
VkAttachmentDescriptionAccessor::getformat() {
    auto formatEnum = (jobject) env->GetObjectField(obj, formatField);
    auto enumValue = (VkFormat) enum_utils::getEnumFromObject(env, formatEnum);
    env->DeleteLocalRef(formatEnum); // release enum reference
    return enumValue;
}

VkAttachmentLoadOp
VkAttachmentDescriptionAccessor::getstencilLoadOp() {
    auto stencilLoadOpEnum = (jobject) env->GetObjectField(obj, stencilLoadOpField);
    auto enumValue = (VkAttachmentLoadOp) enum_utils::getEnumFromObject(env, stencilLoadOpEnum);
    env->DeleteLocalRef(stencilLoadOpEnum); // release enum reference
    return enumValue;
}

void
VkAttachmentDescriptionAccessor::fromObject(VkAttachmentDescription &clazzInfo) {
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.format = getformat(); // Enum VkFormat
    clazzInfo.samples = getsamples(); // Enum VkSampleCountFlagBits
    clazzInfo.loadOp = getloadOp(); // Enum VkAttachmentLoadOp
    clazzInfo.storeOp = getstoreOp(); // Enum VkAttachmentStoreOp
    clazzInfo.stencilLoadOp = getstencilLoadOp(); // Enum VkAttachmentLoadOp
    clazzInfo.stencilStoreOp = getstencilStoreOp(); // Enum VkAttachmentStoreOp
    clazzInfo.initialLayout = getinitialLayout(); // Enum VkImageLayout
    clazzInfo.finalLayout = getfinalLayout(); // Enum VkImageLayout
}

VkAttachmentDescriptionAccessor::~VkAttachmentDescriptionAccessor() {
    env->DeleteLocalRef(clazz);
}

