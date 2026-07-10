/*
 *  Int32Accessor.cpp
 *  Vulkan accessor for Int32
 *  Created by Ron June Valdoz */

#include <includes/Int32Accessor.h>

Int32Accessor::Int32Accessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    valuesField = env->GetFieldID(clazz, "values", "[I");
}

void
Int32Accessor::fromObject(VkClearColorValue &clazzInfo) {
    getvalues(clazzInfo);  // int Object Array
}

void
Int32Accessor::getvalues(VkClearColorValue &clazzInfo) {
    auto valuesArray = (jintArray) env->GetObjectField(obj, valuesField);
    if (valuesArray == nullptr) {
        // int32 no need to release;
        env->DeleteLocalRef(valuesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(valuesArray);
    // primitive array?
    std::vector<uint32_t> values(size);
    env->GetIntArrayRegion(valuesArray, 0, size, reinterpret_cast<jint *>(values.data()));
    // processing array data
    std::copy(values.begin(), values.end(), clazzInfo.int32); // union member
    env->DeleteLocalRef(valuesArray); // release reference
}

Int32Accessor::~Int32Accessor() {
    env->DeleteLocalRef(clazz);
}

