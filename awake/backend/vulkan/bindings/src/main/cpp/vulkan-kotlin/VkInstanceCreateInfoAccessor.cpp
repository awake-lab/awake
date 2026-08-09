/*
 *  VkInstanceCreateInfoAccessor.cpp
 *  Vulkan accessor for VkInstanceCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkInstanceCreateInfoAccessor.h>

VkInstanceCreateInfoAccessor::VkInstanceCreateInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                       obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pApplicationInfoField = env->GetFieldID(clazz, "pApplicationInfo",
                                            "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkApplicationInfo;");
    ppEnabledLayerNamesField = env->GetFieldID(clazz, "ppEnabledLayerNames", "[Ljava/lang/String;");
    ppEnabledExtensionNamesField = env->GetFieldID(clazz, "ppEnabledExtensionNames",
                                                   "[Ljava/lang/String;");
}

VkStructureType
VkInstanceCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkInstanceCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkInstanceCreateInfoAccessor::getppEnabledExtensionNames(VkInstanceCreateInfo &clazzInfo) {
    auto ppEnabledExtensionNamesArray = (jobjectArray) env->GetObjectField(obj,
                                                                           ppEnabledExtensionNamesField);
    if (ppEnabledExtensionNamesArray == nullptr) {
        clazzInfo.enabledExtensionCount = 0;
        clazzInfo.ppEnabledExtensionNames = nullptr;
        env->DeleteLocalRef(ppEnabledExtensionNamesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(ppEnabledExtensionNamesArray);
    std::vector<const char *> ppEnabledExtensionNames;
    for (int i = 0; i < size; ++i) {
        auto element = (jstring) env->GetObjectArrayElement(ppEnabledExtensionNamesArray,
                                                            i); // actual type is String[];
        auto str = env->GetStringUTFChars(element, nullptr);
        auto result = strdup(str); // Allocate memory and copy the string
        env->ReleaseStringUTFChars(element, str); // Release the local string reference
        ppEnabledExtensionNames.push_back(result);
    }
    // processing array data
    auto enabledExtensionCount = static_cast<uint32_t>(ppEnabledExtensionNames.size());
    clazzInfo.enabledExtensionCount = enabledExtensionCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new const char *[size];
    std::copy(ppEnabledExtensionNames.begin(), ppEnabledExtensionNames.end(), copy);
    clazzInfo.ppEnabledExtensionNames = copy;
    env->DeleteLocalRef(ppEnabledExtensionNamesArray); // release reference
}

void
VkInstanceCreateInfoAccessor::getpNext(VkInstanceCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkInstanceCreateInfoAccessor::fromObject(VkInstanceCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpApplicationInfo(clazzInfo);  // VkApplicationInfo Object Array
    getppEnabledLayerNames(clazzInfo);  // String Object Array
    getppEnabledExtensionNames(clazzInfo);  // String Object Array
}

void
VkInstanceCreateInfoAccessor::getpApplicationInfo(VkInstanceCreateInfo &clazzInfo) {
    auto pApplicationInfoArray = (jobjectArray) env->GetObjectField(obj, pApplicationInfoField);
    if (pApplicationInfoArray == nullptr) {
        clazzInfo.pApplicationInfo = nullptr;
        env->DeleteLocalRef(pApplicationInfoArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pApplicationInfoArray);
    std::vector<VkApplicationInfo> pApplicationInfo;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pApplicationInfoArray,
                                                            i); // actual type is VkApplicationInfo[];
        // experimental optimize accessor
        VkApplicationInfoAccessor accessor(env, element);
        VkApplicationInfo ref{};
        accessor.fromObject(ref);
        pApplicationInfo.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkApplicationInfo[size];
    std::copy(pApplicationInfo.begin(), pApplicationInfo.end(), copy);
    clazzInfo.pApplicationInfo = copy;
    env->DeleteLocalRef(pApplicationInfoArray); // release reference
}

void
VkInstanceCreateInfoAccessor::getppEnabledLayerNames(VkInstanceCreateInfo &clazzInfo) {
    auto ppEnabledLayerNamesArray = (jobjectArray) env->GetObjectField(obj,
                                                                       ppEnabledLayerNamesField);
    if (ppEnabledLayerNamesArray == nullptr) {
        clazzInfo.enabledLayerCount = 0;
        clazzInfo.ppEnabledLayerNames = nullptr;
        env->DeleteLocalRef(ppEnabledLayerNamesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(ppEnabledLayerNamesArray);
    std::vector<const char *> ppEnabledLayerNames;
    for (int i = 0; i < size; ++i) {
        auto element = (jstring) env->GetObjectArrayElement(ppEnabledLayerNamesArray,
                                                            i); // actual type is String[];
        auto str = env->GetStringUTFChars(element, nullptr);
        auto result = strdup(str); // Allocate memory and copy the string
        env->ReleaseStringUTFChars(element, str); // Release the local string reference
        ppEnabledLayerNames.push_back(result);
    }
    // processing array data
    auto enabledLayerCount = static_cast<uint32_t>(ppEnabledLayerNames.size());
    clazzInfo.enabledLayerCount = enabledLayerCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new const char *[size];
    std::copy(ppEnabledLayerNames.begin(), ppEnabledLayerNames.end(), copy);
    clazzInfo.ppEnabledLayerNames = copy;
    env->DeleteLocalRef(ppEnabledLayerNamesArray); // release reference
}

VkInstanceCreateInfoAccessor::~VkInstanceCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

