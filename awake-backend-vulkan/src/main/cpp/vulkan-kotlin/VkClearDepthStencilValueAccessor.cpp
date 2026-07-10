/*
 *  VkClearDepthStencilValueAccessor.cpp
 *  Vulkan accessor for VkClearDepthStencilValue
 *  Created by Ron June Valdoz */

#include <includes/VkClearDepthStencilValueAccessor.h>

VkClearDepthStencilValueAccessor::VkClearDepthStencilValueAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    depthField = env->GetFieldID(clazz, "depth", "F");
    stencilField = env->GetFieldID(clazz, "stencil", "I");
}

uint32_t
VkClearDepthStencilValueAccessor::getstencil() {
    return (uint32_t) (jint) env->GetIntField(obj, stencilField); // primitive
}

void
VkClearDepthStencilValueAccessor::fromObject(VkClearDepthStencilValue &clazzInfo) {
    clazzInfo.depth = getdepth(); // Primitive float
    clazzInfo.stencil = getstencil(); // Primitive uint32_t
}

float
VkClearDepthStencilValueAccessor::getdepth() {
    return (float) (jfloat) env->GetFloatField(obj, depthField); // primitive
}

VkClearDepthStencilValueAccessor::~VkClearDepthStencilValueAccessor() {
    env->DeleteLocalRef(clazz);
}

