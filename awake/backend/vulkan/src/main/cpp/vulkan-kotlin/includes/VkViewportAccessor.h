/*
 *  VkViewportAccessor.h
 *  Vulkan accessor for VkViewport
 *  Created by Ron June Valdoz */

#ifndef VKVIEWPORTACCESSOR_H
#define VKVIEWPORTACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkViewportAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID xField;
    jfieldID yField;
    jfieldID widthField;
    jfieldID heightField;
    jfieldID minDepthField;
    jfieldID maxDepthField;
public:
    VkViewportAccessor(JNIEnv *env, jobject obj);

    float getminDepth();
    float getheight();

    float getx();

    void fromObject(VkViewport &clazzInfo);

    float gety();
    float getwidth();
    float getmaxDepth();
    ~VkViewportAccessor();
};
#endif // VKVIEWPORTACCESSOR_H