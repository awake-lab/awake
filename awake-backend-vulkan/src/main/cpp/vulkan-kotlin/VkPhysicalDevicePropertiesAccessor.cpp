/*
 *  VkPhysicalDevicePropertiesAccessor.cpp
 *  Vulkan accessor for VkPhysicalDeviceProperties
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDevicePropertiesAccessor.h>

VkPhysicalDevicePropertiesAccessor::VkPhysicalDevicePropertiesAccessor(JNIEnv *env, jobject obj)
        : env(env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    apiVersionField = env->GetFieldID(clazz, "apiVersion", "I");
    driverVersionField = env->GetFieldID(clazz, "driverVersion", "I");
    vendorIDField = env->GetFieldID(clazz, "vendorID", "I");
    deviceIDField = env->GetFieldID(clazz, "deviceID", "I");
    deviceTypeField = env->GetFieldID(clazz, "deviceType",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkPhysicalDeviceType;");
    deviceNameField = env->GetFieldID(clazz, "deviceName", "[C");
    pipelineCacheUUIDField = env->GetFieldID(clazz, "pipelineCacheUUID", "[B");
    limitsField = env->GetFieldID(clazz, "limits",
                                  "Lio/github/ronjunevaldoz/awake/vulkan/models/physicaldevice/VkPhysicalDeviceLimits;");
    sparsePropertiesField = env->GetFieldID(clazz, "sparseProperties",
                                            "Lio/github/ronjunevaldoz/awake/vulkan/models/physicaldevice/VkPhysicalDeviceSparseProperties;");
}

void
VkPhysicalDevicePropertiesAccessor::getdeviceName(VkPhysicalDeviceProperties &clazzInfo) {
    auto deviceNameArray = (jcharArray) env->GetObjectField(obj, deviceNameField);
    if (deviceNameArray == nullptr) {
        // const array
        // clazzInfo.deviceName = nullptr;
        env->DeleteLocalRef(deviceNameArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(deviceNameArray);
    // primitive array?
    std::vector<char> deviceName(size);
    env->GetCharArrayRegion(deviceNameArray, 0, size, reinterpret_cast<jchar *>(deviceName.data()));
    // processing array data
    std::copy(deviceName.begin(), deviceName.end(), clazzInfo.deviceName); // fixed array size
    env->DeleteLocalRef(deviceNameArray); // release reference
}

void
VkPhysicalDevicePropertiesAccessor::getlimits(VkPhysicalDeviceProperties &clazzInfo) {
    auto limitsObj = (jobject) env->GetObjectField(obj, limitsField);
    if (limitsObj == nullptr) {
        env->DeleteLocalRef(limitsObj); // Delete null object reference
        return;
    }
    VkPhysicalDeviceLimitsAccessor accessor(env, limitsObj);
    VkPhysicalDeviceLimits ref{};
    accessor.fromObject(ref);
    clazzInfo.limits = ref;
    env->DeleteLocalRef(limitsObj); // Delete object reference
}

void
VkPhysicalDevicePropertiesAccessor::getsparseProperties(VkPhysicalDeviceProperties &clazzInfo) {
    auto sparsePropertiesObj = (jobject) env->GetObjectField(obj, sparsePropertiesField);
    if (sparsePropertiesObj == nullptr) {
        env->DeleteLocalRef(sparsePropertiesObj); // Delete null object reference
        return;
    }
    VkPhysicalDeviceSparsePropertiesAccessor accessor(env, sparsePropertiesObj);
    VkPhysicalDeviceSparseProperties ref{};
    accessor.fromObject(ref);
    clazzInfo.sparseProperties = ref;
    env->DeleteLocalRef(sparsePropertiesObj); // Delete object reference
}

uint32_t
VkPhysicalDevicePropertiesAccessor::getdeviceID() {
    return (uint32_t) (jint) env->GetIntField(obj, deviceIDField); // primitive
}

VkPhysicalDeviceType
VkPhysicalDevicePropertiesAccessor::getdeviceType() {
    auto deviceTypeEnum = (jobject) env->GetObjectField(obj, deviceTypeField);
    auto enumValue = (VkPhysicalDeviceType) enum_utils::getEnumFromObject(env, deviceTypeEnum);
    env->DeleteLocalRef(deviceTypeEnum); // release enum reference
    return enumValue;
}

void
VkPhysicalDevicePropertiesAccessor::fromObject(VkPhysicalDeviceProperties &clazzInfo) {
    clazzInfo.apiVersion = getapiVersion(); // Primitive uint32_t
    clazzInfo.driverVersion = getdriverVersion(); // Primitive uint32_t
    clazzInfo.vendorID = getvendorID(); // Primitive uint32_t
    clazzInfo.deviceID = getdeviceID(); // Primitive uint32_t
    clazzInfo.deviceType = getdeviceType(); // Enum VkPhysicalDeviceType
    getdeviceName(clazzInfo);  // char Object Array
    getpipelineCacheUUID(clazzInfo);  // byte Object Array
    getlimits(clazzInfo); // Other VkPhysicalDeviceLimits
    getsparseProperties(clazzInfo); // Other VkPhysicalDeviceSparseProperties
}

void
VkPhysicalDevicePropertiesAccessor::getpipelineCacheUUID(VkPhysicalDeviceProperties &clazzInfo) {
    auto pipelineCacheUUIDArray = (jbyteArray) env->GetObjectField(obj, pipelineCacheUUIDField);
    if (pipelineCacheUUIDArray == nullptr) {
        // const array
        // clazzInfo.pipelineCacheUUID = nullptr;
        env->DeleteLocalRef(pipelineCacheUUIDArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pipelineCacheUUIDArray);
    // primitive array?
    std::vector<uint8_t> pipelineCacheUUID(size);
    env->GetByteArrayRegion(pipelineCacheUUIDArray, 0, size,
                            reinterpret_cast<jbyte *>(pipelineCacheUUID.data()));
    // processing array data
    std::copy(pipelineCacheUUID.begin(), pipelineCacheUUID.end(),
              clazzInfo.pipelineCacheUUID); // fixed array size
    env->DeleteLocalRef(pipelineCacheUUIDArray); // release reference
}

uint32_t
VkPhysicalDevicePropertiesAccessor::getvendorID() {
    return (uint32_t) (jint) env->GetIntField(obj, vendorIDField); // primitive
}

uint32_t
VkPhysicalDevicePropertiesAccessor::getapiVersion() {
    return (uint32_t) (jint) env->GetIntField(obj, apiVersionField); // primitive
}

uint32_t
VkPhysicalDevicePropertiesAccessor::getdriverVersion() {
    return (uint32_t) (jint) env->GetIntField(obj, driverVersionField); // primitive
}

VkPhysicalDevicePropertiesAccessor::~VkPhysicalDevicePropertiesAccessor() {
    env->DeleteLocalRef(clazz);
}

