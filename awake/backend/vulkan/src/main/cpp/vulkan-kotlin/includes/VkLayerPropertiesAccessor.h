/*
 *  VkLayerPropertiesAccessor.h
 *  Vulkan accessor for VkLayerProperties
 *  Created by Ron June Valdoz */

#ifndef VKLAYERPROPERTIESACCESSOR_H
#define VKLAYERPROPERTIESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkLayerPropertiesAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID layerNameField;
    jfieldID specVersionField;
    jfieldID implementationVersionField;
    jfieldID descriptionField;
public:
    VkLayerPropertiesAccessor(JNIEnv *env, jobject obj);

    uint32_t getspecVersion();

    void fromObject(VkLayerProperties &clazzInfo);

    void getlayerName(VkLayerProperties &clazzInfo);

    uint32_t getimplementationVersion();

    void getdescription(VkLayerProperties &clazzInfo);

    ~VkLayerPropertiesAccessor();
};
#endif // VKLAYERPROPERTIESACCESSOR_H