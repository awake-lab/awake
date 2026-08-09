/*
 *  VkRenderPassCreateInfoAccessor.h
 *  Vulkan accessor for VkRenderPassCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKRENDERPASSCREATEINFOACCESSOR_H
#define VKRENDERPASSCREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkAttachmentDescriptionAccessor.h>
#include <includes/VkSubpassDescriptionAccessor.h>
#include <includes/VkSubpassDependencyAccessor.h>

class VkRenderPassCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pAttachmentsField;
    jfieldID pSubpassesField;
    jfieldID pDependenciesField;
public:
    VkRenderPassCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkRenderPassCreateInfo &clazzInfo);

    void getpAttachments(VkRenderPassCreateInfo &clazzInfo);

    void getpDependencies(VkRenderPassCreateInfo &clazzInfo);

    void fromObject(VkRenderPassCreateInfo &clazzInfo);

    void getpSubpasses(VkRenderPassCreateInfo &clazzInfo);

    ~VkRenderPassCreateInfoAccessor();
};
#endif // VKRENDERPASSCREATEINFOACCESSOR_H