/*
 *  VkCommandBufferAllocateInfoAccessor.h
 *  Vulkan accessor for VkCommandBufferAllocateInfo
 *  Created by Ron June Valdoz */

#ifndef VKCOMMANDBUFFERALLOCATEINFOACCESSOR_H
#define VKCOMMANDBUFFERALLOCATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkCommandBufferAllocateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID commandPoolField;
    jfieldID levelField;
    jfieldID commandBufferCountField;
public:
    VkCommandBufferAllocateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void fromObject(VkCommandBufferAllocateInfo &clazzInfo);

    VkCommandPool getcommandPool();

    VkCommandBufferLevel getlevel();

    void getpNext(VkCommandBufferAllocateInfo &clazzInfo);

    uint32_t getcommandBufferCount();
    ~VkCommandBufferAllocateInfoAccessor();
};
#endif // VKCOMMANDBUFFERALLOCATEINFOACCESSOR_H