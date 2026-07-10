/*
 *  VkOffset2DAccessor.h
 *  Vulkan accessor for VkOffset2D
 *  Created by Ron June Valdoz */

#ifndef VKOFFSET2DACCESSOR_H
#define VKOFFSET2DACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkOffset2DAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID xField;
    jfieldID yField;
public:
    VkOffset2DAccessor(JNIEnv *env, jobject obj);

    uint32_t gety();

    uint32_t getx();

    void fromObject(VkOffset2D &clazzInfo);

    ~VkOffset2DAccessor();
};
#endif // VKOFFSET2DACCESSOR_H