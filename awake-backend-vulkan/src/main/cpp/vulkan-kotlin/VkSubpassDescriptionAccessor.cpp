/*
 *  VkSubpassDescriptionAccessor.cpp
 *  Vulkan accessor for VkSubpassDescription
 *  Created by Ron June Valdoz */

#include <includes/VkSubpassDescriptionAccessor.h>

VkSubpassDescriptionAccessor::VkSubpassDescriptionAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                       obj(obj) {
    clazz = env->GetObjectClass(obj);
    flagsField = env->GetFieldID(clazz, "flags", "I");
    pipelineBindPointField = env->GetFieldID(clazz, "pipelineBindPoint",
                                             "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkPipelineBindPoint;");
    pInputAttachmentsField = env->GetFieldID(clazz, "pInputAttachments",
                                             "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkAttachmentReference;");
    pColorAttachmentsField = env->GetFieldID(clazz, "pColorAttachments",
                                             "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkAttachmentReference;");
    pResolveAttachmentsField = env->GetFieldID(clazz, "pResolveAttachments",
                                               "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkAttachmentReference;");
    pDepthStencilAttachmentField = env->GetFieldID(clazz, "pDepthStencilAttachment",
                                                   "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkAttachmentReference;");
    pPreserveAttachmentsField = env->GetFieldID(clazz, "pPreserveAttachments", "[I");
}

uint32_t
VkSubpassDescriptionAccessor::getflags() {
    return (uint32_t) (jint) env->GetIntField(obj, flagsField); // primitive
}

void
VkSubpassDescriptionAccessor::getpInputAttachments(VkSubpassDescription &clazzInfo) {
    auto pInputAttachmentsArray = (jobjectArray) env->GetObjectField(obj, pInputAttachmentsField);
    if (pInputAttachmentsArray == nullptr) {
        clazzInfo.inputAttachmentCount = 0;
        clazzInfo.pInputAttachments = nullptr;
        env->DeleteLocalRef(pInputAttachmentsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pInputAttachmentsArray);
    std::vector<VkAttachmentReference> pInputAttachments;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pInputAttachmentsArray,
                                                            i); // actual type is VkAttachmentReference[];
        // experimental optimize accessor
        VkAttachmentReferenceAccessor accessor(env, element);
        VkAttachmentReference ref{};
        accessor.fromObject(ref);
        pInputAttachments.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto inputAttachmentCount = static_cast<uint32_t>(pInputAttachments.size());
    clazzInfo.inputAttachmentCount = inputAttachmentCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkAttachmentReference[size];
    std::copy(pInputAttachments.begin(), pInputAttachments.end(), copy);
    clazzInfo.pInputAttachments = copy;
    env->DeleteLocalRef(pInputAttachmentsArray); // release reference
}

void
VkSubpassDescriptionAccessor::getpColorAttachments(VkSubpassDescription &clazzInfo) {
    auto pColorAttachmentsArray = (jobjectArray) env->GetObjectField(obj, pColorAttachmentsField);
    if (pColorAttachmentsArray == nullptr) {
        clazzInfo.colorAttachmentCount = 0;
        clazzInfo.pColorAttachments = nullptr;
        env->DeleteLocalRef(pColorAttachmentsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pColorAttachmentsArray);
    std::vector<VkAttachmentReference> pColorAttachments;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pColorAttachmentsArray,
                                                            i); // actual type is VkAttachmentReference[];
        // experimental optimize accessor
        VkAttachmentReferenceAccessor accessor(env, element);
        VkAttachmentReference ref{};
        accessor.fromObject(ref);
        pColorAttachments.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto colorAttachmentCount = static_cast<uint32_t>(pColorAttachments.size());
    clazzInfo.colorAttachmentCount = colorAttachmentCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkAttachmentReference[size];
    std::copy(pColorAttachments.begin(), pColorAttachments.end(), copy);
    clazzInfo.pColorAttachments = copy;
    env->DeleteLocalRef(pColorAttachmentsArray); // release reference
}

VkPipelineBindPoint
VkSubpassDescriptionAccessor::getpipelineBindPoint() {
    auto pipelineBindPointEnum = (jobject) env->GetObjectField(obj, pipelineBindPointField);
    auto enumValue = (VkPipelineBindPoint) enum_utils::getEnumFromObject(env,
                                                                         pipelineBindPointEnum);
    env->DeleteLocalRef(pipelineBindPointEnum); // release enum reference
    return enumValue;
}

void
VkSubpassDescriptionAccessor::getpResolveAttachments(VkSubpassDescription &clazzInfo) {
    auto pResolveAttachmentsArray = (jobjectArray) env->GetObjectField(obj,
                                                                       pResolveAttachmentsField);
    if (pResolveAttachmentsArray == nullptr) {
        clazzInfo.pResolveAttachments = nullptr;
        env->DeleteLocalRef(pResolveAttachmentsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pResolveAttachmentsArray);
    std::vector<VkAttachmentReference> pResolveAttachments;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pResolveAttachmentsArray,
                                                            i); // actual type is VkAttachmentReference[];
        // experimental optimize accessor
        VkAttachmentReferenceAccessor accessor(env, element);
        VkAttachmentReference ref{};
        accessor.fromObject(ref);
        pResolveAttachments.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkAttachmentReference[size];
    std::copy(pResolveAttachments.begin(), pResolveAttachments.end(), copy);
    clazzInfo.pResolveAttachments = copy;
    env->DeleteLocalRef(pResolveAttachmentsArray); // release reference
}

void
VkSubpassDescriptionAccessor::fromObject(VkSubpassDescription &clazzInfo) {
    clazzInfo.flags = getflags(); // Primitive uint32_t
    clazzInfo.pipelineBindPoint = getpipelineBindPoint(); // Enum VkPipelineBindPoint
    getpInputAttachments(clazzInfo);  // VkAttachmentReference Object Array
    getpColorAttachments(clazzInfo);  // VkAttachmentReference Object Array
    getpResolveAttachments(clazzInfo);  // VkAttachmentReference Object Array
    getpDepthStencilAttachment(clazzInfo);  // VkAttachmentReference Object Array
    getpPreserveAttachments(clazzInfo);  // int Object Array
}

void
VkSubpassDescriptionAccessor::getpPreserveAttachments(VkSubpassDescription &clazzInfo) {
    auto pPreserveAttachmentsArray = (jintArray) env->GetObjectField(obj,
                                                                     pPreserveAttachmentsField);
    if (pPreserveAttachmentsArray == nullptr) {
        clazzInfo.preserveAttachmentCount = 0;
        clazzInfo.pPreserveAttachments = nullptr;
        env->DeleteLocalRef(pPreserveAttachmentsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pPreserveAttachmentsArray);
    // primitive array?
    std::vector<uint32_t> pPreserveAttachments(size);
    env->GetIntArrayRegion(pPreserveAttachmentsArray, 0, size,
                           reinterpret_cast<jint *>(pPreserveAttachments.data()));
    // processing array data
    auto preserveAttachmentCount = static_cast<uint32_t>(pPreserveAttachments.size());
    clazzInfo.preserveAttachmentCount = preserveAttachmentCount;
    // Make a copy of the primitive to ensure proper memory management;
    auto copy = new uint32_t[size];
    std::copy(pPreserveAttachments.begin(), pPreserveAttachments.end(), copy);
    clazzInfo.pPreserveAttachments = copy;
    env->DeleteLocalRef(pPreserveAttachmentsArray); // release reference
}

void
VkSubpassDescriptionAccessor::getpDepthStencilAttachment(VkSubpassDescription &clazzInfo) {
    auto pDepthStencilAttachmentArray = (jobjectArray) env->GetObjectField(obj,
                                                                           pDepthStencilAttachmentField);
    if (pDepthStencilAttachmentArray == nullptr) {
        clazzInfo.pDepthStencilAttachment = nullptr;
        env->DeleteLocalRef(pDepthStencilAttachmentArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pDepthStencilAttachmentArray);
    std::vector<VkAttachmentReference> pDepthStencilAttachment;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pDepthStencilAttachmentArray,
                                                            i); // actual type is VkAttachmentReference[];
        // experimental optimize accessor
        VkAttachmentReferenceAccessor accessor(env, element);
        VkAttachmentReference ref{};
        accessor.fromObject(ref);
        pDepthStencilAttachment.push_back(ref);
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkAttachmentReference[size];
    std::copy(pDepthStencilAttachment.begin(), pDepthStencilAttachment.end(), copy);
    clazzInfo.pDepthStencilAttachment = copy;
    env->DeleteLocalRef(pDepthStencilAttachmentArray); // release reference
}

VkSubpassDescriptionAccessor::~VkSubpassDescriptionAccessor() {
    env->DeleteLocalRef(clazz);
}

