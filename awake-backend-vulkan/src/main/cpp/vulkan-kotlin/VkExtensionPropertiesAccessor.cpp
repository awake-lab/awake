/*
 *  VkExtensionPropertiesAccessor.cpp
 *  Vulkan accessor for VkExtensionProperties
 *  Created by Ron June Valdoz */

#include <includes/VkExtensionPropertiesAccessor.h>

VkExtensionPropertiesAccessor::VkExtensionPropertiesAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                         obj(obj) {
    clazz = env->GetObjectClass(obj);
    extensionNameField = env->GetFieldID(clazz, "extensionName", "Ljava/lang/String;");
    specVersionField = env->GetFieldID(clazz, "specVersion", "I");
}

void
VkExtensionPropertiesAccessor::fromObject(VkExtensionProperties &clazzInfo) {
    getextensionName(clazzInfo); // Other const char*
    clazzInfo.specVersion = getspecVersion(); // Primitive uint32_t
}

void
VkExtensionPropertiesAccessor::getextensionName(VkExtensionProperties &clazzInfo) {
    auto extensionNameObj = (jstring) env->GetObjectField(obj, extensionNameField);
    auto str = (const char *) env->GetStringUTFChars(extensionNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(extensionNameObj, str); // Release the local string reference
    strncpy(clazzInfo.extensionName, result, VK_MAX_EXTENSION_NAME_SIZE);
}

uint32_t
VkExtensionPropertiesAccessor::getspecVersion() {
    return (uint32_t) (jint) env->GetIntField(obj, specVersionField); // primitive
}

VkExtensionPropertiesAccessor::~VkExtensionPropertiesAccessor() {
    env->DeleteLocalRef(clazz);
}

