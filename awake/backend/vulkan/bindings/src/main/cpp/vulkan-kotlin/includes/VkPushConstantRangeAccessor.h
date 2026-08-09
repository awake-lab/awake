/*
 *  VkPushConstantRangeAccessor.h
 *  Vulkan accessor for VkPushConstantRange
 *  Created by Ron June Valdoz */

#ifndef VKPUSHCONSTANTRANGEACCESSOR_H
#define VKPUSHCONSTANTRANGEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPushConstantRangeAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID stageFlagsField;
    jfieldID offsetField;
    jfieldID sizeField;
public:
    VkPushConstantRangeAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkPushConstantRange &clazzInfo);

    uint32_t getstageFlags();

    uint32_t getsize();

    uint32_t getoffset();

    ~VkPushConstantRangeAccessor();
};
#endif // VKPUSHCONSTANTRANGEACCESSOR_H