/*
 *  VkExtent2DAccessor.cpp
 *  Vulkan accessor for VkExtent2D
 *  Created by Ron June Valdoz */

#include <includes/VkExtent2DAccessor.h>

VkExtent2DAccessor::VkExtent2DAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    widthField = env->GetFieldID(clazz, "width", "I");
    heightField = env->GetFieldID(clazz, "height", "I");
}

uint32_t
VkExtent2DAccessor::getwidth() {
    return (uint32_t) (jint) env->GetIntField(obj, widthField); // primitive
}

void
VkExtent2DAccessor::fromObject(VkExtent2D &clazzInfo) {
    clazzInfo.width = getwidth(); // Primitive uint32_t
    clazzInfo.height = getheight(); // Primitive uint32_t
}

uint32_t
VkExtent2DAccessor::getheight() {
    return (uint32_t) (jint) env->GetIntField(obj, heightField); // primitive
}

VkExtent2DAccessor::~VkExtent2DAccessor() {
    env->DeleteLocalRef(clazz);
}

