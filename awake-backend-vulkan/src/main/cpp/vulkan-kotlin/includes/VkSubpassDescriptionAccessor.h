/*
 *  VkSubpassDescriptionAccessor.h
 *  Vulkan accessor for VkSubpassDescription
 *  Created by Ron June Valdoz */

#ifndef VKSUBPASSDESCRIPTIONACCESSOR_H
#define VKSUBPASSDESCRIPTIONACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkAttachmentReferenceAccessor.h>

class VkSubpassDescriptionAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID flagsField;
    jfieldID pipelineBindPointField;
    jfieldID pInputAttachmentsField;
    jfieldID pColorAttachmentsField;
    jfieldID pResolveAttachmentsField;
    jfieldID pDepthStencilAttachmentField;
    jfieldID pPreserveAttachmentsField;
public:
    VkSubpassDescriptionAccessor(JNIEnv *env, jobject obj);

    uint32_t getflags();

    void getpInputAttachments(VkSubpassDescription &clazzInfo);

    void getpColorAttachments(VkSubpassDescription &clazzInfo);

    VkPipelineBindPoint getpipelineBindPoint();

    void getpResolveAttachments(VkSubpassDescription &clazzInfo);

    void fromObject(VkSubpassDescription &clazzInfo);

    void getpPreserveAttachments(VkSubpassDescription &clazzInfo);

    void getpDepthStencilAttachment(VkSubpassDescription &clazzInfo);

    ~VkSubpassDescriptionAccessor();
};
#endif // VKSUBPASSDESCRIPTIONACCESSOR_H