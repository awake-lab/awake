/*
 *  VkRect2DAccessor.cpp
 *  Vulkan accessor for VkRect2D
 *  Created by Ron June Valdoz */

#include <includes/VkRect2DAccessor.h>

VkRect2DAccessor::VkRect2DAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    offsetField = env->GetFieldID(clazz, "offset",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/models/VkOffset2D;");
    extentField = env->GetFieldID(clazz, "extent",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D;");
}

void
VkRect2DAccessor::fromObject(VkRect2D &clazzInfo) {
    getoffset(clazzInfo); // Other VkOffset2D
    getextent(clazzInfo); // Other VkExtent2D
}

void
VkRect2DAccessor::getoffset(VkRect2D &clazzInfo) {
    auto offsetObj = (jobject) env->GetObjectField(obj, offsetField);
    if (offsetObj == nullptr) {
        env->DeleteLocalRef(offsetObj); // Delete null object reference
        return;
    }
    VkOffset2DAccessor accessor(env, offsetObj);
    VkOffset2D ref{};
    accessor.fromObject(ref);
    clazzInfo.offset = ref;
    env->DeleteLocalRef(offsetObj); // Delete object reference
}

void
VkRect2DAccessor::getextent(VkRect2D &clazzInfo) {
    auto extentObj = (jobject) env->GetObjectField(obj, extentField);
    if (extentObj == nullptr) {
        env->DeleteLocalRef(extentObj); // Delete null object reference
        return;
    }
    VkExtent2DAccessor accessor(env, extentObj);
    VkExtent2D ref{};
    accessor.fromObject(ref);
    clazzInfo.extent = ref;
    env->DeleteLocalRef(extentObj); // Delete object reference
}

VkRect2DAccessor::~VkRect2DAccessor() {
    env->DeleteLocalRef(clazz);
}

