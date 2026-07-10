/*
 *  VkCommandBufferBeginInfoAccessor.h
 *  Vulkan accessor for VkCommandBufferBeginInfo
 *  Created by Ron June Valdoz */

#ifndef VKCOMMANDBUFFERBEGININFOACCESSOR_H
#define VKCOMMANDBUFFERBEGININFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkCommandBufferInheritanceInfoAccessor.h>

class VkCommandBufferBeginInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pInheritanceInfoField;
public:
    VkCommandBufferBeginInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpInheritanceInfo(VkCommandBufferBeginInfo &clazzInfo);

    void getpNext(VkCommandBufferBeginInfo &clazzInfo);

    void fromObject(VkCommandBufferBeginInfo &clazzInfo);

    ~VkCommandBufferBeginInfoAccessor();
};
#endif // VKCOMMANDBUFFERBEGININFOACCESSOR_H