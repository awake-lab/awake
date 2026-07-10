/*
 *  VkPipelineDepthStencilStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineDepthStencilStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINEDEPTHSTENCILSTATECREATEINFOACCESSOR_H
#define VKPIPELINEDEPTHSTENCILSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkStencilOpStateAccessor.h>

class VkPipelineDepthStencilStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID depthTestEnableField;
    jfieldID depthWriteEnableField;
    jfieldID depthCompareOpField;
    jfieldID depthBoundsTestEnableField;
    jfieldID stencilTestEnableField;
    jfieldID frontField;
    jfieldID backField;
    jfieldID minDepthBoundsField;
    jfieldID maxDepthBoundsField;
public:
    VkPipelineDepthStencilStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    float getmaxDepthBounds();

    VkBool32 getdepthWriteEnable();

    void getfront(VkPipelineDepthStencilStateCreateInfo &clazzInfo);

    void getpNext(VkPipelineDepthStencilStateCreateInfo &clazzInfo);

    VkStructureType getsType();

    uint32_t getflags();

    VkBool32 getdepthTestEnable();

    VkBool32 getdepthBoundsTestEnable();

    void fromObject(VkPipelineDepthStencilStateCreateInfo &clazzInfo);

    VkBool32 getstencilTestEnable();

    float getminDepthBounds();

    void getback(VkPipelineDepthStencilStateCreateInfo &clazzInfo);

    VkCompareOp getdepthCompareOp();

    ~VkPipelineDepthStencilStateCreateInfoAccessor();
};
#endif // VKPIPELINEDEPTHSTENCILSTATECREATEINFOACCESSOR_H