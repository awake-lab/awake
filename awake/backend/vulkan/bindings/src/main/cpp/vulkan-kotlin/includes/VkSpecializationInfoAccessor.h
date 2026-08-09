/*
 *  VkSpecializationInfoAccessor.h
 *  Vulkan accessor for VkSpecializationInfo
 *  Created by Ron June Valdoz */

#ifndef VKSPECIALIZATIONINFOACCESSOR_H
#define VKSPECIALIZATIONINFOACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>
#include <includes/VkSpecializationMapEntryAccessor.h>

class VkSpecializationInfoAccessor {
private:
    JNIEnv *env;
    jobject obj;
    jclass clazz;
    jfieldID mapEntryCountField;
    jfieldID pMapEntriesField;
    jfieldID dataSizeField;
    jfieldID pDataField;
public:
    VkSpecializationInfoAccessor(JNIEnv *env, jobject obj);

    uint32_t getmapEntryCount();

    void fromObject(VkSpecializationInfo &clazzInfo);

    uint64_t getdataSize();

    void getpMapEntries(VkSpecializationInfo &clazzInfo);

    void getpData(VkSpecializationInfo &clazzInfo);

    ~VkSpecializationInfoAccessor();
};
#endif // VKSPECIALIZATIONINFOACCESSOR_H