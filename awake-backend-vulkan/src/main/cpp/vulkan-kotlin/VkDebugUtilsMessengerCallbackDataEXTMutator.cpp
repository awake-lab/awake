/*
 *  VkDebugUtilsMessengerCallbackDataEXTMutator.cpp
 *  Vulkan mutator for VkDebugUtilsMessengerCallbackDataEXT
 *  Created by Ron June Valdoz */

#include <includes/VkDebugUtilsMessengerCallbackDataEXTMutator.h>

VkDebugUtilsMessengerCallbackDataEXTMutator::VkDebugUtilsMessengerCallbackDataEXTMutator(
        JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsMessengerCallbackDataEXT");
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

jobject
VkDebugUtilsMessengerCallbackDataEXTMutator::toObject(VkDebugUtilsMessengerCallbackDataEXT source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    auto sType = enum_utils::setEnumFromVulkan(env, static_cast<jint>(source.sType),
                                               "io/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType");
    env->SetObjectField(newObj, sTypeField, sType);
    env->DeleteLocalRef(sType);
    // processing Any, Void, Null, Object
    auto pNext = (jobject) source.pNext;
    env->SetObjectField(newObj, pNextField, pNext);
    env->DeleteLocalRef(pNext);
    env->SetIntField(newObj, flagsField, static_cast<jint>(source.flags));
    // process string
    auto pMessageIdName = env->NewStringUTF(source.pMessageIdName);
    env->SetObjectField(newObj, pMessageIdNameField, pMessageIdName);
    env->DeleteLocalRef(pMessageIdName);
    env->SetIntField(newObj, messageIdNumberField, static_cast<jint>(source.messageIdNumber));
    // process string
    auto pMessage = env->NewStringUTF(source.pMessage);
    env->SetObjectField(newObj, pMessageField, pMessage);
    env->DeleteLocalRef(pMessage);
    // processing non-primitive array
    jclass pQueueLabelsClazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsLabelEXT");
    jobjectArray pQueueLabelsArray = env->NewObjectArray(source.queueLabelCount, pQueueLabelsClazz,
                                                         nullptr);
    for (int i = 0; i < source.queueLabelCount; ++i) {
        auto element = source.pQueueLabels[i];
        auto obj = VkDebugUtilsLabelEXTMutator(env).toObject(element);
        env->SetObjectArrayElement(pQueueLabelsArray, i, obj);
        env->DeleteLocalRef(obj);
    }
    env->SetObjectField(newObj, pQueueLabelsField, pQueueLabelsArray);
    env->DeleteLocalRef(pQueueLabelsArray);
    env->DeleteLocalRef(pQueueLabelsClazz);
    // processing non-primitive array
    jclass pCmdBufLabelsClazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsLabelEXT");
    jobjectArray pCmdBufLabelsArray = env->NewObjectArray(source.cmdBufLabelCount,
                                                          pCmdBufLabelsClazz, nullptr);
    for (int i = 0; i < source.cmdBufLabelCount; ++i) {
        auto element = source.pCmdBufLabels[i];
        auto obj = VkDebugUtilsLabelEXTMutator(env).toObject(element);
        env->SetObjectArrayElement(pCmdBufLabelsArray, i, obj);
        env->DeleteLocalRef(obj);
    }
    env->SetObjectField(newObj, pCmdBufLabelsField, pCmdBufLabelsArray);
    env->DeleteLocalRef(pCmdBufLabelsArray);
    env->DeleteLocalRef(pCmdBufLabelsClazz);
    // processing non-primitive array
    jclass pObjectsClazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/info/debug/VkDebugUtilsObjectNameInfoEXT");
    jobjectArray pObjectsArray = env->NewObjectArray(source.objectCount, pObjectsClazz, nullptr);
    for (int i = 0; i < source.objectCount; ++i) {
        auto element = source.pObjects[i];
        auto obj = VkDebugUtilsObjectNameInfoEXTMutator(env).toObject(element);
        env->SetObjectArrayElement(pObjectsArray, i, obj);
        env->DeleteLocalRef(obj);
    }
    env->SetObjectField(newObj, pObjectsField, pObjectsArray);
    env->DeleteLocalRef(pObjectsArray);
    env->DeleteLocalRef(pObjectsClazz);
    return newObj;
}

VkDebugUtilsMessengerCallbackDataEXTMutator::~VkDebugUtilsMessengerCallbackDataEXTMutator() {
    // env->DeleteGlobalRef(clazz);
}

