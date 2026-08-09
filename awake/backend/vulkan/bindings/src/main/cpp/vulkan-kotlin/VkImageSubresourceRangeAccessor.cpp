/*
 *  VkImageSubresourceRangeAccessor.cpp
 *  Vulkan accessor for VkImageSubresourceRange
 *  Created by Ron June Valdoz */

#include <includes/VkImageSubresourceRangeAccessor.h>

VkImageSubresourceRangeAccessor::VkImageSubresourceRangeAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    aspectMaskField = env->GetFieldID(clazz, "aspectMask", "I");
    baseMipLevelField = env->GetFieldID(clazz, "baseMipLevel", "I");
    levelCountField = env->GetFieldID(clazz, "levelCount", "I");
    baseArrayLayerField = env->GetFieldID(clazz, "baseArrayLayer", "I");
    layerCountField = env->GetFieldID(clazz, "layerCount", "I");
}

uint32_t
VkImageSubresourceRangeAccessor::getlevelCount() {
    return (uint32_t) (jint) env->GetIntField(obj, levelCountField); // primitive
}

uint32_t
VkImageSubresourceRangeAccessor::getbaseMipLevel() {
    return (uint32_t) (jint) env->GetIntField(obj, baseMipLevelField); // primitive
}

void
VkImageSubresourceRangeAccessor::fromObject(VkImageSubresourceRange &clazzInfo) {
    clazzInfo.aspectMask = getaspectMask(); // Primitive uint32_t
    clazzInfo.baseMipLevel = getbaseMipLevel(); // Primitive uint32_t
    clazzInfo.levelCount = getlevelCount(); // Primitive uint32_t
    clazzInfo.baseArrayLayer = getbaseArrayLayer(); // Primitive uint32_t
    clazzInfo.layerCount = getlayerCount(); // Primitive uint32_t
}

uint32_t
VkImageSubresourceRangeAccessor::getbaseArrayLayer() {
    return (uint32_t) (jint) env->GetIntField(obj, baseArrayLayerField); // primitive
}

uint32_t
VkImageSubresourceRangeAccessor::getlayerCount() {
    return (uint32_t) (jint) env->GetIntField(obj, layerCountField); // primitive
}

uint32_t
VkImageSubresourceRangeAccessor::getaspectMask() {
    return (uint32_t) (jint) env->GetIntField(obj, aspectMaskField); // primitive
}

VkImageSubresourceRangeAccessor::~VkImageSubresourceRangeAccessor() {
    env->DeleteLocalRef(clazz);
}

