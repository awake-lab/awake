/*
 *  VkDebugUtilsMessengerCreateInfoEXTAccessor.h
 *  Vulkan accessor for VkDebugUtilsMessengerCreateInfoEXT
 *  Created by Ron June Valdoz */

#ifndef VKDEBUGUTILSMESSENGERCREATEINFOEXTACCESSOR_H
#define VKDEBUGUTILSMESSENGERCREATEINFOEXTACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan_core.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkDebugUtilsMessengerCreateInfoEXTAccessor {
private:
    JNIEnv *env;
    jobject callbackObj;
    jobject obj;
    jclass clazz;
    jfieldID sTypeField;
    jfieldID pNextField;
    jfieldID flagsField;
    jfieldID messageSeverityField;
    jfieldID messageTypeField;
    jfieldID pfnUserCallbackField;
    jfieldID pUserDataField;
    static VkDebugUtilsMessengerCreateInfoEXTAccessor *sInstance;

    // Private constructor
    VkDebugUtilsMessengerCreateInfoEXTAccessor(JNIEnv *env, jobject obj) {
        this->env = env;
        this->obj = env->NewGlobalRef(obj);
        clazz = (jclass) env->NewGlobalRef(env->GetObjectClass(obj));
        sTypeField = env->GetFieldID(clazz, "sType",
                                     "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
        pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
        flagsField = env->GetFieldID(clazz, "flags", "I");
        messageSeverityField = env->GetFieldID(clazz, "messageSeverity", "I");
        messageTypeField = env->GetFieldID(clazz, "messageType", "I");
        pfnUserCallbackField = env->GetFieldID(clazz, "pfnUserCallback",
                                               "Lkotlin/jvm/functions/Function4;");
        pUserDataField = env->GetFieldID(clazz, "pUserData", "Ljava/lang/Object;");
        callbackObj = env->NewGlobalRef(env->GetObjectField(obj, pfnUserCallbackField));
    }

    // Static wrapper function
    static VKAPI_ATTR VkBool32 VKAPI_CALL StaticOnDebugUtilsMessage(
            VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
            VkDebugUtilsMessageTypeFlagsEXT messageTypes,
            const VkDebugUtilsMessengerCallbackDataEXT *callbackData,
            void *userData) {
        return sInstance->onDebugUtilsMessage(messageSeverity, messageTypes, callbackData,
                                              userData);
    }

public:

    static void init(JNIEnv *jniEnv, jobject obj) {
        if (sInstance) {
            delete sInstance;
            sInstance = nullptr;
        }
        sInstance = new VkDebugUtilsMessengerCreateInfoEXTAccessor(jniEnv, obj);
    }

    static VkDebugUtilsMessengerCreateInfoEXTAccessor getInstance() {
        if (sInstance) {
            return *sInstance;
        }

        throw std::runtime_error("Instance null");
    }

    VkStructureType getsType();

    uint32_t getflags();

    uint32_t getmessageType();

    VKAPI_ATTR VkBool32
    VKAPI_CALL onDebugUtilsMessage(
            VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
            VkDebugUtilsMessageTypeFlagsEXT messageTypes,
            const VkDebugUtilsMessengerCallbackDataEXT *callbackData,
            void *userData);

    void getpfnUserCallback(VkDebugUtilsMessengerCreateInfoEXT &clazzInfo);

    void getpUserData(VkDebugUtilsMessengerCreateInfoEXT &clazzInfo);

    void fromObject(VkDebugUtilsMessengerCreateInfoEXT &clazzInfo);

    void getpNext(VkDebugUtilsMessengerCreateInfoEXT &clazzInfo);

    uint32_t getmessageSeverity();

    ~VkDebugUtilsMessengerCreateInfoEXTAccessor();
};

#endif // VKDEBUGUTILSMESSENGERCREATEINFOEXTACCESSOR_H