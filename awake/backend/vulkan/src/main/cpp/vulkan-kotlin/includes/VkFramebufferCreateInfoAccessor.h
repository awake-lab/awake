/*
 *  VkFramebufferCreateInfoAccessor.h
 *  Vulkan accessor for VkFramebufferCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKFRAMEBUFFERCREATEINFOACCESSOR_H
#define VKFRAMEBUFFERCREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkFramebufferCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID renderPassField;
    jfieldID pAttachmentsField;
    jfieldID widthField;
    jfieldID heightField;
    jfieldID layersField;
public:
    VkFramebufferCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getwidth();

    void getpNext(VkFramebufferCreateInfo &clazzInfo);

    uint32_t getflags();

    void getpAttachments(VkFramebufferCreateInfo &clazzInfo);

    void fromObject(VkFramebufferCreateInfo &clazzInfo);

    uint32_t getlayers();

    VkRenderPass getrenderPass();

    uint32_t getheight();

    ~VkFramebufferCreateInfoAccessor();
};
#endif // VKFRAMEBUFFERCREATEINFOACCESSOR_H