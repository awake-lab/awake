/*
 *  VkSubmitInfoAccessor.h
 *  Vulkan accessor for VkSubmitInfo
 *  Created by Ron June Valdoz */

#ifndef VKSUBMITINFOACCESSOR_H
#define VKSUBMITINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkSubmitInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID pWaitSemaphoresField;
    jfieldID pWaitDstStageMaskField;
    jfieldID pCommandBuffersField;
    jfieldID pSignalSemaphoresField;
public:
    VkSubmitInfoAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void getpNext(VkSubmitInfo &clazzInfo);

    void getpSignalSemaphores(VkSubmitInfo &clazzInfo);

    void getpWaitDstStageMask(VkSubmitInfo &clazzInfo);

    void fromObject(VkSubmitInfo &clazzInfo);

    void getpCommandBuffers(VkSubmitInfo &clazzInfo);

    void getpWaitSemaphores(VkSubmitInfo &clazzInfo);

    ~VkSubmitInfoAccessor();
};
#endif // VKSUBMITINFOACCESSOR_H