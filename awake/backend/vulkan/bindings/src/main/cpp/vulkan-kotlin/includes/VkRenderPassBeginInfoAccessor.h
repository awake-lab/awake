/*
 *  VkRenderPassBeginInfoAccessor.h
 *  Vulkan accessor for VkRenderPassBeginInfo
 *  Created by Ron June Valdoz */

#ifndef VKRENDERPASSBEGININFOACCESSOR_H
#define VKRENDERPASSBEGININFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkRect2DAccessor.h>
#include <includes/VkClearColorValueAccessor.h>
#include <includes/VkClearDepthStencilValueAccessor.h>

class VkRenderPassBeginInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID renderPassField;
    jfieldID framebufferField;
    jfieldID renderAreaField;
    jfieldID pClearValuesField;
public:
    VkRenderPassBeginInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    VkFramebuffer getframebuffer();

    void fromObject(VkRenderPassBeginInfo &clazzInfo);

    void getpClearValues(VkRenderPassBeginInfo &clazzInfo);

    VkRenderPass getrenderPass();

    void getpNext(VkRenderPassBeginInfo &clazzInfo);

    void getrenderArea(VkRenderPassBeginInfo &clazzInfo);

    ~VkRenderPassBeginInfoAccessor();
};
#endif // VKRENDERPASSBEGININFOACCESSOR_H