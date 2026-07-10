/*
 *  VkPresentInfoKHRAccessor.cpp
 *  Vulkan accessor for VkPresentInfoKHR
 *  Created by Ron June Valdoz */

#include <includes/VkPresentInfoKHRAccessor.h>

VkPresentInfoKHRAccessor::VkPresentInfoKHRAccessor(JNIEnv *env, jobject obj) : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    pWaitSemaphoresField = env->GetFieldID(clazz, "pWaitSemaphores", "[Ljava/lang/Long;");
    pSwapchainsField = env->GetFieldID(clazz, "pSwapchains", "[Ljava/lang/Long;");
    pImageIndicesField = env->GetFieldID(clazz, "pImageIndices", "[I");
    pResultsField = env->GetFieldID(clazz, "pResults",
                                    "[Lio/github/ronjunevaldoz/awake/vulkan/enums/VkResult;");
}

VkStructureType
VkPresentInfoKHRAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkPresentInfoKHRAccessor::getpImageIndices(VkPresentInfoKHR &clazzInfo) {
    auto pImageIndicesArray = (jintArray) env->GetObjectField(obj, pImageIndicesField);
    if (pImageIndicesArray == nullptr) {
        clazzInfo.pImageIndices = nullptr;
        env->DeleteLocalRef(pImageIndicesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pImageIndicesArray);
    // primitive array?
    std::vector<uint32_t> pImageIndices(size);
    env->GetIntArrayRegion(pImageIndicesArray, 0, size,
                           reinterpret_cast<jint *>(pImageIndices.data()));
    // processing array data
    // Make a copy of the primitive to ensure proper memory management;
    auto copy = new uint32_t[size];
    std::copy(pImageIndices.begin(), pImageIndices.end(), copy);
    clazzInfo.pImageIndices = copy;
    env->DeleteLocalRef(pImageIndicesArray); // release reference
}

void
VkPresentInfoKHRAccessor::getpSwapchains(VkPresentInfoKHR &clazzInfo) {
    auto pSwapchainsArray = (jobjectArray) env->GetObjectField(obj, pSwapchainsField);
    if (pSwapchainsArray == nullptr) {
        clazzInfo.swapchainCount = 0;
        clazzInfo.pSwapchains = nullptr;
        env->DeleteLocalRef(pSwapchainsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pSwapchainsArray);
    // vkhandle array?
    std::vector<VkSwapchainKHR> pSwapchains;
    for (int i = 0; i < size; ++i) {
        auto element = env->GetObjectArrayElement(pSwapchainsArray, i);
        jmethodID getValueMethod = env->GetMethodID(env->GetObjectClass(element), "longValue",
                                                    "()J");
        jlong value = env->CallLongMethod(element, getValueMethod);
        pSwapchains.push_back(reinterpret_cast<VkSwapchainKHR>(value)); //vkhandle
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto swapchainCount = static_cast<uint32_t>(pSwapchains.size());
    clazzInfo.swapchainCount = swapchainCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSwapchainKHR[size];
    std::copy(pSwapchains.begin(), pSwapchains.end(), copy);
    clazzInfo.pSwapchains = copy;
    env->DeleteLocalRef(pSwapchainsArray); // release reference
}

void
VkPresentInfoKHRAccessor::getpWaitSemaphores(VkPresentInfoKHR &clazzInfo) {
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

void
VkPresentInfoKHRAccessor::getpNext(VkPresentInfoKHR &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkPresentInfoKHRAccessor::fromObject(VkPresentInfoKHR &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    getpWaitSemaphores(clazzInfo);  // Long Object Array
    getpSwapchains(clazzInfo);  // Long Object Array
    getpImageIndices(clazzInfo);  // int Object Array
    getpResults(clazzInfo);  // VkResult Object Array
}

void
VkPresentInfoKHRAccessor::getpResults(VkPresentInfoKHR &clazzInfo) {
    auto pResultsArray = (jobjectArray) env->GetObjectField(obj, pResultsField);
    if (pResultsArray == nullptr) {
        clazzInfo.pResults = nullptr;
        env->DeleteLocalRef(pResultsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pResultsArray);
    std::vector<VkResult> pResults;
    for (int i = 0; i < size; ++i) {
        auto element = (jobjectArray) env->GetObjectArrayElement(pResultsArray,
                                                                 i); // actual type is VkResult[];
        pResults.push_back(
                static_cast<VkResult>(enum_utils::getEnumFromObject(env, element))); // type is enum
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkResult[size];
    std::copy(pResults.begin(), pResults.end(), copy);
    clazzInfo.pResults = copy;
    env->DeleteLocalRef(pResultsArray); // release reference
}

VkPresentInfoKHRAccessor::~VkPresentInfoKHRAccessor() {
    env->DeleteLocalRef(clazz);
}

