/*
 *  Int32Accessor.h
 *  Vulkan accessor for Int32
 *  Created by Ron June Valdoz */

#ifndef INT32ACCESSOR_H
#define INT32ACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class Int32Accessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID valuesField;
public:
    Int32Accessor(JNIEnv *env, jobject obj);

    void fromObject(VkClearColorValue &clazzInfo);

    void getvalues(VkClearColorValue &clazzInfo);

    ~Int32Accessor();
};
#endif // INT32ACCESSOR_H