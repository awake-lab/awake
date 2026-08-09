/*
 *  VkSubpassDependencyAccessor.cpp
 *  Vulkan accessor for VkSubpassDependency
 *  Created by Ron June Valdoz */

#include <includes/VkSubpassDependencyAccessor.h>

VkSubpassDependencyAccessor::VkSubpassDependencyAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                     obj(obj) {
    clazz = env->GetObjectClass(obj);
    srcSubpassField = env->GetFieldID(clazz, "srcSubpass", "I");
    dstSubpassField = env->GetFieldID(clazz, "dstSubpass", "I");
    srcStageMaskField = env->GetFieldID(clazz, "srcStageMask", "I");
    dstStageMaskField = env->GetFieldID(clazz, "dstStageMask", "I");
    srcAccessMaskField = env->GetFieldID(clazz, "srcAccessMask", "I");
    dstAccessMaskField = env->GetFieldID(clazz, "dstAccessMask", "I");
    dependencyFlagsField = env->GetFieldID(clazz, "dependencyFlags", "I");
}

uint32_t
VkSubpassDependencyAccessor::getdstStageMask() {
    return (uint32_t) (jint) env->GetIntField(obj, dstStageMaskField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getdstAccessMask() {
    return (uint32_t) (jint) env->GetIntField(obj, dstAccessMaskField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getsrcSubpass() {
    return (uint32_t) (jint) env->GetIntField(obj, srcSubpassField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getdstSubpass() {
    return (uint32_t) (jint) env->GetIntField(obj, dstSubpassField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getdependencyFlags() {
    return (uint32_t) (jint) env->GetIntField(obj, dependencyFlagsField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getsrcAccessMask() {
    return (uint32_t) (jint) env->GetIntField(obj, srcAccessMaskField); // primitive
}

uint32_t
VkSubpassDependencyAccessor::getsrcStageMask() {
    return (uint32_t) (jint) env->GetIntField(obj, srcStageMaskField); // primitive
}

void
VkSubpassDependencyAccessor::fromObject(VkSubpassDependency &clazzInfo) {
    clazzInfo.srcSubpass = getsrcSubpass(); // Primitive uint32_t
    clazzInfo.dstSubpass = getdstSubpass(); // Primitive uint32_t
    clazzInfo.srcStageMask = getsrcStageMask(); // Primitive uint32_t
    clazzInfo.dstStageMask = getdstStageMask(); // Primitive uint32_t
    clazzInfo.srcAccessMask = getsrcAccessMask(); // Primitive uint32_t
    clazzInfo.dstAccessMask = getdstAccessMask(); // Primitive uint32_t
    clazzInfo.dependencyFlags = getdependencyFlags(); // Primitive uint32_t
}

VkSubpassDependencyAccessor::~VkSubpassDependencyAccessor() {
    env->DeleteLocalRef(clazz);
}

