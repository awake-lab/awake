/*
 *  VkPhysicalDeviceSparsePropertiesMutator.cpp
 *  Vulkan mutator for VkPhysicalDeviceSparseProperties
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceSparsePropertiesMutator.h>

VkPhysicalDeviceSparsePropertiesMutator::VkPhysicalDeviceSparsePropertiesMutator(JNIEnv *env) : env(
        env) {
    this->env = env;
    clazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/physicaldevice/VkPhysicalDeviceSparseProperties");
    residencyStandard2DBlockShapeField = env->GetFieldID(clazz, "residencyStandard2DBlockShape",
                                                         "Z");
    residencyStandard2DMultisampleBlockShapeField = env->GetFieldID(clazz,
                                                                    "residencyStandard2DMultisampleBlockShape",
                                                                    "Z");
    residencyStandard3DBlockShapeField = env->GetFieldID(clazz, "residencyStandard3DBlockShape",
                                                         "Z");
    residencyAlignedMipSizeField = env->GetFieldID(clazz, "residencyAlignedMipSize", "Z");
    residencyNonResidentStrictField = env->GetFieldID(clazz, "residencyNonResidentStrict", "Z");
}

jobject
VkPhysicalDeviceSparsePropertiesMutator::toObject(VkPhysicalDeviceSparseProperties source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetBooleanField(newObj, residencyStandard2DBlockShapeField,
                         static_cast<jboolean>(source.residencyStandard2DBlockShape));
    env->SetBooleanField(newObj, residencyStandard2DMultisampleBlockShapeField,
                         static_cast<jboolean>(source.residencyStandard2DMultisampleBlockShape));
    env->SetBooleanField(newObj, residencyStandard3DBlockShapeField,
                         static_cast<jboolean>(source.residencyStandard3DBlockShape));
    env->SetBooleanField(newObj, residencyAlignedMipSizeField,
                         static_cast<jboolean>(source.residencyAlignedMipSize));
    env->SetBooleanField(newObj, residencyNonResidentStrictField,
                         static_cast<jboolean>(source.residencyNonResidentStrict));
    return newObj;
}

VkPhysicalDeviceSparsePropertiesMutator::~VkPhysicalDeviceSparsePropertiesMutator() {
    // env->DeleteGlobalRef(clazz);
}

