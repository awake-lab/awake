/*
 *  VkFenceCreateInfoAccessor.h
 *  Vulkan accessor for VkFenceCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKFENCECREATEINFOACCESSOR_H
#define VKFENCECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkFenceCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
public:
    VkFenceCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void getpNext(VkFenceCreateInfo &clazzInfo);

    uint32_t getflags();

    void fromObject(VkFenceCreateInfo &clazzInfo);

    ~VkFenceCreateInfoAccessor();
};
#endif // VKFENCECREATEINFOACCESSOR_H