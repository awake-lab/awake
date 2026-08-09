/*
 *  VkDebugUtilsMessengerCallbackDataEXTAccessor.cpp
 *  Vulkan accessor for VkDebugUtilsMessengerCallbackDataEXT
 *  Created by Ron June Valdoz */

#include <includes/VkDebugUtilsMessengerCallbackDataEXTAccessor.h>

VkDebugUtilsMessengerCallbackDataEXTAccessor::VkDebugUtilsMessengerCallbackDataEXTAccessor(
        JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pMessageIdNameField = env->GetFieldID(clazz, "pMessageIdName", "Ljava/lang/String;");
    messageIdNumberField = env->GetFieldID(clazz, "messageIdNumber", "I");
    pMessageField = env->GetFieldID(clazz, "pMessage", "Ljava/lang/String;");
    pQueueLabelsField = env->GetFieldID(clazz, "pQueueLabels",
                                        "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsLabelEXT;");
    pCmdBufLabelsField = env->GetFieldID(clazz, "pCmdBufLabels",
                                         "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsLabelEXT;");
    pObjectsField = env->GetFieldID(clazz, "pObjects",
                                    "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsObjectNameInfoEXT;");
}

VkStructureType
VkDebugUtilsMessengerCallbackDataEXTAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkDebugUtilsMessengerCallbackDataEXTAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpNext(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpMessage(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto pMessageObj = (jstring) env->GetObjectField(obj, pMessageField);
    auto str = (const char *) env->GetStringUTFChars(pMessageObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(pMessageObj, str); // Release the local string reference
    clazzInfo.pMessage = result;
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpQueueLabels(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto pQueueLabelsArray = (jobjectArray) env->GetObjectField(obj, pQueueLabelsField);
    if (pQueueLabelsArray == nullptr) {
        clazzInfo.queueLabelCount = 0;
        clazzInfo.pQueueLabels = nullptr;
        env->DeleteLocalRef(pQueueLabelsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pQueueLabelsArray);
    std::vector<VkDebugUtilsLabelEXT> pQueueLabels;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pQueueLabelsArray,
                                                            i); // actual type is VkDebugUtilsLabelEXT[];
        // experimental optimize accessor
        VkDebugUtilsLabelEXTAccessor accessor(env, element);
        VkDebugUtilsLabelEXT ref{};
        accessor.fromObject(ref);
        pQueueLabels.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto queueLabelCount = static_cast<uint32_t>(pQueueLabels.size());
    clazzInfo.queueLabelCount = queueLabelCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkDebugUtilsLabelEXT[size];
    std::copy(pQueueLabels.begin(), pQueueLabels.end(), copy);
    clazzInfo.pQueueLabels = copy;
    env->DeleteLocalRef(pQueueLabelsArray); // release reference
}

uint32_t
VkDebugUtilsMessengerCallbackDataEXTAccessor::getmessageIdNumber() {
    return (uint32_t) (jint) env->GetIntField(obj, messageIdNumberField); // primitive
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpCmdBufLabels(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto pCmdBufLabelsArray = (jobjectArray) env->GetObjectField(obj, pCmdBufLabelsField);
    if (pCmdBufLabelsArray == nullptr) {
        clazzInfo.cmdBufLabelCount = 0;
        clazzInfo.pCmdBufLabels = nullptr;
        env->DeleteLocalRef(pCmdBufLabelsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pCmdBufLabelsArray);
    std::vector<VkDebugUtilsLabelEXT> pCmdBufLabels;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pCmdBufLabelsArray,
                                                            i); // actual type is VkDebugUtilsLabelEXT[];
        // experimental optimize accessor
        VkDebugUtilsLabelEXTAccessor accessor(env, element);
        VkDebugUtilsLabelEXT ref{};
        accessor.fromObject(ref);
        pCmdBufLabels.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto cmdBufLabelCount = static_cast<uint32_t>(pCmdBufLabels.size());
    clazzInfo.cmdBufLabelCount = cmdBufLabelCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkDebugUtilsLabelEXT[size];
    std::copy(pCmdBufLabels.begin(), pCmdBufLabels.end(), copy);
    clazzInfo.pCmdBufLabels = copy;
    env->DeleteLocalRef(pCmdBufLabelsArray); // release reference
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpMessageIdName(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto pMessageIdNameObj = (jstring) env->GetObjectField(obj, pMessageIdNameField);
    auto str = (const char *) env->GetStringUTFChars(pMessageIdNameObj, nullptr);
    auto result = strdup(str); // Allocate memory and copy the string
    env->ReleaseStringUTFChars(pMessageIdNameObj, str); // Release the local string reference
    clazzInfo.pMessageIdName = result;
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::fromObject(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpMessageIdName(clazzInfo); // Other const char*
    clazzInfo.messageIdNumber = getmessageIdNumber(); // Primitive uint32_t
    getpMessage(clazzInfo); // Other const char*
    getpQueueLabels(clazzInfo);  // VkDebugUtilsLabelEXT Object Array
    getpCmdBufLabels(clazzInfo);  // VkDebugUtilsLabelEXT Object Array
    getpObjects(clazzInfo);  // VkDebugUtilsObjectNameInfoEXT Object Array
}

void
VkDebugUtilsMessengerCallbackDataEXTAccessor::getpObjects(
        VkDebugUtilsMessengerCallbackDataEXT &clazzInfo) {
    auto pObjectsArray = (jobjectArray) env->GetObjectField(obj, pObjectsField);
    if (pObjectsArray == nullptr) {
        clazzInfo.objectCount = 0;
        clazzInfo.pObjects = nullptr;
        env->DeleteLocalRef(pObjectsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pObjectsArray);
    std::vector<VkDebugUtilsObjectNameInfoEXT> pObjects;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pObjectsArray,
                                                            i); // actual type is VkDebugUtilsObjectNameInfoEXT[];
        // experimental optimize accessor
        VkDebugUtilsObjectNameInfoEXTAccessor accessor(env, element);
        VkDebugUtilsObjectNameInfoEXT ref{};
        accessor.fromObject(ref);
        pObjects.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto objectCount = static_cast<uint32_t>(pObjects.size());
    clazzInfo.objectCount = objectCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkDebugUtilsObjectNameInfoEXT[size];
    std::copy(pObjects.begin(), pObjects.end(), copy);
    clazzInfo.pObjects = copy;
    env->DeleteLocalRef(pObjectsArray); // release reference
}

VkDebugUtilsMessengerCallbackDataEXTAccessor::~VkDebugUtilsMessengerCallbackDataEXTAccessor() {
    env->DeleteLocalRef(clazz);
}

