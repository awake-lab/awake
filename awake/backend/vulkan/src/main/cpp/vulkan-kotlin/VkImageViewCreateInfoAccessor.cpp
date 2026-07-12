/*
 *  VkImageViewCreateInfoAccessor.cpp
 *  Vulkan accessor for VkImageViewCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkImageViewCreateInfoAccessor.h>

VkImageViewCreateInfoAccessor::VkImageViewCreateInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                         obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    imageField = env->GetFieldID(clazz, "image", "J");
    viewTypeField = env->GetFieldID(clazz, "viewType",
                                    "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkImageViewType;");
    formatField = env->GetFieldID(clazz, "format",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    componentsField = env->GetFieldID(clazz, "components",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkComponentMapping;");
    subresourceRangeField = env->GetFieldID(clazz, "subresourceRange",
                                            "Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkImageSubresourceRange;");
}

VkStructureType
VkImageViewCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkImageViewCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkImageViewCreateInfoAccessor::getpNext(VkImageViewCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkImage
VkImageViewCreateInfoAccessor::getimage() {
    auto value = env->GetLongField(obj, imageField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkImage>(value); // VkHandle
}

void
VkImageViewCreateInfoAccessor::getcomponents(VkImageViewCreateInfo &clazzInfo) {
    auto componentsObj = (jobject) env->GetObjectField(obj, componentsField);
    if (componentsObj == nullptr) {
        env->DeleteLocalRef(componentsObj); // Delete null object reference
        return;
    }
    VkComponentMappingAccessor accessor(env, componentsObj);
    VkComponentMapping ref{};
    accessor.fromObject(ref);
    clazzInfo.components = ref;
    env->DeleteLocalRef(componentsObj); // Delete object reference
}

VkImageViewType
VkImageViewCreateInfoAccessor::getviewType() {
    auto viewTypeEnum = (jobject) env->GetObjectField(obj, viewTypeField);
    auto enumValue = (VkImageViewType) enum_utils::getEnumFromObject(env, viewTypeEnum);
    env->DeleteLocalRef(viewTypeEnum); // release enum reference
    return enumValue;
}

void
VkImageViewCreateInfoAccessor::getsubresourceRange(VkImageViewCreateInfo &clazzInfo) {
    auto subresourceRangeObj = (jobject) env->GetObjectField(obj, subresourceRangeField);
    if (subresourceRangeObj == nullptr) {
        env->DeleteLocalRef(subresourceRangeObj); // Delete null object reference
        return;
    }
    VkImageSubresourceRangeAccessor accessor(env, subresourceRangeObj);
    VkImageSubresourceRange ref{};
    accessor.fromObject(ref);
    clazzInfo.subresourceRange = ref;
    env->DeleteLocalRef(subresourceRangeObj); // Delete object reference
}

VkFormat
VkImageViewCreateInfoAccessor::getformat() {
    auto formatEnum = (jobject) env->GetObjectField(obj, formatField);
    auto enumValue = (VkFormat) enum_utils::getEnumFromObject(env, formatEnum);
    env->DeleteLocalRef(formatEnum); // release enum reference
    return enumValue;
}

void
VkImageViewCreateInfoAccessor::fromObject(VkImageViewCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.image = getimage(); // VkHandle
    clazzInfo.viewType = getviewType(); // Enum VkImageViewType
    clazzInfo.format = getformat(); // Enum VkFormat
    getcomponents(clazzInfo); // Other VkComponentMapping
    getsubresourceRange(clazzInfo); // Other VkImageSubresourceRange
}

VkImageViewCreateInfoAccessor::~VkImageViewCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

