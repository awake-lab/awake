/*
 *  VkDebugUtilsObjectNameInfoEXTAccessor.h
 *  Vulkan accessor for VkDebugUtilsObjectNameInfoEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSOBJECTNAMEINFOEXTACCESSOR_H
#define VKDEBUGUTILSOBJECTNAMEINFOEXTACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDebugUtilsObjectNameInfoEXTAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID objectTypeField;
    jfieldID objectHandleField;
    jfieldID pObjectNameField;
public:
    VkDebugUtilsObjectNameInfoEXTAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void fromObject(VkDebugUtilsObjectNameInfoEXT &clazzInfo);

    uint64_t getobjectHandle();

    VkObjectType getobjectType();

    void getpNext(VkDebugUtilsObjectNameInfoEXT &clazzInfo);

    void getpObjectName(VkDebugUtilsObjectNameInfoEXT &clazzInfo);

    ~VkDebugUtilsObjectNameInfoEXTAccessor();
};
#endif // VKDEBUGUTILSOBJECTNAMEINFOEXTACCESSOR_H