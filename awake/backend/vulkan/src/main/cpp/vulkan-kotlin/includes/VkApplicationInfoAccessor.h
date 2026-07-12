/*
 *  VkApplicationInfoAccessor.h
 *  Vulkan accessor for VkApplicationInfo
 *  Created by Ron June Valdoz */

#ifndef VKAPPLICATIONINFOACCESSOR_H
#define VKAPPLICATIONINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkApplicationInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID pApplicationNameField;
    jfieldID applicationVersionField;
    jfieldID pEngineNameField;
    jfieldID engineVersionField;
    jfieldID apiVersionField;
public:
    VkApplicationInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    uint32_t getapplicationVersion();

    void fromObject(VkApplicationInfo &clazzInfo);

    void getpNext(VkApplicationInfo &clazzInfo);

    void getpApplicationName(VkApplicationInfo &clazzInfo);

    uint32_t getapiVersion();

    uint32_t getengineVersion();

    void getpEngineName(VkApplicationInfo &clazzInfo);

    ~VkApplicationInfoAccessor();
};
#endif // VKAPPLICATIONINFOACCESSOR_H