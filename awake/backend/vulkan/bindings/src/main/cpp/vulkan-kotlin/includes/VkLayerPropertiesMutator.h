/*
 *  VkLayerPropertiesMutator.h
 *  Vulkan mutator for VkLayerProperties
 *  Created by Ron June Valdoz */

#ifndef VKLAYERPROPERTIESMUTATOR_H
#define VKLAYERPROPERTIESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkLayerPropertiesMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID layerNameField;
    jfieldID specVersionField;
    jfieldID implementationVersionField;
    jfieldID descriptionField;
public:
    VkLayerPropertiesMutator(JNIEnv *env);

    jobject toObject(VkLayerProperties source);
    ~VkLayerPropertiesMutator();
};
#endif // VKLAYERPROPERTIESMUTATOR_H