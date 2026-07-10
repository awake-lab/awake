/*
 *  VkPresentInfoKHRAccessor.h
 *  Vulkan accessor for VkPresentInfoKHR
 *  Created by Ron June Valdoz */

#ifndef VKPRESENTINFOKHRACCESSOR_H
#define VKPRESENTINFOKHRACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPresentInfoKHRAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID pWaitSemaphoresField;
    jfieldID pSwapchainsField;
    jfieldID pImageIndicesField;
    jfieldID pResultsField;
public:
    VkPresentInfoKHRAccessor(JNIEnv *env, jobject obj);

    VkStructureType getsType();

    void getpImageIndices(VkPresentInfoKHR &clazzInfo);

    void getpSwapchains(VkPresentInfoKHR &clazzInfo);

    void getpWaitSemaphores(VkPresentInfoKHR &clazzInfo);

    void getpNext(VkPresentInfoKHR &clazzInfo);

    void fromObject(VkPresentInfoKHR &clazzInfo);

    void getpResults(VkPresentInfoKHR &clazzInfo);

    ~VkPresentInfoKHRAccessor();
};
#endif // VKPRESENTINFOKHRACCESSOR_H