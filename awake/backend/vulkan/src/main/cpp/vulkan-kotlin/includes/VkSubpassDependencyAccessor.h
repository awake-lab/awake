/*
 *  VkSubpassDependencyAccessor.h
 *  Vulkan accessor for VkSubpassDependency
 *  Created by Ron June Valdoz */

#ifndef VKSUBPASSDEPENDENCYACCESSOR_H
#define VKSUBPASSDEPENDENCYACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSubpassDependencyAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID srcSubpassField;
    jfieldID dstSubpassField;
    jfieldID srcStageMaskField;
    jfieldID dstStageMaskField;
    jfieldID srcAccessMaskField;
    jfieldID dstAccessMaskField;
    jfieldID dependencyFlagsField;
public:
    VkSubpassDependencyAccessor(JNIEnv *env, jobject obj);

    uint32_t getdstStageMask();
    uint32_t getdstAccessMask();
    uint32_t getsrcSubpass();
    uint32_t getdstSubpass();
    uint32_t getdependencyFlags();
    uint32_t getsrcAccessMask();

    uint32_t getsrcStageMask();

    void fromObject(VkSubpassDependency &clazzInfo);

    ~VkSubpassDependencyAccessor();
};
#endif // VKSUBPASSDEPENDENCYACCESSOR_H