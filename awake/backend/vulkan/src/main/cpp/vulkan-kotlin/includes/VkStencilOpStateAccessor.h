/*
 *  VkStencilOpStateAccessor.h
 *  Vulkan accessor for VkStencilOpState
 *  Created by Ron June Valdoz */

#ifndef VKSTENCILOPSTATEACCESSOR_H
#define VKSTENCILOPSTATEACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkStencilOpStateAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID failOpField;
    jfieldID passOpField;
    jfieldID depthFailOpField;
    jfieldID compareOpField;
    jfieldID compareMaskField;
    jfieldID writeMaskField;
    jfieldID referenceField;
public:
    VkStencilOpStateAccessor(JNIEnv *env, jobject obj);

    VkStencilOp getfailOp();
    uint32_t getcompareMask();
    uint32_t getreference();
    VkCompareOp getcompareOp();

    uint32_t getwriteMask();

    void fromObject(VkStencilOpState &clazzInfo);

    VkStencilOp getpassOp();
    VkStencilOp getdepthFailOp();
    ~VkStencilOpStateAccessor();
};
#endif // VKSTENCILOPSTATEACCESSOR_H