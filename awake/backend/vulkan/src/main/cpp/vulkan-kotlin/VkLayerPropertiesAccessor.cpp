/*
 *  VkLayerPropertiesAccessor.cpp
 *  Vulkan accessor for VkLayerProperties
 *  Created by Ron June Valdoz */

#include <includes/VkLayerPropertiesAccessor.h>

VkLayerPropertiesAccessor::VkLayerPropertiesAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                 obj(obj) {
    clazz = env->GetObjectClass(obj);
    layerNameField = env->GetFieldID(clazz, "layerName", "Ljava/lang/String;");
    specVersionField = env->GetFieldID(clazz, "specVersion", "I");
    implementationVersionField = env->GetFieldID(clazz, "implementationVersion", "I");
    descriptionField = env->GetFieldID(clazz, "description", "Ljava/lang/String;");
}

uint32_t
VkLayerPropertiesAccessor::getspecVersion() {
    return (uint32_t) (jint) env->GetIntField(obj, specVersionField); // primitive
}

void
VkLayerPropertiesAccessor::fromObject(VkLayerProperties &clazzInfo) {
    getlayerName(clazzInfo); // Other const char*
    clazzInfo.specVersion = getspecVersion(); // Primitive uint32_t
    clazzInfo.implementationVersion = getimplementationVersion(); // Primitive uint32_t
    getdescription(clazzInfo); // Other const char*
}

void
VkLayerPropertiesAccessor::getlayerName(VkLayerProperties &clazzInfo) {
    auto layerNameObj = (jstring) env->GetObjectField(obj, layerNameField);
    auto str = (const char *) env->GetStringUTFChars(layerNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(layerNameObj, str); // Release the local string reference
    strncpy(clazzInfo.layerName, result, VK_MAX_EXTENSION_NAME_SIZE);
}

uint32_t
VkLayerPropertiesAccessor::getimplementationVersion() {
    return (uint32_t) (jint) env->GetIntField(obj, implementationVersionField); // primitive
}

void
VkLayerPropertiesAccessor::getdescription(VkLayerProperties &clazzInfo) {
    auto descriptionObj = (jstring) env->GetObjectField(obj, descriptionField);
    auto str = (const char *) env->GetStringUTFChars(descriptionObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(descriptionObj, str); // Release the local string reference
    strncpy(clazzInfo.description, result, VK_MAX_DESCRIPTION_SIZE);
}

VkLayerPropertiesAccessor::~VkLayerPropertiesAccessor() {
    env->DeleteLocalRef(clazz);
}

