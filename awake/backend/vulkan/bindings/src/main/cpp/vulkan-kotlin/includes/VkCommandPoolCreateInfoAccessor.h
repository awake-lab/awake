/*
 *  VkCommandPoolCreateInfoAccessor.h
 *  Vulkan accessor for VkCommandPoolCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKCOMMANDPOOLCREATEINFOACCESSOR_H
#define VKCOMMANDPOOLCREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkCommandPoolCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID queueFamilyIndexField;
public:
    VkCommandPoolCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getqueueFamilyIndex();

    uint32_t getflags();

    void getpNext(VkCommandPoolCreateInfo &clazzInfo);

    void fromObject(VkCommandPoolCreateInfo &clazzInfo);

    ~VkCommandPoolCreateInfoAccessor();
};
#endif // VKCOMMANDPOOLCREATEINFOACCESSOR_H