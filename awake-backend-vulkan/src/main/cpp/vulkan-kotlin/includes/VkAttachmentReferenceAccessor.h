/*
 *  VkAttachmentReferenceAccessor.h
 *  Vulkan accessor for VkAttachmentReference
 *  Created by Ron June Valdoz */

#ifndef VKATTACHMENTREFERENCEACCESSOR_H
#define VKATTACHMENTREFERENCEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkAttachmentReferenceAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID attachmentField;
    jfieldID layoutField;
public:
    VkAttachmentReferenceAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkAttachmentReference &clazzInfo);

    VkImageLayout getlayout();

    uint32_t getattachment();

    ~VkAttachmentReferenceAccessor();
};
#endif // VKATTACHMENTREFERENCEACCESSOR_H