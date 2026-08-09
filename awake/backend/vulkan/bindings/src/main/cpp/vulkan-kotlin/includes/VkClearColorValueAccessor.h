/*
 *  VkClearColorValueAccessor.h
 *  
 *  Created by Ron June Valdoz */

#ifndef VKCLEARCOLORVALUEACCESSOR_H
#define VKCLEARCOLORVALUEACCESSOR_H

#include <jni.h>
#include <includes/Float32Accessor.h>
#include <includes/Int32Accessor.h>
#include <includes/UInt32Accessor.h>

class VkClearColorValueAccessor {
private:
    JNIEnv *env;
    jobject obj;
public:
    VkClearColorValueAccessor(JNIEnv *env, jobject obj);

    void fromObject(VkClearColorValue &info);
};
#endif // VKCLEARCOLORVALUEACCESSOR_H