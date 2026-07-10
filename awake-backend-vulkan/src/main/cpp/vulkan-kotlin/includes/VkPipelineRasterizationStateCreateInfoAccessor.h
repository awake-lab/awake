/*
 *  VkPipelineRasterizationStateCreateInfoAccessor.h
 *  Vulkan accessor for VkPipelineRasterizationStateCreateInfo
 *  Created by Ron June Valdoz */

#ifndef VKPIPELINERASTERIZATIONSTATECREATEINFOACCESSOR_H
#define VKPIPELINERASTERIZATIONSTATECREATEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPipelineRasterizationStateCreateInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID depthClampEnableField;
    jfieldID rasterizerDiscardEnableField;
    jfieldID polygonModeField;
    jfieldID cullModeField;
    jfieldID frontFaceField;
    jfieldID depthBiasEnableField;
    jfieldID depthBiasConstantFactorField;
    jfieldID depthBiasClampField;
    jfieldID depthBiasSlopeFactorField;
    jfieldID lineWidthField;
public:
    VkPipelineRasterizationStateCreateInfoAccessor(JNIEnv *env, jobject obj);

    float getlineWidth();
    VkPolygonMode getpolygonMode();
    float getdepthBiasConstantFactor();
    float getdepthBiasClamp();
    VkFrontFace getfrontFace();

    VkStructureType getsType();

    void getpNext(VkPipelineRasterizationStateCreateInfo &clazzInfo);

    uint32_t getflags();
    float getdepthBiasSlopeFactor();
    VkBool32 getdepthBiasEnable();

    VkBool32 getdepthClampEnable();

    void fromObject(VkPipelineRasterizationStateCreateInfo &clazzInfo);

    VkBool32 getrasterizerDiscardEnable();
    uint32_t getcullMode();
    ~VkPipelineRasterizationStateCreateInfoAccessor();
};
#endif // VKPIPELINERASTERIZATIONSTATECREATEINFOACCESSOR_H