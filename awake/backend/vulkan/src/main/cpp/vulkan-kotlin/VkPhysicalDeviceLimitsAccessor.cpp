/*
 *  VkPhysicalDeviceLimitsAccessor.cpp
 *  Vulkan accessor for VkPhysicalDeviceLimits
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceLimitsAccessor.h>

VkPhysicalDeviceLimitsAccessor::VkPhysicalDeviceLimitsAccessor(JNIEnv *env, jobject obj) : env(env),
                                                                                           obj(obj) {
    clazz = env->GetObjectClass(obj);
    maxImageDimension1DField = env->GetFieldID(clazz, "maxImageDimension1D", "I");
    maxImageDimension2DField = env->GetFieldID(clazz, "maxImageDimension2D", "I");
    maxImageDimension3DField = env->GetFieldID(clazz, "maxImageDimension3D", "I");
    maxImageDimensionCubeField = env->GetFieldID(clazz, "maxImageDimensionCube", "I");
    maxImageArrayLayersField = env->GetFieldID(clazz, "maxImageArrayLayers", "I");
    maxTexelBufferElementsField = env->GetFieldID(clazz, "maxTexelBufferElements", "I");
    maxUniformBufferRangeField = env->GetFieldID(clazz, "maxUniformBufferRange", "I");
    maxStorageBufferRangeField = env->GetFieldID(clazz, "maxStorageBufferRange", "I");
    maxPushConstantsSizeField = env->GetFieldID(clazz, "maxPushConstantsSize", "I");
    maxMemoryAllocationCountField = env->GetFieldID(clazz, "maxMemoryAllocationCount", "I");
    maxSamplerAllocationCountField = env->GetFieldID(clazz, "maxSamplerAllocationCount", "I");
    bufferImageGranularityField = env->GetFieldID(clazz, "bufferImageGranularity", "J");
    sparseAddressSpaceSizeField = env->GetFieldID(clazz, "sparseAddressSpaceSize", "J");
    maxBoundDescriptorSetsField = env->GetFieldID(clazz, "maxBoundDescriptorSets", "I");
    maxPerStageDescriptorSamplersField = env->GetFieldID(clazz, "maxPerStageDescriptorSamplers",
                                                         "I");
    maxPerStageDescriptorUniformBuffersField = env->GetFieldID(clazz,
                                                               "maxPerStageDescriptorUniformBuffers",
                                                               "I");
    maxPerStageDescriptorStorageBuffersField = env->GetFieldID(clazz,
                                                               "maxPerStageDescriptorStorageBuffers",
                                                               "I");
    maxPerStageDescriptorSampledImagesField = env->GetFieldID(clazz,
                                                              "maxPerStageDescriptorSampledImages",
                                                              "I");
    maxPerStageDescriptorStorageImagesField = env->GetFieldID(clazz,
                                                              "maxPerStageDescriptorStorageImages",
                                                              "I");
    maxPerStageDescriptorInputAttachmentsField = env->GetFieldID(clazz,
                                                                 "maxPerStageDescriptorInputAttachments",
                                                                 "I");
    maxPerStageResourcesField = env->GetFieldID(clazz, "maxPerStageResources", "I");
    maxDescriptorSetSamplersField = env->GetFieldID(clazz, "maxDescriptorSetSamplers", "I");
    maxDescriptorSetUniformBuffersField = env->GetFieldID(clazz, "maxDescriptorSetUniformBuffers",
                                                          "I");
    maxDescriptorSetUniformBuffersDynamicField = env->GetFieldID(clazz,
                                                                 "maxDescriptorSetUniformBuffersDynamic",
                                                                 "I");
    maxDescriptorSetStorageBuffersField = env->GetFieldID(clazz, "maxDescriptorSetStorageBuffers",
                                                          "I");
    maxDescriptorSetStorageBuffersDynamicField = env->GetFieldID(clazz,
                                                                 "maxDescriptorSetStorageBuffersDynamic",
                                                                 "I");
    maxDescriptorSetSampledImagesField = env->GetFieldID(clazz, "maxDescriptorSetSampledImages",
                                                         "I");
    maxDescriptorSetStorageImagesField = env->GetFieldID(clazz, "maxDescriptorSetStorageImages",
                                                         "I");
    maxDescriptorSetInputAttachmentsField = env->GetFieldID(clazz,
                                                            "maxDescriptorSetInputAttachments",
                                                            "I");
    maxVertexInputAttributesField = env->GetFieldID(clazz, "maxVertexInputAttributes", "I");
    maxVertexInputBindingsField = env->GetFieldID(clazz, "maxVertexInputBindings", "I");
    maxVertexInputAttributeOffsetField = env->GetFieldID(clazz, "maxVertexInputAttributeOffset",
                                                         "I");
    maxVertexInputBindingStrideField = env->GetFieldID(clazz, "maxVertexInputBindingStride", "I");
    maxVertexOutputComponentsField = env->GetFieldID(clazz, "maxVertexOutputComponents", "I");
    maxTessellationGenerationLevelField = env->GetFieldID(clazz, "maxTessellationGenerationLevel",
                                                          "I");
    maxTessellationPatchSizeField = env->GetFieldID(clazz, "maxTessellationPatchSize", "I");
    maxTessellationControlPerVertexInputComponentsField = env->GetFieldID(clazz,
                                                                          "maxTessellationControlPerVertexInputComponents",
                                                                          "I");
    maxTessellationControlPerVertexOutputComponentsField = env->GetFieldID(clazz,
                                                                           "maxTessellationControlPerVertexOutputComponents",
                                                                           "I");
    maxTessellationControlPerPatchOutputComponentsField = env->GetFieldID(clazz,
                                                                          "maxTessellationControlPerPatchOutputComponents",
                                                                          "I");
    maxTessellationControlTotalOutputComponentsField = env->GetFieldID(clazz,
                                                                       "maxTessellationControlTotalOutputComponents",
                                                                       "I");
    maxTessellationEvaluationInputComponentsField = env->GetFieldID(clazz,
                                                                    "maxTessellationEvaluationInputComponents",
                                                                    "I");
    maxTessellationEvaluationOutputComponentsField = env->GetFieldID(clazz,
                                                                     "maxTessellationEvaluationOutputComponents",
                                                                     "I");
    maxGeometryShaderInvocationsField = env->GetFieldID(clazz, "maxGeometryShaderInvocations", "I");
    maxGeometryInputComponentsField = env->GetFieldID(clazz, "maxGeometryInputComponents", "I");
    maxGeometryOutputComponentsField = env->GetFieldID(clazz, "maxGeometryOutputComponents", "I");
    maxGeometryOutputVerticesField = env->GetFieldID(clazz, "maxGeometryOutputVertices", "I");
    maxGeometryTotalOutputComponentsField = env->GetFieldID(clazz,
                                                            "maxGeometryTotalOutputComponents",
                                                            "I");
    maxFragmentInputComponentsField = env->GetFieldID(clazz, "maxFragmentInputComponents", "I");
    maxFragmentOutputAttachmentsField = env->GetFieldID(clazz, "maxFragmentOutputAttachments", "I");
    maxFragmentDualSrcAttachmentsField = env->GetFieldID(clazz, "maxFragmentDualSrcAttachments",
                                                         "I");
    maxFragmentCombinedOutputResourcesField = env->GetFieldID(clazz,
                                                              "maxFragmentCombinedOutputResources",
                                                              "I");
    maxComputeSharedMemorySizeField = env->GetFieldID(clazz, "maxComputeSharedMemorySize", "I");
    maxComputeWorkGroupCountField = env->GetFieldID(clazz, "maxComputeWorkGroupCount", "[I");
    maxComputeWorkGroupInvocationsField = env->GetFieldID(clazz, "maxComputeWorkGroupInvocations",
                                                          "I");
    maxComputeWorkGroupSizeField = env->GetFieldID(clazz, "maxComputeWorkGroupSize", "[I");
    subPixelPrecisionBitsField = env->GetFieldID(clazz, "subPixelPrecisionBits", "I");
    subTexelPrecisionBitsField = env->GetFieldID(clazz, "subTexelPrecisionBits", "I");
    mipmapPrecisionBitsField = env->GetFieldID(clazz, "mipmapPrecisionBits", "I");
    maxDrawIndexedIndexValueField = env->GetFieldID(clazz, "maxDrawIndexedIndexValue", "I");
    maxDrawIndirectCountField = env->GetFieldID(clazz, "maxDrawIndirectCount", "I");
    maxSamplerLodBiasField = env->GetFieldID(clazz, "maxSamplerLodBias", "F");
    maxSamplerAnisotropyField = env->GetFieldID(clazz, "maxSamplerAnisotropy", "F");
    maxViewportsField = env->GetFieldID(clazz, "maxViewports", "I");
    maxViewportDimensionsField = env->GetFieldID(clazz, "maxViewportDimensions", "[I");
    viewportBoundsRangeField = env->GetFieldID(clazz, "viewportBoundsRange", "[F");
    viewportSubPixelBitsField = env->GetFieldID(clazz, "viewportSubPixelBits", "I");
    minMemoryMapAlignmentField = env->GetFieldID(clazz, "minMemoryMapAlignment", "J");
    minTexelBufferOffsetAlignmentField = env->GetFieldID(clazz, "minTexelBufferOffsetAlignment",
                                                         "J");
    minUniformBufferOffsetAlignmentField = env->GetFieldID(clazz, "minUniformBufferOffsetAlignment",
                                                           "J");
    minStorageBufferOffsetAlignmentField = env->GetFieldID(clazz, "minStorageBufferOffsetAlignment",
                                                           "J");
    minTexelOffsetField = env->GetFieldID(clazz, "minTexelOffset", "I");
    maxTexelOffsetField = env->GetFieldID(clazz, "maxTexelOffset", "I");
    minTexelGatherOffsetField = env->GetFieldID(clazz, "minTexelGatherOffset", "I");
    maxTexelGatherOffsetField = env->GetFieldID(clazz, "maxTexelGatherOffset", "I");
    minInterpolationOffsetField = env->GetFieldID(clazz, "minInterpolationOffset", "F");
    maxInterpolationOffsetField = env->GetFieldID(clazz, "maxInterpolationOffset", "F");
    subPixelInterpolationOffsetBitsField = env->GetFieldID(clazz, "subPixelInterpolationOffsetBits",
                                                           "I");
    maxFramebufferWidthField = env->GetFieldID(clazz, "maxFramebufferWidth", "I");
    maxFramebufferHeightField = env->GetFieldID(clazz, "maxFramebufferHeight", "I");
    maxFramebufferLayersField = env->GetFieldID(clazz, "maxFramebufferLayers", "I");
    framebufferColorSampleCountsField = env->GetFieldID(clazz, "framebufferColorSampleCounts", "I");
    framebufferDepthSampleCountsField = env->GetFieldID(clazz, "framebufferDepthSampleCounts", "I");
    framebufferStencilSampleCountsField = env->GetFieldID(clazz, "framebufferStencilSampleCounts",
                                                          "I");
    framebufferNoAttachmentsSampleCountsField = env->GetFieldID(clazz,
                                                                "framebufferNoAttachmentsSampleCounts",
                                                                "I");
    maxColorAttachmentsField = env->GetFieldID(clazz, "maxColorAttachments", "I");
    sampledImageColorSampleCountsField = env->GetFieldID(clazz, "sampledImageColorSampleCounts",
                                                         "I");
    sampledImageIntegerSampleCountsField = env->GetFieldID(clazz, "sampledImageIntegerSampleCounts",
                                                           "I");
    sampledImageDepthSampleCountsField = env->GetFieldID(clazz, "sampledImageDepthSampleCounts",
                                                         "I");
    sampledImageStencilSampleCountsField = env->GetFieldID(clazz, "sampledImageStencilSampleCounts",
                                                           "I");
    storageImageSampleCountsField = env->GetFieldID(clazz, "storageImageSampleCounts", "I");
    maxSampleMaskWordsField = env->GetFieldID(clazz, "maxSampleMaskWords", "I");
    timestampComputeAndGraphicsField = env->GetFieldID(clazz, "timestampComputeAndGraphics", "Z");
    timestampPeriodField = env->GetFieldID(clazz, "timestampPeriod", "F");
    maxClipDistancesField = env->GetFieldID(clazz, "maxClipDistances", "I");
    maxCullDistancesField = env->GetFieldID(clazz, "maxCullDistances", "I");
    maxCombinedClipAndCullDistancesField = env->GetFieldID(clazz, "maxCombinedClipAndCullDistances",
                                                           "I");
    discreteQueuePrioritiesField = env->GetFieldID(clazz, "discreteQueuePriorities", "I");
    pointSizeRangeField = env->GetFieldID(clazz, "pointSizeRange", "[F");
    lineWidthRangeField = env->GetFieldID(clazz, "lineWidthRange", "[F");
    pointSizeGranularityField = env->GetFieldID(clazz, "pointSizeGranularity", "F");
    lineWidthGranularityField = env->GetFieldID(clazz, "lineWidthGranularity", "F");
    strictLinesField = env->GetFieldID(clazz, "strictLines", "Z");
    standardSampleLocationsField = env->GetFieldID(clazz, "standardSampleLocations", "Z");
    optimalBufferCopyOffsetAlignmentField = env->GetFieldID(clazz,
                                                            "optimalBufferCopyOffsetAlignment",
                                                            "J");
    optimalBufferCopyRowPitchAlignmentField = env->GetFieldID(clazz,
                                                              "optimalBufferCopyRowPitchAlignment",
                                                              "J");
    nonCoherentAtomSizeField = env->GetFieldID(clazz, "nonCoherentAtomSize", "J");
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetUniformBuffers() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxDescriptorSetUniformBuffersField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::gettimestampPeriod() {
    return (float) (jfloat) env->GetFloatField(obj, timestampPeriodField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getmaxComputeWorkGroupCount(VkPhysicalDeviceLimits &clazzInfo) {
    auto maxComputeWorkGroupCountArray = (jintArray) env->GetObjectField(obj,
                                                                         maxComputeWorkGroupCountField);
    if (maxComputeWorkGroupCountArray == nullptr) {
        // const array
        // clazzInfo.maxComputeWorkGroupCount = nullptr;
        env->DeleteLocalRef(maxComputeWorkGroupCountArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(maxComputeWorkGroupCountArray);
    // primitive array?
    std::vector<uint32_t> maxComputeWorkGroupCount(size);
    env->GetIntArrayRegion(maxComputeWorkGroupCountArray, 0, size,
                           reinterpret_cast<jint *>(maxComputeWorkGroupCount.data()));
    // processing array data
    std::copy(maxComputeWorkGroupCount.begin(), maxComputeWorkGroupCount.end(),
              clazzInfo.maxComputeWorkGroupCount); // fixed array size
    env->DeleteLocalRef(maxComputeWorkGroupCountArray); // release reference
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxCullDistances() {
    return (uint32_t) (jint) env->GetIntField(obj, maxCullDistancesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFragmentInputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFragmentInputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsubPixelPrecisionBits() {
    return (uint32_t) (jint) env->GetIntField(obj, subPixelPrecisionBitsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorStorageBuffers() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxPerStageDescriptorStorageBuffersField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxSamplerAllocationCount() {
    return (uint32_t) (jint) env->GetIntField(obj, maxSamplerAllocationCountField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxGeometryTotalOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxGeometryTotalOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFragmentDualSrcAttachments() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFragmentDualSrcAttachmentsField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getbufferImageGranularity() {
    return (uint64_t) (jlong) env->GetLongField(obj, bufferImageGranularityField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsampledImageDepthSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj, sampledImageDepthSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetInputAttachments() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxDescriptorSetInputAttachmentsField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getviewportBoundsRange(VkPhysicalDeviceLimits &clazzInfo) {
    auto viewportBoundsRangeArray = (jfloatArray) env->GetObjectField(obj,
                                                                      viewportBoundsRangeField);
    if (viewportBoundsRangeArray == nullptr) {
        // const array
        // clazzInfo.viewportBoundsRange = nullptr;
        env->DeleteLocalRef(viewportBoundsRangeArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(viewportBoundsRangeArray);
    // primitive array?
    std::vector<float> viewportBoundsRange(size);
    env->GetFloatArrayRegion(viewportBoundsRangeArray, 0, size,
                             reinterpret_cast<jfloat *>(viewportBoundsRange.data()));
    // processing array data
    std::copy(viewportBoundsRange.begin(), viewportBoundsRange.end(),
              clazzInfo.viewportBoundsRange); // fixed array size
    env->DeleteLocalRef(viewportBoundsRangeArray); // release reference
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getminStorageBufferOffsetAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj,
                                                minStorageBufferOffsetAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getframebufferDepthSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj, framebufferDepthSampleCountsField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getnonCoherentAtomSize() {
    return (uint64_t) (jlong) env->GetLongField(obj, nonCoherentAtomSizeField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::fromObject(VkPhysicalDeviceLimits &clazzInfo) {
    clazzInfo.maxImageDimension1D = getmaxImageDimension1D(); // Primitive uint32_t
    clazzInfo.maxImageDimension2D = getmaxImageDimension2D(); // Primitive uint32_t
    clazzInfo.maxImageDimension3D = getmaxImageDimension3D(); // Primitive uint32_t
    clazzInfo.maxImageDimensionCube = getmaxImageDimensionCube(); // Primitive uint32_t
    clazzInfo.maxImageArrayLayers = getmaxImageArrayLayers(); // Primitive uint32_t
    clazzInfo.maxTexelBufferElements = getmaxTexelBufferElements(); // Primitive uint32_t
    clazzInfo.maxUniformBufferRange = getmaxUniformBufferRange(); // Primitive uint32_t
    clazzInfo.maxStorageBufferRange = getmaxStorageBufferRange(); // Primitive uint32_t
    clazzInfo.maxPushConstantsSize = getmaxPushConstantsSize(); // Primitive uint32_t
    clazzInfo.maxMemoryAllocationCount = getmaxMemoryAllocationCount(); // Primitive uint32_t
    clazzInfo.maxSamplerAllocationCount = getmaxSamplerAllocationCount(); // Primitive uint32_t
    clazzInfo.bufferImageGranularity = getbufferImageGranularity(); // Primitive uint64_t
    clazzInfo.sparseAddressSpaceSize = getsparseAddressSpaceSize(); // Primitive uint64_t
    clazzInfo.maxBoundDescriptorSets = getmaxBoundDescriptorSets(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorSamplers = getmaxPerStageDescriptorSamplers(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorUniformBuffers = getmaxPerStageDescriptorUniformBuffers(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorStorageBuffers = getmaxPerStageDescriptorStorageBuffers(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorSampledImages = getmaxPerStageDescriptorSampledImages(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorStorageImages = getmaxPerStageDescriptorStorageImages(); // Primitive uint32_t
    clazzInfo.maxPerStageDescriptorInputAttachments = getmaxPerStageDescriptorInputAttachments(); // Primitive uint32_t
    clazzInfo.maxPerStageResources = getmaxPerStageResources(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetSamplers = getmaxDescriptorSetSamplers(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetUniformBuffers = getmaxDescriptorSetUniformBuffers(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetUniformBuffersDynamic = getmaxDescriptorSetUniformBuffersDynamic(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetStorageBuffers = getmaxDescriptorSetStorageBuffers(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetStorageBuffersDynamic = getmaxDescriptorSetStorageBuffersDynamic(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetSampledImages = getmaxDescriptorSetSampledImages(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetStorageImages = getmaxDescriptorSetStorageImages(); // Primitive uint32_t
    clazzInfo.maxDescriptorSetInputAttachments = getmaxDescriptorSetInputAttachments(); // Primitive uint32_t
    clazzInfo.maxVertexInputAttributes = getmaxVertexInputAttributes(); // Primitive uint32_t
    clazzInfo.maxVertexInputBindings = getmaxVertexInputBindings(); // Primitive uint32_t
    clazzInfo.maxVertexInputAttributeOffset = getmaxVertexInputAttributeOffset(); // Primitive uint32_t
    clazzInfo.maxVertexInputBindingStride = getmaxVertexInputBindingStride(); // Primitive uint32_t
    clazzInfo.maxVertexOutputComponents = getmaxVertexOutputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationGenerationLevel = getmaxTessellationGenerationLevel(); // Primitive uint32_t
    clazzInfo.maxTessellationPatchSize = getmaxTessellationPatchSize(); // Primitive uint32_t
    clazzInfo.maxTessellationControlPerVertexInputComponents = getmaxTessellationControlPerVertexInputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationControlPerVertexOutputComponents = getmaxTessellationControlPerVertexOutputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationControlPerPatchOutputComponents = getmaxTessellationControlPerPatchOutputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationControlTotalOutputComponents = getmaxTessellationControlTotalOutputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationEvaluationInputComponents = getmaxTessellationEvaluationInputComponents(); // Primitive uint32_t
    clazzInfo.maxTessellationEvaluationOutputComponents = getmaxTessellationEvaluationOutputComponents(); // Primitive uint32_t
    clazzInfo.maxGeometryShaderInvocations = getmaxGeometryShaderInvocations(); // Primitive uint32_t
    clazzInfo.maxGeometryInputComponents = getmaxGeometryInputComponents(); // Primitive uint32_t
    clazzInfo.maxGeometryOutputComponents = getmaxGeometryOutputComponents(); // Primitive uint32_t
    clazzInfo.maxGeometryOutputVertices = getmaxGeometryOutputVertices(); // Primitive uint32_t
    clazzInfo.maxGeometryTotalOutputComponents = getmaxGeometryTotalOutputComponents(); // Primitive uint32_t
    clazzInfo.maxFragmentInputComponents = getmaxFragmentInputComponents(); // Primitive uint32_t
    clazzInfo.maxFragmentOutputAttachments = getmaxFragmentOutputAttachments(); // Primitive uint32_t
    clazzInfo.maxFragmentDualSrcAttachments = getmaxFragmentDualSrcAttachments(); // Primitive uint32_t
    clazzInfo.maxFragmentCombinedOutputResources = getmaxFragmentCombinedOutputResources(); // Primitive uint32_t
    clazzInfo.maxComputeSharedMemorySize = getmaxComputeSharedMemorySize(); // Primitive uint32_t
    getmaxComputeWorkGroupCount(clazzInfo);  // int Object Array
    clazzInfo.maxComputeWorkGroupInvocations = getmaxComputeWorkGroupInvocations(); // Primitive uint32_t
    getmaxComputeWorkGroupSize(clazzInfo);  // int Object Array
    clazzInfo.subPixelPrecisionBits = getsubPixelPrecisionBits(); // Primitive uint32_t
    clazzInfo.subTexelPrecisionBits = getsubTexelPrecisionBits(); // Primitive uint32_t
    clazzInfo.mipmapPrecisionBits = getmipmapPrecisionBits(); // Primitive uint32_t
    clazzInfo.maxDrawIndexedIndexValue = getmaxDrawIndexedIndexValue(); // Primitive uint32_t
    clazzInfo.maxDrawIndirectCount = getmaxDrawIndirectCount(); // Primitive uint32_t
    clazzInfo.maxSamplerLodBias = getmaxSamplerLodBias(); // Primitive float
    clazzInfo.maxSamplerAnisotropy = getmaxSamplerAnisotropy(); // Primitive float
    clazzInfo.maxViewports = getmaxViewports(); // Primitive uint32_t
    getmaxViewportDimensions(clazzInfo);  // int Object Array
    getviewportBoundsRange(clazzInfo);  // float Object Array
    clazzInfo.viewportSubPixelBits = getviewportSubPixelBits(); // Primitive uint32_t
    clazzInfo.minMemoryMapAlignment = getminMemoryMapAlignment(); // Primitive uint64_t
    clazzInfo.minTexelBufferOffsetAlignment = getminTexelBufferOffsetAlignment(); // Primitive uint64_t
    clazzInfo.minUniformBufferOffsetAlignment = getminUniformBufferOffsetAlignment(); // Primitive uint64_t
    clazzInfo.minStorageBufferOffsetAlignment = getminStorageBufferOffsetAlignment(); // Primitive uint64_t
    clazzInfo.minTexelOffset = getminTexelOffset(); // Primitive uint32_t
    clazzInfo.maxTexelOffset = getmaxTexelOffset(); // Primitive uint32_t
    clazzInfo.minTexelGatherOffset = getminTexelGatherOffset(); // Primitive uint32_t
    clazzInfo.maxTexelGatherOffset = getmaxTexelGatherOffset(); // Primitive uint32_t
    clazzInfo.minInterpolationOffset = getminInterpolationOffset(); // Primitive float
    clazzInfo.maxInterpolationOffset = getmaxInterpolationOffset(); // Primitive float
    clazzInfo.subPixelInterpolationOffsetBits = getsubPixelInterpolationOffsetBits(); // Primitive uint32_t
    clazzInfo.maxFramebufferWidth = getmaxFramebufferWidth(); // Primitive uint32_t
    clazzInfo.maxFramebufferHeight = getmaxFramebufferHeight(); // Primitive uint32_t
    clazzInfo.maxFramebufferLayers = getmaxFramebufferLayers(); // Primitive uint32_t
    clazzInfo.framebufferColorSampleCounts = getframebufferColorSampleCounts(); // Primitive uint32_t
    clazzInfo.framebufferDepthSampleCounts = getframebufferDepthSampleCounts(); // Primitive uint32_t
    clazzInfo.framebufferStencilSampleCounts = getframebufferStencilSampleCounts(); // Primitive uint32_t
    clazzInfo.framebufferNoAttachmentsSampleCounts = getframebufferNoAttachmentsSampleCounts(); // Primitive uint32_t
    clazzInfo.maxColorAttachments = getmaxColorAttachments(); // Primitive uint32_t
    clazzInfo.sampledImageColorSampleCounts = getsampledImageColorSampleCounts(); // Primitive uint32_t
    clazzInfo.sampledImageIntegerSampleCounts = getsampledImageIntegerSampleCounts(); // Primitive uint32_t
    clazzInfo.sampledImageDepthSampleCounts = getsampledImageDepthSampleCounts(); // Primitive uint32_t
    clazzInfo.sampledImageStencilSampleCounts = getsampledImageStencilSampleCounts(); // Primitive uint32_t
    clazzInfo.storageImageSampleCounts = getstorageImageSampleCounts(); // Primitive uint32_t
    clazzInfo.maxSampleMaskWords = getmaxSampleMaskWords(); // Primitive uint32_t
    clazzInfo.timestampComputeAndGraphics = gettimestampComputeAndGraphics(); // Primitive VkBool32
    clazzInfo.timestampPeriod = gettimestampPeriod(); // Primitive float
    clazzInfo.maxClipDistances = getmaxClipDistances(); // Primitive uint32_t
    clazzInfo.maxCullDistances = getmaxCullDistances(); // Primitive uint32_t
    clazzInfo.maxCombinedClipAndCullDistances = getmaxCombinedClipAndCullDistances(); // Primitive uint32_t
    clazzInfo.discreteQueuePriorities = getdiscreteQueuePriorities(); // Primitive uint32_t
    getpointSizeRange(clazzInfo);  // float Object Array
    getlineWidthRange(clazzInfo);  // float Object Array
    clazzInfo.pointSizeGranularity = getpointSizeGranularity(); // Primitive float
    clazzInfo.lineWidthGranularity = getlineWidthGranularity(); // Primitive float
    clazzInfo.strictLines = getstrictLines(); // Primitive VkBool32
    clazzInfo.standardSampleLocations = getstandardSampleLocations(); // Primitive VkBool32
    clazzInfo.optimalBufferCopyOffsetAlignment = getoptimalBufferCopyOffsetAlignment(); // Primitive uint64_t
    clazzInfo.optimalBufferCopyRowPitchAlignment = getoptimalBufferCopyRowPitchAlignment(); // Primitive uint64_t
    clazzInfo.nonCoherentAtomSize = getnonCoherentAtomSize(); // Primitive uint64_t
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFramebufferWidth() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFramebufferWidthField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxStorageBufferRange() {
    return (uint32_t) (jint) env->GetIntField(obj, maxStorageBufferRangeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorInputAttachments() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxPerStageDescriptorInputAttachmentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetStorageBuffersDynamic() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxDescriptorSetStorageBuffersDynamicField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::getminInterpolationOffset() {
    return (float) (jfloat) env->GetFloatField(obj, minInterpolationOffsetField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getminUniformBufferOffsetAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj,
                                                minUniformBufferOffsetAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorSamplers() {
    return (uint32_t) (jint) env->GetIntField(obj, maxPerStageDescriptorSamplersField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageResources() {
    return (uint32_t) (jint) env->GetIntField(obj, maxPerStageResourcesField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getminTexelBufferOffsetAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj,
                                                minTexelBufferOffsetAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFramebufferHeight() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFramebufferHeightField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFramebufferLayers() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFramebufferLayersField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getlineWidthRange(VkPhysicalDeviceLimits &clazzInfo) {
    auto lineWidthRangeArray = (jfloatArray) env->GetObjectField(obj, lineWidthRangeField);
    if (lineWidthRangeArray == nullptr) {
        // const array
        // clazzInfo.lineWidthRange = nullptr;
        env->DeleteLocalRef(lineWidthRangeArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(lineWidthRangeArray);
    // primitive array?
    std::vector<float> lineWidthRange(size);
    env->GetFloatArrayRegion(lineWidthRangeArray, 0, size,
                             reinterpret_cast<jfloat *>(lineWidthRange.data()));
    // processing array data
    std::copy(lineWidthRange.begin(), lineWidthRange.end(),
              clazzInfo.lineWidthRange); // fixed array size
    env->DeleteLocalRef(lineWidthRangeArray); // release reference
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxVertexInputBindings() {
    return (uint32_t) (jint) env->GetIntField(obj, maxVertexInputBindingsField); // primitive
}

VkBool32
VkPhysicalDeviceLimitsAccessor::gettimestampComputeAndGraphics() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      timestampComputeAndGraphicsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxImageDimension2D() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageDimension2DField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsampledImageColorSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj, sampledImageColorSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxComputeWorkGroupInvocations() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxComputeWorkGroupInvocationsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsampledImageIntegerSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              sampledImageIntegerSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxImageDimensionCube() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageDimensionCubeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsampledImageStencilSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              sampledImageStencilSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxGeometryShaderInvocations() {
    return (uint32_t) (jint) env->GetIntField(obj, maxGeometryShaderInvocationsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getminTexelOffset() {
    return (uint32_t) (jint) env->GetIntField(obj, minTexelOffsetField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTexelGatherOffset() {
    return (uint32_t) (jint) env->GetIntField(obj, maxTexelGatherOffsetField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxClipDistances() {
    return (uint32_t) (jint) env->GetIntField(obj, maxClipDistancesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationControlTotalOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationControlTotalOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDrawIndexedIndexValue() {
    return (uint32_t) (jint) env->GetIntField(obj, maxDrawIndexedIndexValueField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationControlPerVertexInputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationControlPerVertexInputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFragmentOutputAttachments() {
    return (uint32_t) (jint) env->GetIntField(obj, maxFragmentOutputAttachmentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationEvaluationInputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationEvaluationInputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getdiscreteQueuePriorities() {
    return (uint32_t) (jint) env->GetIntField(obj, discreteQueuePrioritiesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getviewportSubPixelBits() {
    return (uint32_t) (jint) env->GetIntField(obj, viewportSubPixelBitsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsubPixelInterpolationOffsetBits() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              subPixelInterpolationOffsetBitsField); // primitive
}

VkBool32
VkPhysicalDeviceLimitsAccessor::getstrictLines() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, strictLinesField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getoptimalBufferCopyOffsetAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj,
                                                optimalBufferCopyOffsetAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxVertexInputBindingStride() {
    return (uint32_t) (jint) env->GetIntField(obj, maxVertexInputBindingStrideField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::getmaxInterpolationOffset() {
    return (float) (jfloat) env->GetFloatField(obj, maxInterpolationOffsetField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxSampleMaskWords() {
    return (uint32_t) (jint) env->GetIntField(obj, maxSampleMaskWordsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDrawIndirectCount() {
    return (uint32_t) (jint) env->GetIntField(obj, maxDrawIndirectCountField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxBoundDescriptorSets() {
    return (uint32_t) (jint) env->GetIntField(obj, maxBoundDescriptorSetsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxMemoryAllocationCount() {
    return (uint32_t) (jint) env->GetIntField(obj, maxMemoryAllocationCountField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxGeometryOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj, maxGeometryOutputComponentsField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getmaxComputeWorkGroupSize(VkPhysicalDeviceLimits &clazzInfo) {
    auto maxComputeWorkGroupSizeArray = (jintArray) env->GetObjectField(obj,
                                                                        maxComputeWorkGroupSizeField);
    if (maxComputeWorkGroupSizeArray == nullptr) {
        // const array
        // clazzInfo.maxComputeWorkGroupSize = nullptr;
        env->DeleteLocalRef(maxComputeWorkGroupSizeArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(maxComputeWorkGroupSizeArray);
    // primitive array?
    std::vector<uint32_t> maxComputeWorkGroupSize(size);
    env->GetIntArrayRegion(maxComputeWorkGroupSizeArray, 0, size,
                           reinterpret_cast<jint *>(maxComputeWorkGroupSize.data()));
    // processing array data
    std::copy(maxComputeWorkGroupSize.begin(), maxComputeWorkGroupSize.end(),
              clazzInfo.maxComputeWorkGroupSize); // fixed array size
    env->DeleteLocalRef(maxComputeWorkGroupSizeArray); // release reference
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetUniformBuffersDynamic() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxDescriptorSetUniformBuffersDynamicField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetSamplers() {
    return (uint32_t) (jint) env->GetIntField(obj, maxDescriptorSetSamplersField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::getmaxSamplerLodBias() {
    return (float) (jfloat) env->GetFloatField(obj, maxSamplerLodBiasField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxUniformBufferRange() {
    return (uint32_t) (jint) env->GetIntField(obj, maxUniformBufferRangeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxGeometryOutputVertices() {
    return (uint32_t) (jint) env->GetIntField(obj, maxGeometryOutputVerticesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorUniformBuffers() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxPerStageDescriptorUniformBuffersField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationPatchSize() {
    return (uint32_t) (jint) env->GetIntField(obj, maxTessellationPatchSizeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getminTexelGatherOffset() {
    return (uint32_t) (jint) env->GetIntField(obj, minTexelGatherOffsetField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::getmaxSamplerAnisotropy() {
    return (float) (jfloat) env->GetFloatField(obj, maxSamplerAnisotropyField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getsubTexelPrecisionBits() {
    return (uint32_t) (jint) env->GetIntField(obj, subTexelPrecisionBitsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetStorageImages() {
    return (uint32_t) (jint) env->GetIntField(obj, maxDescriptorSetStorageImagesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationControlPerVertexOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationControlPerVertexOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationEvaluationOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationEvaluationOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorSampledImages() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxPerStageDescriptorSampledImagesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationControlPerPatchOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationControlPerPatchOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetStorageBuffers() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxDescriptorSetStorageBuffersField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxGeometryInputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj, maxGeometryInputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getframebufferStencilSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              framebufferStencilSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxCombinedClipAndCullDistances() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxCombinedClipAndCullDistancesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getframebufferNoAttachmentsSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              framebufferNoAttachmentsSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxDescriptorSetSampledImages() {
    return (uint32_t) (jint) env->GetIntField(obj, maxDescriptorSetSampledImagesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxVertexInputAttributeOffset() {
    return (uint32_t) (jint) env->GetIntField(obj, maxVertexInputAttributeOffsetField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getoptimalBufferCopyRowPitchAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj,
                                                optimalBufferCopyRowPitchAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxImageDimension3D() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageDimension3DField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTessellationGenerationLevel() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxTessellationGenerationLevelField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPushConstantsSize() {
    return (uint32_t) (jint) env->GetIntField(obj, maxPushConstantsSizeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxColorAttachments() {
    return (uint32_t) (jint) env->GetIntField(obj, maxColorAttachmentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxViewports() {
    return (uint32_t) (jint) env->GetIntField(obj, maxViewportsField); // primitive
}

float
VkPhysicalDeviceLimitsAccessor::getpointSizeGranularity() {
    return (float) (jfloat) env->GetFloatField(obj, pointSizeGranularityField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxComputeSharedMemorySize() {
    return (uint32_t) (jint) env->GetIntField(obj, maxComputeSharedMemorySizeField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getminMemoryMapAlignment() {
    return (uint64_t) (jlong) env->GetLongField(obj, minMemoryMapAlignmentField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxVertexOutputComponents() {
    return (uint32_t) (jint) env->GetIntField(obj, maxVertexOutputComponentsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxImageArrayLayers() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageArrayLayersField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getpointSizeRange(VkPhysicalDeviceLimits &clazzInfo) {
    auto pointSizeRangeArray = (jfloatArray) env->GetObjectField(obj, pointSizeRangeField);
    if (pointSizeRangeArray == nullptr) {
        // const array
        // clazzInfo.pointSizeRange = nullptr;
        env->DeleteLocalRef(pointSizeRangeArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(pointSizeRangeArray);
    // primitive array?
    std::vector<float> pointSizeRange(size);
    env->GetFloatArrayRegion(pointSizeRangeArray, 0, size,
                             reinterpret_cast<jfloat *>(pointSizeRange.data()));
    // processing array data
    std::copy(pointSizeRange.begin(), pointSizeRange.end(),
              clazzInfo.pointSizeRange); // fixed array size
    env->DeleteLocalRef(pointSizeRangeArray); // release reference
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getframebufferColorSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj, framebufferColorSampleCountsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxPerStageDescriptorStorageImages() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxPerStageDescriptorStorageImagesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmipmapPrecisionBits() {
    return (uint32_t) (jint) env->GetIntField(obj, mipmapPrecisionBitsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxImageDimension1D() {
    return (uint32_t) (jint) env->GetIntField(obj, maxImageDimension1DField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxFragmentCombinedOutputResources() {
    return (uint32_t) (jint) env->GetIntField(obj,
                                              maxFragmentCombinedOutputResourcesField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTexelOffset() {
    return (uint32_t) (jint) env->GetIntField(obj, maxTexelOffsetField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxVertexInputAttributes() {
    return (uint32_t) (jint) env->GetIntField(obj, maxVertexInputAttributesField); // primitive
}

void
VkPhysicalDeviceLimitsAccessor::getmaxViewportDimensions(VkPhysicalDeviceLimits &clazzInfo) {
    auto maxViewportDimensionsArray = (jintArray) env->GetObjectField(obj,
                                                                      maxViewportDimensionsField);
    if (maxViewportDimensionsArray == nullptr) {
        // const array
        // clazzInfo.maxViewportDimensions = nullptr;
        env->DeleteLocalRef(maxViewportDimensionsArray); // release null reference
        return;
    }
    auto size = env->GetArrayLength(maxViewportDimensionsArray);
    // primitive array?
    std::vector<uint32_t> maxViewportDimensions(size);
    env->GetIntArrayRegion(maxViewportDimensionsArray, 0, size,
                           reinterpret_cast<jint *>(maxViewportDimensions.data()));
    // processing array data
    std::copy(maxViewportDimensions.begin(), maxViewportDimensions.end(),
              clazzInfo.maxViewportDimensions); // fixed array size
    env->DeleteLocalRef(maxViewportDimensionsArray); // release reference
}

float
VkPhysicalDeviceLimitsAccessor::getlineWidthGranularity() {
    return (float) (jfloat) env->GetFloatField(obj, lineWidthGranularityField); // primitive
}

VkBool32
VkPhysicalDeviceLimitsAccessor::getstandardSampleLocations() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      standardSampleLocationsField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getmaxTexelBufferElements() {
    return (uint32_t) (jint) env->GetIntField(obj, maxTexelBufferElementsField); // primitive
}

uint64_t
VkPhysicalDeviceLimitsAccessor::getsparseAddressSpaceSize() {
    return (uint64_t) (jlong) env->GetLongField(obj, sparseAddressSpaceSizeField); // primitive
}

uint32_t
VkPhysicalDeviceLimitsAccessor::getstorageImageSampleCounts() {
    return (uint32_t) (jint) env->GetIntField(obj, storageImageSampleCountsField); // primitive
}

VkPhysicalDeviceLimitsAccessor::~VkPhysicalDeviceLimitsAccessor() {
    env->DeleteLocalRef(clazz);
}

