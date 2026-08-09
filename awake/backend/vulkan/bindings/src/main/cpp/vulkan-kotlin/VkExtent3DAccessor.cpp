/*
 *  VkExtent3DAccessor.cpp
 *  Vulkan accessor for VkExtent3D
 *  Created by Ron June Valdoz */

#include <includes/VkExtent3DAccessor.h>

VkExtent3DAccessor::VkExtent3DAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    widthField = env->GetFieldID(clazz, "width", "I");
    heightField = env->GetFieldID(clazz, "height", "I");
    depthField = env->GetFieldID(clazz, "depth", "I");
}

uint32_t
VkExtent3DAccessor::getwidth() {
    return (uint32_t) (jint) env->GetIntField(obj, widthField); // primitive
}

void
VkExtent3DAccessor::fromObject(VkExtent3D &clazzInfo) {
    clazzInfo.width = getwidth(); // Primitive uint32_t
    clazzInfo.height = getheight(); // Primitive uint32_t
    clazzInfo.depth = getdepth(); // Primitive uint32_t
}

uint32_t
VkExtent3DAccessor::getdepth() {
    return (uint32_t) (jint) env->GetIntField(obj, depthField); // primitive
}

uint32_t
VkExtent3DAccessor::getheight() {
    return (uint32_t) (jint) env->GetIntField(obj, heightField); // primitive
}

VkExtent3DAccessor::~VkExtent3DAccessor() {
    env->DeleteLocalRef(clazz);
}

