/*
 *  VkDebugUtilsMessengerCreateInfoEXTAccessor.cpp
 *  Vulkan accessor for VkDebugUtilsMessengerCreateInfoEXT
 *  Created by Ron June Valdoz */

#include  <includes/VkDebugUtilsMessengerCreateInfoEXTAccessor.h>
// android/log.h isn't actually used in this file (no __android_log_* calls) --
// guarded rather than removed outright since it's generated/vendored code, and
// android/log.h doesn't exist on non-Android hosts.
#ifdef __ANDROID__
#include <android/log.h>
#endif // __ANDROID__
#include "VkDebugUtilsMessengerCallbackDataEXTAccessor.h"
#include "VkDebugUtilsMessengerCallbackDataEXTMutator.h"

VkDebugUtilsMessengerCreateInfoEXTAccessor *VkDebugUtilsMessengerCreateInfoEXTAccessor::sInstance = nullptr;


VkStructureType
VkDebugUtilsMessengerCreateInfoEXTAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkDebugUtilsMessengerCreateInfoEXTAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

uint32_t
VkDebugUtilsMessengerCreateInfoEXTAccessor::getmessageType() {
    return (uint32_t) (jint) env->GetIntField(obj, messageTypeField); // primitive
}

VkBool32 VkDebugUtilsMessengerCreateInfoEXTAccessor::onDebugUtilsMessage(
        VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
        VkDebugUtilsMessageTypeFlagsEXT messageTypes,
        const VkDebugUtilsMessengerCallbackDataEXT *callbackData,
        void *userData) {


    jclass callbackInterface = env->GetObjectClass(callbackObj);
//
//    // Get the method ID of the onDebugUtilsMessage method
// Assuming you have a reference to the callback interface class and the method name is "messageSeverity"
    jmethodID invokeMethod = env->GetMethodID(callbackInterface, "invoke",
                                              "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jint jSeverity = static_cast<jint>(messageSeverity);
    jint jTypes = static_cast<jint>(messageTypes);

    auto jData = VkDebugUtilsMessengerCallbackDataEXTMutator(env).toObject(*callbackData);


    // Convert the void* to jlong
    auto jUserData = reinterpret_cast<jlong>(userData);

    jstring jStringValue = env->NewStringUTF("NOT YET IMPLEMENTED");

    jclass integerClass = env->FindClass("java/lang/Integer");
    jmethodID integerConstructor = env->GetMethodID(integerClass, "<init>", "(I)V");

    jclass stringClass = env->FindClass("java/lang/String");
    jmethodID stringConstructor = env->GetMethodID(stringClass, "<init>", "(Ljava/lang/String;)V");


    jobject arg1 = enum_utils::setEnumFromVulkan(env, jSeverity,
                                                 "io/github/ronjunevaldoz/awake/vulkan/enums/flags/VkDebugUtilsMessageSeverityFlagBitsEXT");
    jobject arg2 = env->NewObject(integerClass, integerConstructor,
                                  static_cast<jint>(messageTypes));
    jobject arg4 = env->NewObject(stringClass, stringConstructor, jStringValue);

    env->DeleteLocalRef(jStringValue);

    jobject callbackResult = env->CallObjectMethod(callbackObj, invokeMethod, arg1, arg2, jData,
                                                   arg4);
    VkBool32 eventFlag = VK_FALSE;
    if (callbackResult != nullptr) {
        jclass boolClass = env->FindClass("java/lang/Boolean");
        jmethodID booleanValueMethod = env->GetMethodID(boolClass, "booleanValue", "()Z");
        jboolean returnResult = env->CallBooleanMethod(callbackResult, booleanValueMethod);

        eventFlag = (returnResult == JNI_TRUE) ? VK_TRUE : VK_FALSE;
        env->DeleteLocalRef(boolClass);
        env->DeleteLocalRef(callbackResult);
    }
    env->DeleteLocalRef(arg1);
    env->DeleteLocalRef(arg2);
    env->DeleteLocalRef(integerClass);
    env->DeleteLocalRef(callbackInterface);
    // Returning false tells the layer not to stop when the event occurs, so
    // they see the same behavior with and without validation layers enabled.
    return eventFlag;
}


void VkDebugUtilsMessengerCreateInfoEXTAccessor::getpfnUserCallback(
        VkDebugUtilsMessengerCreateInfoEXT &clazzInfo) {
//    auto callbackObj = (jobject) env->GetObjectField(obj, pfnUserCallbackField); // Any Object
    clazzInfo.pfnUserCallback = &StaticOnDebugUtilsMessage;
}

void
VkDebugUtilsMessengerCreateInfoEXTAccessor::getpUserData(
        VkDebugUtilsMessengerCreateInfoEXT &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pUserDataField); // Any Object
    clazzInfo.pUserData = ref;
}

void
VkDebugUtilsMessengerCreateInfoEXTAccessor::fromObject(
        VkDebugUtilsMessengerCreateInfoEXT &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.messageSeverity = getmessageSeverity(); // Primitive uint32_t
    clazzInfo.messageType = getmessageType(); // Primitive uint32_t
    getpfnUserCallback(clazzInfo); // Other Function4
    getpUserData(clazzInfo); // Other void*
}

void
VkDebugUtilsMessengerCreateInfoEXTAccessor::getpNext(
        VkDebugUtilsMessengerCreateInfoEXT &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

uint32_t
VkDebugUtilsMessengerCreateInfoEXTAccessor::getmessageSeverity() {
    return (uint32_t) (jint) env->GetIntField(obj, messageSeverityField); // primitive
}

VkDebugUtilsMessengerCreateInfoEXTAccessor::~VkDebugUtilsMessengerCreateInfoEXTAccessor() {
    env->DeleteGlobalRef(obj);
    env->DeleteGlobalRef(clazz);
//    env->DeleteGlobalRef(callbackObj);
}