/*
 *  Float32Accessor.h
 *  Vulkan accessor for Float32
 *  Created by Ron June Valdoz */

#ifndef FLOAT32ACCESSOR_H
#define FLOAT32ACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class Float32Accessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID valuesField;
public:
    Float32Accessor(JNIEnv *env, jobject obj);

    void fromObject(VkClearColorValue &clazzInfo);

    void getvalues(VkClearColorValue &clazzInfo);

    ~Float32Accessor();
};
#endif // FLOAT32ACCESSOR_H