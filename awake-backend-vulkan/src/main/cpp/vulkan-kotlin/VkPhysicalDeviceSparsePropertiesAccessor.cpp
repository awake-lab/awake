/*
 *  VkPhysicalDeviceSparsePropertiesAccessor.cpp
 *  Vulkan accessor for VkPhysicalDeviceSparseProperties
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceSparsePropertiesAccessor.h>

VkPhysicalDeviceSparsePropertiesAccessor::VkPhysicalDeviceSparsePropertiesAccessor(JNIEnv *env,
                                                                                   jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
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

VkBool32
VkPhysicalDeviceSparsePropertiesAccessor::getresidencyStandard2DBlockShape() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      residencyStandard2DBlockShapeField); // primitive
}

VkBool32
VkPhysicalDeviceSparsePropertiesAccessor::getresidencyNonResidentStrict() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      residencyNonResidentStrictField); // primitive
}

VkBool32
VkPhysicalDeviceSparsePropertiesAccessor::getresidencyStandard3DBlockShape() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      residencyStandard3DBlockShapeField); // primitive
}

VkBool32
VkPhysicalDeviceSparsePropertiesAccessor::getresidencyStandard2DMultisampleBlockShape() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      residencyStandard2DMultisampleBlockShapeField); // primitive
}

VkBool32
VkPhysicalDeviceSparsePropertiesAccessor::getresidencyAlignedMipSize() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      residencyAlignedMipSizeField); // primitive
}

void
VkPhysicalDeviceSparsePropertiesAccessor::fromObject(VkPhysicalDeviceSparseProperties &clazzInfo) {
    clazzInfo.residencyStandard2DBlockShape = getresidencyStandard2DBlockShape(); // Primitive VkBool32
    clazzInfo.residencyStandard2DMultisampleBlockShape = getresidencyStandard2DMultisampleBlockShape(); // Primitive VkBool32
    clazzInfo.residencyStandard3DBlockShape = getresidencyStandard3DBlockShape(); // Primitive VkBool32
    clazzInfo.residencyAlignedMipSize = getresidencyAlignedMipSize(); // Primitive VkBool32
    clazzInfo.residencyNonResidentStrict = getresidencyNonResidentStrict(); // Primitive VkBool32
}

VkPhysicalDeviceSparsePropertiesAccessor::~VkPhysicalDeviceSparsePropertiesAccessor() {
    env->DeleteLocalRef(clazz);
}

