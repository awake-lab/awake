/*
 *  VkRenderPassBeginInfoAccessor.cpp
 *  Vulkan accessor for VkRenderPassBeginInfo
 *  Created by Ron June Valdoz */

#include <includes/VkRenderPassBeginInfoAccessor.h>

VkRenderPassBeginInfoAccessor::VkRenderPassBeginInfoAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                         obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    renderPassField = env->GetFieldID(clazz, "renderPass", "J");
    framebufferField = env->GetFieldID(clazz, "framebuffer", "J");
    renderAreaField = env->GetFieldID(clazz, "renderArea",
                                      "Lio/github/ronjunevaldoz/awake/vulkan/models/VkRect2D;");
    pClearValuesField = env->GetFieldID(clazz, "pClearValues",
                                        "[Lio/github/ronjunevaldoz/awake/vulkan/models/VkClearValue;");
}

VkStructureType
VkRenderPassBeginInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

VkFramebuffer
VkRenderPassBeginInfoAccessor::getframebuffer() {
    auto value = env->GetLongField(obj, framebufferField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkFramebuffer>(value); // VkHandle
}

void
VkRenderPassBeginInfoAccessor::fromObject(VkRenderPassBeginInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.renderPass = getrenderPass(); // VkHandle
    clazzInfo.framebuffer = getframebuffer(); // VkHandle
    getrenderArea(clazzInfo); // Other VkRect2D
    getpClearValues(clazzInfo);  // VkClearValue Object Array
}

void
VkRenderPassBeginInfoAccessor::getpClearValues(VkRenderPassBeginInfo &clazzInfo) {
    auto pClearValuesArray = (jobjectArray) env->GetObjectField(obj, pClearValuesField);
    if (pClearValuesArray == nullptr) {
        clazzInfo.clearValueCount = 0;
        clazzInfo.pClearValues = nullptr;
        env->DeleteLocalRef(pClearValuesArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pClearValuesArray);
    std::vector<VkClearValue> pClearValues;
    for (int i = 0; i < size; ++i) {
        auto element = (jobject) env->GetObjectArrayElement(pClearValuesArray,
                                                            i); // actual type is VkClearValue[];
        // sealed class
        auto vkClearColorValueClass = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkClearColorValue");
        if (env->IsInstanceOf(element, vkClearColorValueClass)) {
            VkClearColorValueAccessor accessor(env, element);
            VkClearColorValue ref{};
            accessor.fromObject(ref);
            VkClearValue value = {.color {ref}};
            pClearValues.push_back(value);
            env->DeleteLocalRef(vkClearColorValueClass);
            continue;
        }
        auto vkClearDepthStencilValueClass = env->FindClass(
                "io/github/ronjunevaldoz/awake/vulkan/models/VkClearDepthStencilValue");
        if (env->IsInstanceOf(element, vkClearDepthStencilValueClass)) {
            VkClearDepthStencilValueAccessor accessor(env, element);
            VkClearDepthStencilValue ref{};
            accessor.fromObject(ref);
            VkClearValue value = {.depthStencil {ref}};
            pClearValues.push_back(value);
            env->DeleteLocalRef(vkClearDepthStencilValueClass);
            continue;
        }
        env->DeleteLocalRef(element); // release element reference
    }
    // processing array data
    auto clearValueCount = static_cast<uint32_t>(pClearValues.size());
    clazzInfo.clearValueCount = clearValueCount;
    // Make a copy of the object to ensure proper memory management;
    // jobjectArray
    auto copy = new VkClearValue[size];
    std::copy(pClearValues.begin(), pClearValues.end(), copy);
    clazzInfo.pClearValues = copy;
    env->DeleteLocalRef(pClearValuesArray); // release reference
}

VkRenderPass
VkRenderPassBeginInfoAccessor::getrenderPass() {
    auto value = env->GetLongField(obj, renderPassField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkRenderPass>(value); // VkHandle
}

void
VkRenderPassBeginInfoAccessor::getpNext(VkRenderPassBeginInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

void
VkRenderPassBeginInfoAccessor::getrenderArea(VkRenderPassBeginInfo &clazzInfo) {
    auto renderAreaObj = (jobject) env->GetObjectField(obj, renderAreaField);
    if (renderAreaObj == nullptr) {
        env->DeleteLocalRef(renderAreaObj); // Delete null object reference
        return;
    }
    VkRect2DAccessor accessor(env, renderAreaObj);
    VkRect2D ref{};
    accessor.fromObject(ref);
    clazzInfo.renderArea = ref;
    env->DeleteLocalRef(renderAreaObj); // Delete object reference
}

VkRenderPassBeginInfoAccessor::~VkRenderPassBeginInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

