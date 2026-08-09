/*
 *  VkCommandBufferInheritanceInfoAccessor.cpp
 *  Vulkan accessor for VkCommandBufferInheritanceInfo
 *  Created by Ron June Valdoz */

#include <includes/VkCommandBufferInheritanceInfoAccessor.h>

VkCommandBufferInheritanceInfoAccessor::VkCommandBufferInheritanceInfoAccessor(JNIEnv *env,
                                                                               jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
    sTypeField = env->GetFieldID(clazz, "sType",
                                 "Lio/github/ronjunevaldoz/awake/vulkan/enums/VkStructureType;");
    pNextField = env->GetFieldID(clazz, "pNext", "Ljava/lang/Object;");
    renderPassField = env->GetFieldID(clazz, "renderPass", "J");
    subpassField = env->GetFieldID(clazz, "subpass", "I");
    framebufferField = env->GetFieldID(clazz, "framebuffer", "J");
    occlusionQueryEnableField = env->GetFieldID(clazz, "occlusionQueryEnable", "Z");
    queryFlagsField = env->GetFieldID(clazz, "queryFlags", "I");
    pipelineStatisticsField = env->GetFieldID(clazz, "pipelineStatistics", "I");
}

VkStructureType
VkCommandBufferInheritanceInfoAccessor::getsType() {
    auto sTypeEnum = (jobject) env->GetObjectField(obj, sTypeField);
    auto enumValue = (VkStructureType) enum_utils::getEnumFromObject(env, sTypeEnum);
    env->DeleteLocalRef(sTypeEnum); // release enum reference
    return enumValue;
}

void
VkCommandBufferInheritanceInfoAccessor::getpNext(VkCommandBufferInheritanceInfo &clazzInfo) {
    auto ref = (void *) (jobject) env->GetObjectField(obj, pNextField); // Any Object
    clazzInfo.pNext = ref;
}

uint32_t
VkCommandBufferInheritanceInfoAccessor::getqueryFlags() {
    return (uint32_t) (jint) env->GetIntField(obj, queryFlagsField); // primitive
}

VkFramebuffer
VkCommandBufferInheritanceInfoAccessor::getframebuffer() {
    auto value = env->GetLongField(obj, framebufferField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkFramebuffer>(value); // VkHandle
}

VkBool32
VkCommandBufferInheritanceInfoAccessor::getocclusionQueryEnable() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, occlusionQueryEnableField); // primitive
}

void
VkCommandBufferInheritanceInfoAccessor::fromObject(VkCommandBufferInheritanceInfo &clazzInfo) {
    clazzInfo.sType = getsType(); // Enum VkStructureType
    getpNext(clazzInfo); // Other void*
    clazzInfo.renderPass = getrenderPass(); // VkHandle
    clazzInfo.subpass = getsubpass(); // Primitive uint32_t
    clazzInfo.framebuffer = getframebuffer(); // VkHandle
    clazzInfo.occlusionQueryEnable = getocclusionQueryEnable(); // Primitive VkBool32
    clazzInfo.queryFlags = getqueryFlags(); // Primitive uint32_t
    clazzInfo.pipelineStatistics = getpipelineStatistics(); // Primitive uint32_t
}

uint32_t
VkCommandBufferInheritanceInfoAccessor::getsubpass() {
    return (uint32_t) (jint) env->GetIntField(obj, subpassField); // primitive
}

VkRenderPass
VkCommandBufferInheritanceInfoAccessor::getrenderPass() {
    auto value = env->GetLongField(obj, renderPassField);
    if (value == 0) {
        return VK_NULL_HANDLE;
    }
    return reinterpret_cast<VkRenderPass>(value); // VkHandle
}

uint32_t
VkCommandBufferInheritanceInfoAccessor::getpipelineStatistics() {
    return (uint32_t) (jint) env->GetIntField(obj, pipelineStatisticsField); // primitive
}

VkCommandBufferInheritanceInfoAccessor::~VkCommandBufferInheritanceInfoAccessor() {
    env->DeleteLocalRef(clazz);
}

