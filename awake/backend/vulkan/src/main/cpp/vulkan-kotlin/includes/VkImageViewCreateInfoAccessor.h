/*
 *  VkImageViewCreateInfoAccessor.h
 *  Vulkan accessor for VkImageViewCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKIMAGEVIEWCREATEINFOACCESSOR_H
#define VKIMAGEVIEWCREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkComponentMappingAccessor.h>
#include <includes/VkImageSubresourceRangeAccessor.h>

class VkImageViewCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID imageField;
    jfieldID viewTypeField;
    jfieldID formatField;
    jfieldID componentsField;
    jfieldID subresourceRangeField;
public:
    VkImageViewCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkImageViewCreateInfo &clazzInfo);

    VkImage getimage();

    void getcomponents(VkImageViewCreateInfo &clazzInfo);

    VkImageViewType getviewType();

    void getsubresourceRange(VkImageViewCreateInfo &clazzInfo);

    VkFormat getformat();

    void fromObject(VkImageViewCreateInfo &clazzInfo);

    ~VkImageViewCreateInfoAccessor();
};
#endif // VKIMAGEVIEWCREATEINFOACCESSOR_H