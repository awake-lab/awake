/*
 *  VkInstanceCreateInfoAccessor.h
 *  Vulkan accessor for VkInstanceCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKINSTANCECREATEINFOACCESSOR_H
#define VKINSTANCECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkApplicationInfoAccessor.h>

class VkInstanceCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID pApplicationInfoField;
    jfieldID ppEnabledLayerNamesField;
    jfieldID ppEnabledExtensionNamesField;
public:
    VkInstanceCreateInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getflags();

    void getppEnabledExtensionNames(VkInstanceCreateInfo &clazzInfo);

    void getpNext(VkInstanceCreateInfo &clazzInfo);

    void fromObject(VkInstanceCreateInfo &clazzInfo);

    void getpApplicationInfo(VkInstanceCreateInfo &clazzInfo);

    void getppEnabledLayerNames(VkInstanceCreateInfo &clazzInfo);

    ~VkInstanceCreateInfoAccessor();
};
#endif // VKINSTANCECREATEINFOACCESSOR_H