/*
 *  VkExtensionPropertiesMutator.cpp
 *  Vulkan mutator for VkExtensionProperties
 *  Created by Ron June Valdoz */

#include <includes/VkExtensionPropertiesMutator.h>

VkExtensionPropertiesMutator::VkExtensionPropertiesMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkExtensionProperties");
    extensionNameField = env->GetFieldID(clazz, "extensionName", "Ljava/lang/String;");
    specVersionField = env->GetFieldID(clazz, "specVersion", "I");
}

jobject
VkExtensionPropertiesMutator::toObject(VkExtensionProperties source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    // process string
    auto extensionName = env->NewStringUTF(source.extensionName);
    env->SetObjectField(newObj, extensionNameField, extensionName);
    env->DeleteLocalRef(extensionName);
    env->SetIntField(newObj, specVersionField, static_cast<jint>(source.specVersion));
    return newObj;
}

VkExtensionPropertiesMutator::~VkExtensionPropertiesMutator() {
    // env->DeleteGlobalRef(clazz);
}

