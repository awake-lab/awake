/*
 *  VkSpecializationMapEntryAccessor.cpp
 *  Vulkan accessor for VkSpecializationMapEntry
 *  Created by Ron June Valdoz */

#include <includes/VkSpecializationMapEntryAccessor.h>

VkSpecializationMapEntryAccessor::VkSpecializationMapEntryAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    constantIDField = env->GetFieldID(clazz, "constantID", "I");
    offsetField = env->GetFieldID(clazz, "offset", "I");
    sizeField = env->GetFieldID(clazz, "size", "J");
}

void
VkSpecializationMapEntryAccessor::fromObject(VkSpecializationMapEntry &clazzInfo) {
    clazzInfo.constantID = getconstantID(); // Primitive uint32_t
    clazzInfo.offset = getoffset(); // Primitive uint32_t
    clazzInfo.size = getsize(); // Primitive uint64_t
}

uint64_t
VkSpecializationMapEntryAccessor::getsize() {
    return (uint64_t) (jlong) env->GetLongField(obj, sizeField); // primitive
}

uint32_t
VkSpecializationMapEntryAccessor::getconstantID() {
    return (uint32_t) (jint) env->GetIntField(obj, constantIDField); // primitive
}

uint32_t
VkSpecializationMapEntryAccessor::getoffset() {
    return (uint32_t) (jint) env->GetIntField(obj, offsetField); // primitive
}

VkSpecializationMapEntryAccessor::~VkSpecializationMapEntryAccessor() {
    env->DeleteLocalRef(clazz);
}

