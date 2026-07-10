/*
 *  VkRenderPassCreateInfoAccessor.cpp
 *  Vulkan accessor for VkRenderPassCreateInfo
 *  Created by Ron June Valdoz */

#include <includes/VkRenderPassCreateInfoAccessor.h>

VkRenderPassCreateInfoAccessor::VkRenderPassCreateInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                           obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pAttachmentsField = env->GetFieldID(clazz, "pAttachments",
                                        "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkAttachmentDescription;");
    pSubpassesField = env->GetFieldID(clazz, "pSubpasses",
                                      "[Lio/github/ronjunevaldoz/awake/vulkan/models/info/VkSubpassDescription;");
    pDependenciesField = env->GetFieldID(clazz, "pDependencies",
                                         "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkSubpassDependency;");
}

VkStructureType
VkRenderPassCreateInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

uint32_t
VkRenderPassCreateInfoAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkRenderPassCreateInfoAccessor::getpNext(VkRenderPassCreateInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkRenderPassCreateInfoAccessor::getpAttachments(VkRenderPassCreateInfo &clazzInfo) {
    auto pAttachmentsArray = (jobjectArray) env->GetObjectField(obj, pAttachmentsField);
    if (pAttachmentsArray == nullptr) {
        clazzInfo.attachmentCount = 0;
        clazzInfo.pAttachments = nullptr;
        env->DeleteLocalRef(pAttachmentsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pAttachmentsArray);
    std::vector<VkAttachmentDescription> pAttachments;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pAttachmentsArray,
                                                            i); // actual type is VkAttachmentDescription[];
        // experimental optimize accessor
        VkAttachmentDescriptionAccessor accessor(env, element);
        VkAttachmentDescription ref{};
        accessor.fromObject(ref);
        pAttachments.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto attachmentCount = static_cast<uint32_t>(pAttachments.size());
    clazzInfo.attachmentCount = attachmentCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkAttachmentDescription[size];
    std::copy(pAttachments.begin(), pAttachments.end(), copy);
    clazzInfo.pAttachments = copy;
    env->DeleteLocalRef(pAttachmentsArray); // release reference
}

void
VkRenderPassCreateInfoAccessor::getpDependencies(VkRenderPassCreateInfo &clazzInfo) {
    auto pDependenciesArray = (jobjectArray) env->GetObjectField(obj, pDependenciesField);
    if (pDependenciesArray == nullptr) {
        clazzInfo.dependencyCount = 0;
        clazzInfo.pDependencies = nullptr;
        env->DeleteLocalRef(pDependenciesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pDependenciesArray);
    std::vector<VkSubpassDependency> pDependencies;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pDependenciesArray,
                                                            i); // actual type is VkSubpassDependency[];
        // experimental optimize accessor
        VkSubpassDependencyAccessor accessor(env, element);
        VkSubpassDependency ref{};
        accessor.fromObject(ref);
        pDependencies.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto dependencyCount = static_cast<uint32_t>(pDependencies.size());
    clazzInfo.dependencyCount = dependencyCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSubpassDependency[size];
    std::copy(pDependencies.begin(), pDependencies.end(), copy);
    clazzInfo.pDependencies = copy;
    env->DeleteLocalRef(pDependenciesArray); // release reference
}

void
VkRenderPassCreateInfoAccessor::fromObject(VkRenderPassCreateInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.flags = getflags(); // Primitive uint32_t
    getpAttachments(clazzInfo);  // VkAttachmentDescription Object Array
    getpSubpasses(clazzInfo);  // VkSubpassDescription Object Array
    getpDependencies(clazzInfo);  // VkSubpassDependency Object Array
}

void
VkRenderPassCreateInfoAccessor::getpSubpasses(VkRenderPassCreateInfo &clazzInfo) {
    auto pSubpassesArray = (jobjectArray) env->GetObjectField(obj, pSubpassesField);
    if (pSubpassesArray == nullptr) {
        clazzInfo.subpassCount = 0;
        clazzInfo.pSubpasses = nullptr;
        env->DeleteLocalRef(pSubpassesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pSubpassesArray);
    std::vector<VkSubpassDescription> pSubpasses;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pSubpassesArray,
                                                            i); // actual type is VkSubpassDescription[];
        // experimental optimize accessor
        VkSubpassDescriptionAccessor accessor(env, element);
        VkSubpassDescription ref{};
        accessor.fromObject(ref);
        pSubpasses.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto subpassCount = static_cast<uint32_t>(pSubpasses.size());
    clazzInfo.subpassCount = subpassCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkSubpassDescription[size];
    std::copy(pSubpasses.begin(), pSubpasses.end(), copy);
    clazzInfo.pSubpasses = copy;
    env->DeleteLocalRef(pSubpassesArray); // release reference
}

VkRenderPassCreateInfoAccessor::~VkRenderPassCreateInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

