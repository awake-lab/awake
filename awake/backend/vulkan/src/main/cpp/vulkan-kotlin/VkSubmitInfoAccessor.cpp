/*
 *  VkSubmitInfoAccessor.cpp
 *  Vulkan accessor for VkSubmitInfo
 *  Created by Ron June Valdoz */

#include <includes/VkSubmitInfoAccessor.h>

VkSubmitInfoAccessor::VkSubmitInfoAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    pWaitSemaphoresField = env->GetFieldID(clazz, "pWaitSemaphores", "[Ljava/lang/Long;");
    pWaitDstStageMaskField = env->GetFieldID(clazz, "pWaitDstStageMask", "[I");
    pCommandBuffersField = env->GetFieldID(clazz, "pCommandBuffers", "[Ljava/lang/Long;");
    pSignalSemaphoresField = env->GetFieldID(clazz, "pSignalSemaphores", "[Ljava/lang/Long;");
}

VkStructureType
VkSubmitInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkSubmitInfoAccessor::getpNext(VkSubmitInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkSubmitInfoAccessor::getpSignalSemaphores(VkSubmitInfo &clazzInfo) {
    auto pSignalSemaphoresArray = (jobjectArray) env->GetObjectField(obj, pSignalSemaphoresField);
    if (pSignalSemaphoresArray == nullptr) {
        clazzInfo.signalSemaphoreCount = 0;
        clazzInfo.pSignalSemaphores = nullptr;
        env->DeleteLocalRef(pSignalSemaphoresArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pSignalSemaphoresArray);
    // vkhandle array?
    std::vector<VkSemaphore> pSignalSemaphores;
    for (int i = 0; i < size; ++i) {
        auto element = env->GetObjectArrayElement(pSignalSemaphoresArray, i);
        jmethodID getValueMethod = env->GetMethodID(env->GetObjectClass(element), "longValue",
                                                    "()J");
        jlong value = env->CallLongMethod(element, getValueMethod);
        pSignalSemaphores.push_back(reinterpret_cast<VkSemaphore>(value)); //vkhandle
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto signalSemaphoreCount = static_cast<uint32_t>(pSignalSemaphores.size());
    clazzInfo.signalSemaphoreCount = signalSemaphoreCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSemaphore[size];
    std::copy(pSignalSemaphores.begin(), pSignalSemaphores.end(), copy);
    clazzInfo.pSignalSemaphores = copy;
    env->DeleteLocalRef(pSignalSemaphoresArray); // release reference
}

void
VkSubmitInfoAccessor::getpWaitDstStageMask(VkSubmitInfo &clazzInfo) {
    auto pWaitDstStageMaskArray = (jintArray) env->GetObjectField(obj, pWaitDstStageMaskField);
    if (pWaitDstStageMaskArray == nullptr) {
        clazzInfo.pWaitDstStageMask = nullptr;
        env->DeleteLocalRef(pWaitDstStageMaskArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pWaitDstStageMaskArray);
    // primitive array?
    std::vector<uint32_t> pWaitDstStageMask(size);
    env->GetIntArrayRegion(pWaitDstStageMaskArray, 0, size,
                           reinterpret_cast<jint *>(pWaitDstStageMask.data()));
    // processing array data
    // Make a copy of the primitive to ensure proper memory management;
    auto copy = new uint32_t[size];
    std::copy(pWaitDstStageMask.begin(), pWaitDstStageMask.end(), copy);
    clazzInfo.pWaitDstStageMask = copy;
    env->DeleteLocalRef(pWaitDstStageMaskArray); // release reference
}

void
VkSubmitInfoAccessor::fromObject(VkSubmitInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    getpWaitSemaphores(clazzInfo);  // Long Object Array
    getpWaitDstStageMask(clazzInfo);  // int Object Array
    getpCommandBuffers(clazzInfo);  // Long Object Array
    getpSignalSemaphores(clazzInfo);  // Long Object Array
}

void
VkSubmitInfoAccessor::getpCommandBuffers(VkSubmitInfo &clazzInfo) {
    auto pCommandBuffersArray = (jobjectArray) env->GetObjectField(obj, pCommandBuffersField);
    if (pCommandBuffersArray == nullptr) {
        clazzInfo.commandBufferCount = 0;
        clazzInfo.pCommandBuffers = nullptr;
        env->DeleteLocalRef(pCommandBuffersArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pCommandBuffersArray);
    // vkhandle array?
    std::vector<VkCommandBuffer> pCommandBuffers;
    for (int i = 0; i < size; ++i) {
        auto element = env->GetObjectArrayElement(pCommandBuffersArray, i);
        jmethodID getValueMethod = env->GetMethodID(env->GetObjectClass(element), "longValue",
                                                    "()J");
        jlong value = env->CallLongMethod(element, getValueMethod);
        pCommandBuffers.push_back(reinterpret_cast<VkCommandBuffer>(value)); //vkhandle
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto commandBufferCount = static_cast<uint32_t>(pCommandBuffers.size());
    clazzInfo.commandBufferCount = commandBufferCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkCommandBuffer[size];
    std::copy(pCommandBuffers.begin(), pCommandBuffers.end(), copy);
    clazzInfo.pCommandBuffers = copy;
    env->DeleteLocalRef(pCommandBuffersArray); // release reference
}

void
VkSubmitInfoAccessor::getpWaitSemaphores(VkSubmitInfo &clazzInfo) {
    auto pWaitSemaphoresArray = (jobjectArray) env->GetObjectField(obj, pWaitSemaphoresField);
    if (pWaitSemaphoresArray == nullptr) {
        clazzInfo.waitSemaphoreCount = 0;
        clazzInfo.pWaitSemaphores = nullptr;
        env->DeleteLocalRef(pWaitSemaphoresArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pWaitSemaphoresArray);
    // vkhandle array?
    std::vector<VkSemaphore> pWaitSemaphores;
    for (int i = 0; i < size; ++i) {
        auto element = env->GetObjectArrayElement(pWaitSemaphoresArray, i);
        jmethodID getValueMethod = env->GetMethodID(env->GetObjectClass(element), "longValue",
                                                    "()J");
        jlong value = env->CallLongMethod(element, getValueMethod);
        pWaitSemaphores.push_back(reinterpret_cast<VkSemaphore>(value)); //vkhandle
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto waitSemaphoreCount = static_cast<uint32_t>(pWaitSemaphores.size());
    clazzInfo.waitSemaphoreCount = waitSemaphoreCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSemaphore[size];
    std::copy(pWaitSemaphores.begin(), pWaitSemaphores.end(), copy);
    clazzInfo.pWaitSemaphores = copy;
    env->DeleteLocalRef(pWaitSemaphoresArray); // release reference
}

VkSubmitInfoAccessor::~VkSubmitInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

