/*
 *  Float32Accessor.cpp
 *  Vulkan accessor for Float32
 *  Created by Ron June Valdoz */

#include <includes/Float32Accessor.h>

Float32Accessor::Float32Accessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    valuesField = env->GetFieldID(clazz, "values", "[F");
}

void
Float32Accessor::fromObject(VkClearColorValue &clazzInfo) {
    getvalues(clazzInfo);  // float Object Array
}

void
Float32Accessor::getvalues(VkClearColorValue &clazzInfo) {
    auto valuesArray = (jfloatArray) env->GetObjectField(obj, valuesField);
    if (valuesArray == nullptr) {
        // float32 no need to release;
        env->DeleteLocalRef(valuesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(valuesArray);
    // primitive array?
    std::vector<float> values(size);
    env->GetFloatArrayRegion(valuesArray, 0, size, reinterpret_cast<jfloat *>(values.data()));
    // processing array data
    std::copy(values.begin(), values.end(), clazzInfo.float32); // union member
    env->DeleteLocalRef(valuesArray); // release reference
}

Float32Accessor::~Float32Accessor() {
    env->DeleteLocalRef(clazz);
}

