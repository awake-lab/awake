/*
 *  VkDebugUtilsObjectNameInfoEXTAccessor.cpp
 *  Vulkan accessor for VkDebugUtilsObjectNameInfoEXT
 *  Created by Ron June Valdoz */

#include <includes/VkDebugUtilsObjectNameInfoEXTAccessor.h>

VkDebugUtilsObjectNameInfoEXTAccessor::VkDebugUtilsObjectNameInfoEXTAccessor(JNIEnv *env,
                                                                             jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    objectTypeField = env->GetFieldID(clazz, "objectType",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkObjectType;");
    objectHandleField = env->GetFieldID(clazz, "objectHandle", "J");
    pObjectNameField = env->GetFieldID(clazz, "pObjectName", "Ljava/lang/String;");
}

VkStructureType
VkDebugUtilsObjectNameInfoEXTAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkDebugUtilsObjectNameInfoEXTAccessor::fromObject(VkDebugUtilsObjectNameInfoEXT &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.objectType = getobjectType(); // Enum VkObjectType
    clazzInfo.objectHandle = getobjectHandle(); // Primitive uint64_t
    getpObjectName(clazzInfo); // Other const char*
}

uint64_t
VkDebugUtilsObjectNameInfoEXTAccessor::getobjectHandle() {
    return (uint64_t) (jlong) env->GetLongField(obj, objectHandleField); // primitive
}

VkObjectType
VkDebugUtilsObjectNameInfoEXTAccessor::getobjectType() {
    auto objectTypeEnum = (jobject) env->GetObjectField(obj, objectTypeField);
    auto enumValue = (VkObjectType) enum_utils::getEnumFromObject(env, objectTypeEnum);
    env->DeleteLocalRef(objectTypeEnum); // release enum reference
    return enumValue;
}

void
VkDebugUtilsObjectNameInfoEXTAccessor::getpNext(VkDebugUtilsObjectNameInfoEXT &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkDebugUtilsObjectNameInfoEXTAccessor::getpObjectName(VkDebugUtilsObjectNameInfoEXT &clazzInfo) {
    auto pObjectNameObj = (jstring) env->GetObjectField(obj, pObjectNameField);
    auto str = (const char *) env->GetStringUTFChars(pObjectNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(pObjectNameObj, str); // Release the local string reference
    clazzInfo.pObjectName = result;
}

VkDebugUtilsObjectNameInfoEXTAccessor::~VkDebugUtilsObjectNameInfoEXTAccessor() {
    env->DeleteLocalRef(clazz);
}

