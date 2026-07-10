/*
 *  VkPipelineTessellationStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineTessellationStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINETESSELLATIONSTATECREATEINFOACCESSOR_H
#define VKPIPELINETESSELLATIONSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineTessellationStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID patchControlPointsField;
public:
    VkPipelineTessellationStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getpNext(VkPipelineTessellationStateCreateInfo &clazzInfo);

    void fromObject(VkPipelineTessellationStateCreateInfo &clazzInfo);

    uint32_t getpatchControlPoints();

    ~VkPipelineTessellationStateCreateInfoAccessor();
};
#endif // VKPIPELINETESSELLATIONSTATECREATEINFOACCESSOR_H