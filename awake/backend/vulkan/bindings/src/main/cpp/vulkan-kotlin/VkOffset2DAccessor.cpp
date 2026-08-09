/*
 *  VkOffset2DAccessor.cpp
 *  Vulkan accessor for VkOffset2D
 *  Created by Ron June Valdoz */

#include <includes/VkOffset2DAccessor.h>

VkOffset2DAccessor::VkOffset2DAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    xField = env->GetFieldID(clazz, "x", "I");
    yField = env->GetFieldID(clazz, "y", "I");
}

uint32_t
VkOffset2DAccessor::gety() {
    return (uint32_t) (jint) env->GetIntField(obj, yField); // primitive
}

uint32_t
VkOffset2DAccessor::getx() {
    return (uint32_t) (jint) env->GetIntField(obj, xField); // primitive
}

void
VkOffset2DAccessor::fromObject(VkOffset2D &clazzInfo) {
    clazzInfo.x = getx(); // Primitive uint32_t
    clazzInfo.y = gety(); // Primitive uint32_t
}

VkOffset2DAccessor::~VkOffset2DAccessor() {
    env->DeleteLocalRef(clazz);
}

