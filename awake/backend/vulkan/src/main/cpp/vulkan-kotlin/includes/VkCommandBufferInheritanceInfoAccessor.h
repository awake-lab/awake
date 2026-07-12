/*
 *  VkCommandBufferInheritanceInfoAccessor.h
 *  Vulkan accessor for VkCommandBufferInheritanceInfo
 *  Created by Ron June Valdoz */

#ifndef VKCOMMANDBUFFERINHERITANCEINFOACCESSOR_H
#define VKCOMMANDBUFFERINHERITANCEINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkCommandBufferInheritanceInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID renderPassField;
    jfieldID subpassField;
    jfieldID framebufferField;
    jfieldID occlusionQueryEnableField;
    jfieldID queryFlagsField;
    jfieldID pipelineStatisticsField;
public:
    VkCommandBufferInheritanceInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void getpNext(VkCommandBufferInheritanceInfo &clazzInfo);

    uint32_t getqueryFlags();
    VkFramebuffer getframebuffer();

    VkBool32 getocclusionQueryEnable();

    void fromObject(VkCommandBufferInheritanceInfo &clazzInfo);

    uint32_t getsubpass();
    VkRenderPass getrenderPass();
    uint32_t getpipelineStatistics();
    ~VkCommandBufferInheritanceInfoAccessor();
};
#endif // VKCOMMANDBUFFERINHERITANCEINFOACCESSOR_H