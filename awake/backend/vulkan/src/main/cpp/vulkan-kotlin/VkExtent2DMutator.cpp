/*
 *  VkExtent2DMutator.cpp
 *  Vulkan mutator for VkExtent2D
 *  Created by Ron June Valdoz */

#include <includes/VkExtent2DMutator.h>

VkExtent2DMutator::VkExtent2DMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D");
    widthField = env->GetFieldID(clazz, "width", "I");
    heightField = env->GetFieldID(clazz, "height", "I");
}

jobject
VkExtent2DMutator::toObject(VkExtent2D source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetIntField(newObj, widthField, static_cast<jint>(source.width));
    env->SetIntField(newObj, heightField, static_cast<jint>(source.height));
    return newObj;
}

VkExtent2DMutator::~VkExtent2DMutator() {
    // env->DeleteGlobalRef(clazz);
}

