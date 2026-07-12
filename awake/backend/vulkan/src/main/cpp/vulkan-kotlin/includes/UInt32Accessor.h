/*
 *  UInt32Accessor.h
 *  Vulkan accessor for UInt32
 *  Created by Ron June Valdoz */

#ifndef UINT32ACCESSOR_H
#define UINT32ACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class UInt32Accessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID valuesField;
public:
    UInt32Accessor(JNIEnv *env, jobject obj);

    void fromObject(VkClearColorValue &clazzInfo);

    void getvalues(VkClearColorValue &clazzInfo);

    ~UInt32Accessor();
};
#endif // UINT32ACCESSOR_H