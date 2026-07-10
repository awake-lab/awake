/*
 *  VkAttachmentReferenceAccessor.cpp
 *  Vulkan accessor for VkAttachmentReference
 *  Created by Ron June Valdoz */

#include <includes/VkAttachmentReferenceAccessor.h>

VkAttachmentReferenceAccessor::VkAttachmentReferenceAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                         obj(obj) {
    clazz = env->GetObjectClass(obj);
    attachmentField = env->GetFieldID(clazz, "attachment", "I");
    layoutField = env->GetFieldID(clazz, "layout",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkImageLayout;");
}

void
VkAttachmentReferenceAccessor::fromObject(VkAttachmentReference &clazzInfo) {
    clazzInfo.attachment = getattachment(); // Primitive uint32_t
    clazzInfo.layout = getlayout(); // Enum VkImageLayout
}

VkImageLayout
VkAttachmentReferenceAccessor::getlayout() {
    auto layoutEnum = (jobject) env->GetObjectField(obj, layoutField);
    auto enumValue = (VkImageLayout) enum_utils::getEnumFromObject(env, layoutEnum);
    env->DeleteLocalRef(layoutEnum); // release enum reference
    return enumValue;
}

uint32_t
VkAttachmentReferenceAccessor::getattachment() {
    return (uint32_t) (jint) env->GetIntField(obj, attachmentField); // primitive
}

VkAttachmentReferenceAccessor::~VkAttachmentReferenceAccessor() {
    env->DeleteLocalRef(clazz);
}

