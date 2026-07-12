/*
 *  VkExtent3DMutator.cpp
 *  Vulkan mutator for VkExtent3D
 *  Created by Ron June Valdoz */

#include <includes/VkExtent3DMutator.h>

VkExtent3DMutator::VkExtent3DMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkExtent3D");
    widthField = env->GetFieldID(clazz, "width", "I");
    heightField = env->GetFieldID(clazz, "height", "I");
    depthField = env->GetFieldID(clazz, "depth", "I");
}

jobject
VkExtent3DMutator::toObject(VkExtent3D source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetIntField(newObj, widthField, static_cast<jint>(source.width));
    env->SetIntField(newObj, heightField, static_cast<jint>(source.height));
    env->SetIntField(newObj, depthField, static_cast<jint>(source.depth));
    return newObj;
}

VkExtent3DMutator::~VkExtent3DMutator() {
    // env->DeleteGlobalRef(clazz);
}

