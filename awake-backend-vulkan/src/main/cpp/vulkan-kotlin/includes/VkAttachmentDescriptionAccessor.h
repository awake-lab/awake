/*
 *  VkAttachmentDescriptionAccessor.h
 *  Vulkan accessor for VkAttachmentDescription
 *  Created by Ron June Valdoz */

#ifndef VKATTACHMENTDESCRIPTIONACCESSOR_H
#define VKATTACHMENTDESCRIPTIONACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkAttachmentDescriptionAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID flagsField;
    jfieldID formatField;
    jfieldID samplesField;
    jfieldID loadOpField;
    jfieldID storeOpField;
    jfieldID stencilLoadOpField;
    jfieldID stencilStoreOpField;
    jfieldID initialLayoutField;
    jfieldID finalLayoutField;
public:
    VkAttachmentDescriptionAccessor(JNIEnv *env, jobject obj);

    uint32_t getflags();
    VkSampleCountFlagBits getsamples();
    VkAttachmentStoreOp getstencilStoreOp();
    VkAttachmentLoadOp getloadOp();
    VkImageLayout getfinalLayout();
    VkAttachmentStoreOp getstoreOp();
    VkImageLayout getinitialLayout();
    VkFormat getformat();

    VkAttachmentLoadOp getstencilLoadOp();

    void fromObject(VkAttachmentDescription &clazzInfo);

    ~VkAttachmentDescriptionAccessor();
};
#endif // VKATTACHMENTDESCRIPTIONACCESSOR_H