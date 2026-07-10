/*
 *  VkVertexInputAttributeDescriptionAccessor.cpp
 *  Vulkan accessor for VkVertexInputAttributeDescription
 *  Created by Ron June Valdoz */

#include <includes/VkVertexInputAttributeDescriptionAccessor.h>

VkVertexInputAttributeDescriptionAccessor::VkVertexInputAttributeDescriptionAccessor(JNIEnv *env,
                                                                                     jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    locationField = env->GetFieldID(clazz, "location", "I");
    bindingField = env->GetFieldID(clazz, "binding", "I");
    formatField = env->GetFieldID(clazz, "format",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    offsetField = env->GetFieldID(clazz, "offset", "I");
}

uint32_t
VkVertexInputAttributeDescriptionAccessor::getlocation() {
    return (uint32_t) (jint) env->GetIntField(obj, locationField); // primitive
}

uint32_t
VkVertexInputAttributeDescriptionAccessor::getbinding() {
    return (uint32_t) (jint) env->GetIntField(obj, bindingField); // primitive
}

void
VkVertexInputAttributeDescriptionAccessor::fromObject(
        VkVertexInputAttributeDescription &clazzInfo) {
    clazzInfo.location = getlocation(); // Primitive uint32_t
    clazzInfo.binding = getbinding(); // Primitive uint32_t
    clazzInfo.format = getformat(); // Enum VkFormat
    clazzInfo.offset = getoffset(); // Primitive uint32_t
}

VkFormat
VkVertexInputAttributeDescriptionAccessor::getformat() {
    auto formatEnum = (jobject) env->GetObjectField(obj, formatField);
    auto enumValue = (VkFormat) enum_utils::getEnumFromObject(env, formatEnum);
    env->DeleteLocalRef(formatEnum); // release enum reference
    return enumValue;
}

uint32_t
VkVertexInputAttributeDescriptionAccessor::getoffset() {
    return (uint32_t) (jint) env->GetIntField(obj, offsetField); // primitive
}

VkVertexInputAttributeDescriptionAccessor::~VkVertexInputAttributeDescriptionAccessor() {
    env->DeleteLocalRef(clazz);
}

