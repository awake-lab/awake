/*
 *  VkImageSubresourceRangeAccessor.h
 *  Vulkan accessor for VkImageSubresourceRange
 *  Created by Ron June Valdoz */

#ifndef VKIMAGESUBRESOURCERANGEACCESSOR_H
#define VKIMAGESUBRESOURCERANGEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkImageSubresourceRangeAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID aspectMaskField;
    jfieldID baseMipLevelField;
    jfieldID levelCountField;
    jfieldID baseArrayLayerField;
    jfieldID layerCountField;
public:
    VkImageSubresourceRangeAccessor(JNIEnv *env, jobject obj);

    uint32_t getlevelCount();

    uint32_t getbaseMipLevel();

    void fromObject(VkImageSubresourceRange &clazzInfo);

    uint32_t getbaseArrayLayer();
    uint32_t getlayerCount();
    uint32_t getaspectMask();
    ~VkImageSubresourceRangeAccessor();
};
#endif // VKIMAGESUBRESOURCERANGEACCESSOR_H