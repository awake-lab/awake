/*
 *  VkSpecializationMapEntryAccessor.h
 *  Vulkan accessor for VkSpecializationMapEntry
 *  Created by Ron June Valdoz */

#ifndef VKSPECIALIZATIONMAPENTRYACCESSOR_H
#define VKSPECIALIZATIONMAPENTRYACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSpecializationMapEntryAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID constantIDField;
    jfieldID offsetField;
    jfieldID sizeField;
public:
    VkSpecializationMapEntryAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkSpecializationMapEntry &clazzInfo);

    uint64_t getsize();

    uint32_t getconstantID();

    uint32_t getoffset();

    ~VkSpecializationMapEntryAccessor();
};
#endif // VKSPECIALIZATIONMAPENTRYACCESSOR_H