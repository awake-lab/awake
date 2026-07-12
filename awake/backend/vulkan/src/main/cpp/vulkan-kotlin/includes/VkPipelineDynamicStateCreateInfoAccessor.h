/*
 *  VkPipelineDynamicStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineDynamicStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINEDYNAMICSTATECREATEINFOACCESSOR_H
#define VKPIPELINEDYNAMICSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineDynamicStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pDynamicStatesField;
public:
    VkPipelineDynamicStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkPipelineDynamicStateCreateInfo &clazzInfo);

    void getpDynamicStates(VkPipelineDynamicStateCreateInfo &clazzInfo);

    void fromObject(VkPipelineDynamicStateCreateInfo &clazzInfo);

    ~VkPipelineDynamicStateCreateInfoAccessor();
};
#endif // VKPIPELINEDYNAMICSTATECREATEINFOACCESSOR_H