/*
 *  VkSpecializationInfoAccessor.cpp
 *  Vulkan accessor for VkSpecializationInfo
 *  Created by Ron June Valdoz */

#include <includes/VkSpecializationInfoAccessor.h>

VkSpecializationInfoAccessor::VkSpecializationInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                       obj(obj) {
    clazz = env->GetObjectClass(obj);
    mapEntryCountField = env->GetFieldID(clazz, "mapEntryCount", "I");
    pMapEntriesField = env->GetFieldID(clazz, "pMapEntries",
                                       "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/pipeline/VkSpecializationMapEntry;");
    dataSizeField = env->GetFieldID(clazz, "dataSize", "J");
    pDataField = env->GetFieldID(clazz, "pData", "[Ljava/lang/Object;");
}

uint32_t
VkSpecializationInfoAccessor::getmapEntryCount() {
    return (uint32_t) (jint) env->GetIntField(obj, mapEntryCountField); // primitive
}

void
VkSpecializationInfoAccessor::fromObject(VkSpecializationInfo &clazzInfo) {
    clazzInfo.mapEntryCount = getmapEntryCount(); // Primitive uint32_t
    getpMapEntries(clazzInfo);  // VkSpecializationMapEntry Object Array
    clazzInfo.dataSize = getdataSize(); // Primitive uint64_t
    getpData(clazzInfo);  // Object Object Array
}

uint64_t
VkSpecializationInfoAccessor::getdataSize() {
    return (uint64_t) (jlong) env->GetLongField(obj, dataSizeField); // primitive
}

void
VkSpecializationInfoAccessor::getpMapEntries(VkSpecializationInfo &clazzInfo) {
    auto pMapEntriesArray = (jobjectArray) env->GetObjectField(obj, pMapEntriesField);
    if (pMapEntriesArray == nullptr) {
        clazzInfo.pMapEntries = nullptr;
        env->DeleteLocalRef(pMapEntriesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pMapEntriesArray);
    std::vector<VkSpecializationMapEntry> pMapEntries;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pMapEntriesArray,
                                                            i); // actual type is VkSpecializationMapEntry[];
        // experimental optimize accessor
        VkSpecializationMapEntryAccessor accessor(env, element);
        VkSpecializationMapEntry ref{};
        accessor.fromObject(ref);
        pMapEntries.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSpecializationMapEntry[size];
    std::copy(pMapEntries.begin(), pMapEntries.end(), copy);
    clazzInfo.pMapEntries = copy;
    env->DeleteLocalRef(pMapEntriesArray); // release reference
}

void
VkSpecializationInfoAccessor::getpData(VkSpecializationInfo &clazzInfo) {
    auto pDataArray = (jobjectArray) env->GetObjectField(obj, pDataField);
    if (pDataArray == nullptr) {
        clazzInfo.pData = nullptr;
        env->DeleteLocalRef(pDataArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pDataArray);
    std::vector<void *> pData;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pDataArray,
                                                            i); // actual type is Object[];
        pData.push_back(element); // type is Any??
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new const void *[size];
    std::copy(pData.begin(), pData.end(), copy);
    clazzInfo.pData = copy;
    env->DeleteLocalRef(pDataArray); // release reference
}

VkSpecializationInfoAccessor::~VkSpecializationInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

