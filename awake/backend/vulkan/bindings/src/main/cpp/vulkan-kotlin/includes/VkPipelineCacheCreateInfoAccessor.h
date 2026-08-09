/*
 *  VkPipelineCacheCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineCacheCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINECACHECREATEINFOACCESSOR_H
#define VKPIPELINECACHECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineCacheCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pInitialDataField;
public:
    VkPipelineCacheCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void fromObject(VkPipelineCacheCreateInfo &clazzInfo);

    void getpInitialData(VkPipelineCacheCreateInfo &clazzInfo);

    void getpNext(VkPipelineCacheCreateInfo &clazzInfo);

    ~VkPipelineCacheCreateInfoAccessor();
};
#endif // VKPIPELINECACHECREATEINFOACCESSOR_H