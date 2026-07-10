/*
 *  VkExtent2DAccessor.h
 *  Vulkan accessor for VkExtent2D
 *  Created by Ron June Valdoz */

#ifndef VKEXTENT2DACCESSOR_H
#define VKEXTENT2DACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkExtent2DAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID widthField;
    jfieldID heightField;
public:
    VkExtent2DAccessor(JNIEnv *env, jobject obj);

    uint32_t getwidth();

    void fromObject(VkExtent2D &clazzInfo);

    uint32_t getheight();
    ~VkExtent2DAccessor();
};
#endif // VKEXTENT2DACCESSOR_H