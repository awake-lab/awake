/*
 *  VkExtent3DAccessor.h
 *  Vulkan accessor for VkExtent3D
 *  Created by Ron June Valdoz */

#ifndef VKEXTENT3DACCESSOR_H
#define VKEXTENT3DACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtent3DAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID widthField;
    jfieldID heightField;
    jfieldID depthField;
public:
    VkExtent3DAccessor(JNIEnv *env, jobject obj);

    uint32_t getwidth();

    void fromObject(VkExtent3D &clazzInfo);

    uint32_t getdepth();
    uint32_t getheight();
    ~VkExtent3DAccessor();
};
#endif // VKEXTENT3DACCESSOR_H