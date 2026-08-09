/*
 *  VkDebugUtilsLabelEXTAccessor.h
 *  Vulkan accessor for VkDebugUtilsLabelEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSLABELEXTACCESSOR_H
#define VKDEBUGUTILSLABELEXTACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDebugUtilsLabelEXTAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID pLabelNameField;
    jfieldID colorField;
public:
    VkDebugUtilsLabelEXTAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void fromObject(VkDebugUtilsLabelEXT &clazzInfo);

    void getpLabelName(VkDebugUtilsLabelEXT &clazzInfo);

    void getcolor(VkDebugUtilsLabelEXT &clazzInfo);

    void getpNext(VkDebugUtilsLabelEXT &clazzInfo);

    ~VkDebugUtilsLabelEXTAccessor();
};
#endif // VKDEBUGUTILSLABELEXTACCESSOR_H