/*
 *  VkClearDepthStencilValueAccessor.h
 *  Vulkan accessor for VkClearDepthStencilValue
 *  Created by Ron June Valdoz */

#ifndef VKCLEARDEPTHSTENCILVALUEACCESSOR_H
#define VKCLEARDEPTHSTENCILVALUEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkClearDepthStencilValueAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID depthField;
    jfieldID stencilField;
public:
    VkClearDepthStencilValueAccessor(JNIEnv *env, jobject obj);

    uint32_t getstencil();

    void fromObject(VkClearDepthStencilValue &clazzInfo);

    float getdepth();
    ~VkClearDepthStencilValueAccessor();
};
#endif // VKCLEARDEPTHSTENCILVALUEACCESSOR_H