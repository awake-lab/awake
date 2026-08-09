/*
 *  VkComponentMappingAccessor.h
 *  Vulkan accessor for VkComponentMapping
 *  Created by Ron June Valdoz */

#ifndef VKCOMPONENTMAPPINGACCESSOR_H
#define VKCOMPONENTMAPPINGACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkComponentMappingAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID rField;
    jfieldID gField;
    jfieldID bField;
    jfieldID aField;
public:
    VkComponentMappingAccessor(JNIEnv *env, jobject obj);

    VkComponentSwizzle getr();

    void fromObject(VkComponentMapping &clazzInfo);

    VkComponentSwizzle getb();
    VkComponentSwizzle geta();
    VkComponentSwizzle getg();
    ~VkComponentMappingAccessor();
};
#endif // VKCOMPONENTMAPPINGACCESSOR_H