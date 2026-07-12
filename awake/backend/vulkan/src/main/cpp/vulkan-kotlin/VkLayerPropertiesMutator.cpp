/*
 *  VkLayerPropertiesMutator.cpp
 *  Vulkan mutator for VkLayerProperties
 *  Created by Ron June Valdoz */

#include <includes/VkLayerPropertiesMutator.h>

VkLayerPropertiesMutator::VkLayerPropertiesMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass("io/github/ronjunevaldoz/awake/vulkan/models/VkLayerProperties");
    layerNameField = env->GetFieldID(clazz, "layerName", "Ljava/lang/String;");
    specVersionField = env->GetFieldID(clazz, "specVersion", "I");
    implementationVersionField = env->GetFieldID(clazz, "implementationVersion", "I");
    descriptionField = env->GetFieldID(clazz, "description", "Ljava/lang/String;");
}

jobject
VkLayerPropertiesMutator::toObject(VkLayerProperties source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    // process string
    auto layerName = env->NewStringUTF(source.layerName);
    env->SetObjectField(newObj, layerNameField, layerName);
    env->DeleteLocalRef(layerName);
    env->SetIntField(newObj, specVersionField, static_cast<jint>(source.specVersion));
    env->SetIntField(newObj, implementationVersionField,
                     static_cast<jint>(source.implementationVersion));
    // process string
    auto description = env->NewStringUTF(source.description);
    env->SetObjectField(newObj, descriptionField, description);
    env->DeleteLocalRef(description);
    return newObj;
}

VkLayerPropertiesMutator::~VkLayerPropertiesMutator() {
    // env->DeleteGlobalRef(clazz);
}

