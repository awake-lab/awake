/*
 *  VkShaderModuleCreateInfoAccessor.h
 *  Vulkan accessor for VkShaderModuleCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKSHADERMODULECREATEINFOACCESSOR_H
#define VKSHADERMODULECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkShaderModuleCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pCodeField;
public:
    VkShaderModuleCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void getpCode(VkShaderModuleCreateInfo &clazzInfo);

    uint32_t getflags();

    void fromObject(VkShaderModuleCreateInfo &clazzInfo);

    void getpNext(VkShaderModuleCreateInfo &clazzInfo);

    ~VkShaderModuleCreateInfoAccessor();
};
#endif // VKSHADERMODULECREATEINFOACCESSOR_H