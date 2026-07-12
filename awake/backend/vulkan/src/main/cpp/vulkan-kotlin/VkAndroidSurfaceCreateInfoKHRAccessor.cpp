/*
 *  VkAndroidSurfaceCreateInfoKHRAccessor.cpp
 *  Vulkan accessor for VkAndroidSurfaceCreateInfoKHR
 *  Created by Ron June Valdoz */

#include <includes/VkAndroidSurfaceCreateInfoKHRAccessor.h>

VkAndroidSurfaceCreateInfoKHRAccessor::VkAndroidSurfaceCreateInfoKHRAccessor(JNIEnv *env,
                                                                             jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    windowField = env->GetFieldID(clazz, "window", "Ljava/lang/Object;");
}

VkStructureType
VkAndroidSurfaceCreateInfoKHRAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkAndroidSurfaceCreateInfoKHRAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkAndroidSurfaceCreateInfoKHRAccessor::fromObject(VkAndroidSurfaceCreateInfoKHR &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getwindow(clazzInfo); // Other void*
}

void
VkAndroidSurfaceCreateInfoKHRAccessor::getwindow(VkAndroidSurfaceCreateInfoKHR &clazzInfo) {
    auto windowObj = env->GetObjectField(obj, windowField); // Native surface window
    clazzInfo.window = ANativeWindow_fromSurface(env, windowObj);
}

void
VkAndroidSurfaceCreateInfoKHRAccessor::getpNext(VkAndroidSurfaceCreateInfoKHR &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkAndroidSurfaceCreateInfoKHRAccessor::~VkAndroidSurfaceCreateInfoKHRAccessor() {
    env->DeleteLocalRef(clazz);
}

