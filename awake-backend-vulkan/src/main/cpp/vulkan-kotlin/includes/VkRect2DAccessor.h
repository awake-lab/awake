/*
 *  VkRect2DAccessor.h
 *  Vulkan accessor for VkRect2D
 *  Created by Ron June Valdoz */

#ifndef VKRECT2DACCESSOR_H
#define VKRECT2DACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkOffset2DAccessor.h>
#include <includes/VkExtent2DAccessor.h>

class VkRect2DAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID offsetField;
    jfieldID extentField;
public:
    VkRect2DAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkRect2D &clazzInfo);

    void getoffset(VkRect2D &clazzInfo);

    void getextent(VkRect2D &clazzInfo);

    ~VkRect2DAccessor();
};
#endif // VKRECT2DACCESSOR_H