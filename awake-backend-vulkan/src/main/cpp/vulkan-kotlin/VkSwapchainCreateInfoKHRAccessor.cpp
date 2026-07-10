/*
 *  VkSwapchainCreateInfoKHRAccessor.cpp
 *  Vulkan accessor for VkSwapchainCreateInfoKHR
 *  Created by Ron June Valdoz */

#include <includes/VkSwapchainCreateInfoKHRAccessor.h>

VkSwapchainCreateInfoKHRAccessor::VkSwapchainCreateInfoKHRAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    surfaceField = env->GetFieldID(clazz, "surface", "J");
    minImageCountField = env->GetFieldID(clazz, "minImageCount", "I");
    imageFormatField = env->GetFieldID(clazz, "imageFormat",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkFormat;");
    imageColorSpaceField = env->GetFieldID(clazz, "imageColorSpace",
                                           "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkColorSpaceKHR;");
    imageExtentField = env->GetFieldID(clazz, "imageExtent",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/models/VkExtent2D;");
    imageArrayLayersField = env->GetFieldID(clazz, "imageArrayLayers", "I");
    imageUsageField = env->GetFieldID(clazz, "imageUsage", "I");
    imageSharingModeField = env->GetFieldID(clazz, "imageSharingMode",
                                            "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkSharingMode;");
    pQueueFamilyIndicesField = env->GetFieldID(clazz, "pQueueFamilyIndices", "[I");
    preTransformField = env->GetFieldID(clazz, "preTransform",
                                        "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkSurfaceTransformFlagBitsKHR;");
    compositeAlphaField = env->GetFieldID(clazz, "compositeAlpha",
                                          "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkCompositeAlphaFlagBitsKHR;");
    presentModeField = env->GetFieldID(clazz, "presentMode",
                                       "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkPresentModeKHR;");
    clippedField = env->GetFieldID(clazz, "clipped", "Z");
    oldSwapchainField = env->GetFieldID(clazz, "oldSwapchain", "J");
}

void
VkSwapchainCreateInfoKHRAccessor::getimageExtent(VkSwapchainCreateInfoKHR &clazzInfo) {
    auto imageExtentObj = (jobject) env->GetObjectField(obj, imageExtentField);
    if (imageExtentObj == nullptr) {
        env->DeleteLocalRef(imageExtentObj); // Delete null object reference
        return;
    }
    VkExtent2DAccessor accessor(env, imageExtentObj);
    VkExtent2D ref{};
    accessor.fromObject(ref);
    clazzInfo.imageExtent = ref;
    env->DeleteLocalRef(imageExtentObj); // Delete object reference
}

uint32_t
VkSwapchainCreateInfoKHRAccessor::getimageUsage() {
    return (uint32_t) (jint) env->GetIntField(obj, imageUsageField); // primitive
}

void
VkSwapchainCreateInfoKHRAccessor::getpQueueFamilyIndices(VkSwapchainCreateInfoKHR &clazzInfo) {
    auto pQueueFamilyIndicesArray = (jintArray) env->GetObjectField(obj, pQueueFamilyIndicesField);
    if (pQueueFamilyIndicesArray == nullptr) {
        clazzInfo.queueFamilyIndexCount = 0;
        clazzInfo.pQueueFamilyIndices = nullptr;
        env->DeleteLocalRef(pQueueFamilyIndicesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pQueueFamilyIndicesArray);
    // primitive array?
    std::vector<uint32_t> pQueueFamilyIndices(size);
    env->GetIntArrayRegion(pQueueFamilyIndicesArray, 0, size,
                           reinterpret_cast<jint *>(pQueueFamilyIndices.data()));
    // processing array data
    auto queueFamilyIndexCount = static_cast<uint32_t>(pQueueFamilyIndices.size());
    clazzInfo.queueFamilyIndexCount = queueFamilyIndexCount;
    // Make a copy of the primitive to ensure proper memory management;
    auto copy = new uint32_t[size];
    std::copy(pQueueFamilyIndices.begin(), pQueueFamilyIndices.end(), copy);
    clazzInfo.pQueueFamilyIndices = copy;
    env->DeleteLocalRef(pQueueFamilyIndicesArray); // release reference
}

VkSurfaceTransformFlagBitsKHR
VkSwapchainCreateInfoKHRAccessor::getpreTransform() {
    auto preTransformEnum = (jobject) env->GetObjectField(obj, preTransformField);
    auto enumValue = (VkSurfaceTransformFlagBitsKHR) enum_utils::getEnumFromObject(env,
                                                                                   preTransformEnum);
    env->DeleteLocalRef(preTransformEnum); // release enum reference
    return enumValue;
}

VkBool32
VkSwapchainCreateInfoKHRAccessor::getclipped() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, clippedField); // primitive
}

VkStructureType
VkSwapchainCreateInfoKHRAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkSwapchainCreateInfoKHRAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

uint32_t
VkSwapchainCreateInfoKHRAccessor::getimageArrayLayers() {
    return (uint32_t) (jint) env->GetIntField(obj, imageArrayLayersField); // primitive
}

VkPresentModeKHR
VkSwapchainCreateInfoKHRAccessor::getpresentMode() {
    auto presentModeEnum = (jobject) env->GetObjectField(obj, presentModeField);
    auto enumValue = (VkPresentModeKHR) enum_utils::getEnumFromObject(env, presentModeEnum);
    env->DeleteLocalRef(presentModeEnum); // release enum reference
    return enumValue;
}

void
VkSwapchainCreateInfoKHRAccessor::getpNext(VkSwapchainCreateInfoKHR &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

VkColorSpaceKHR
VkSwapchainCreateInfoKHRAccessor::getimageColorSpace() {
    auto imageColorSpaceEnum = (jobject) env->GetObjectField(obj, imageColorSpaceField);
    auto enumValue = (VkColorSpaceKHR) enum_utils::getEnumFromObject(env, imageColorSpaceEnum);
    env->DeleteLocalRef(imageColorSpaceEnum); // release enum reference
    return enumValue;
}

VkSharingMode
VkSwapchainCreateInfoKHRAccessor::getimageSharingMode() {
    auto imageSharingModeEnum = (jobject) env->GetObjectField(obj, imageSharingModeField);
    auto enumValue = (VkSharingMode) enum_utils::getEnumFromObject(env, imageSharingModeEnum);
    env->DeleteLocalRef(imageSharingModeEnum); // release enum reference
    return enumValue;
}

VkFormat
VkSwapchainCreateInfoKHRAccessor::getimageFormat() {
    auto imageFormatEnum = (jobject) env->GetObjectField(obj, imageFormatField);
    auto enumValue = (VkFormat) enum_utils::getEnumFromObject(env, imageFormatEnum);
    env->DeleteLocalRef(imageFormatEnum); // release enum reference
    return enumValue;
}

void
VkSwapchainCreateInfoKHRAccessor::fromObject(VkSwapchainCreateInfoKHR &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.surface = getsurface(); // VkHandle
    clazzInfo.minImageCount = getminImageCount(); // Primitive uint32_t
    clazzInfo.imageFormat = getimageFormat(); // Enum VkFormat
    clazzInfo.imageColorSpace = getimageColorSpace(); // Enum VkColorSpaceKHR
    getimageExtent(clazzInfo); // Other VkExtent2D
    clazzInfo.imageArrayLayers = getimageArrayLayers(); // Primitive uint32_t
    clazzInfo.imageUsage = getimageUsage(); // Primitive uint32_t
    clazzInfo.imageSharingMode = getimageSharingMode(); // Enum VkSharingMode
    getpQueueFamilyIndices(clazzInfo);  // int Object Array
    clazzInfo.preTransform = getpreTransform(); // Enum VkSurfaceTransformFlagBitsKHR
    clazzInfo.compositeAlpha = getcompositeAlpha(); // Enum VkCompositeAlphaFlagBitsKHR
    clazzInfo.presentMode = getpresentMode(); // Enum VkPresentModeKHR
    clazzInfo.clipped = getclipped(); // Primitive VkBool32
    clazzInfo.oldSwapchain = getoldSwapchain(); // VkHandle
}

uint32_t
VkSwapchainCreateInfoKHRAccessor::getminImageCount() {
    return (uint32_t) (jint) env->GetIntField(obj, minImageCountField); // primitive
}

VkCompositeAlphaFlagBitsKHR
VkSwapchainCreateInfoKHRAccessor::getcompositeAlpha() {
    auto compositeAlphaEnum = (jobject) env->GetObjectField(obj, compositeAlphaField);
    auto enumValue = (VkCompositeAlphaFlagBitsKHR) enum_utils::getEnumFromObject(env,
                                                                                 compositeAlphaEnum);
    env->DeleteLocalRef(compositeAlphaEnum); // release enum reference
    return enumValue;
}

VkSwapchainKHR
VkSwapchainCreateInfoKHRAccessor::getoldSwapchain() {
    auto value = env->GetLongField(obj, oldSwapchainField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkSwapchainKHR>(value); // VkHandle
}

VkSurfaceKHR
VkSwapchainCreateInfoKHRAccessor::getsurface() {
    auto value = env->GetLongField(obj, surfaceField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkSurfaceKHR>(value); // VkHandle
}

VkSwapchainCreateInfoKHRAccessor::~VkSwapchainCreateInfoKHRAccessor() {
    env->DeleteLocalRef(clazz);
}

