/*
 *  VkPhysicalDeviceLimitsMutator.cpp
 *  Vulkan mutator for VkPhysicalDeviceLimits
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceLimitsMutator.h>

VkPhysicalDeviceLimitsMutator::VkPhysicalDeviceLimitsMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/physicaldevice/VkPhysicalDeviceLimits");
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

jobject
VkPhysicalDeviceLimitsMutator::toObject(VkPhysicalDeviceLimits source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetIntField(newObj, maxImageDimension1DField,
                     static_cast<jint>(source.maxImageDimension1D));
    env->SetIntField(newObj, maxImageDimension2DField,
                     static_cast<jint>(source.maxImageDimension2D));
    env->SetIntField(newObj, maxImageDimension3DField,
                     static_cast<jint>(source.maxImageDimension3D));
    env->SetIntField(newObj, maxImageDimensionCubeField,
                     static_cast<jint>(source.maxImageDimensionCube));
    env->SetIntField(newObj, maxImageArrayLayersField,
                     static_cast<jint>(source.maxImageArrayLayers));
    env->SetIntField(newObj, maxTexelBufferElementsField,
                     static_cast<jint>(source.maxTexelBufferElements));
    env->SetIntField(newObj, maxUniformBufferRangeField,
                     static_cast<jint>(source.maxUniformBufferRange));
    env->SetIntField(newObj, maxStorageBufferRangeField,
                     static_cast<jint>(source.maxStorageBufferRange));
    env->SetIntField(newObj, maxPushConstantsSizeField,
                     static_cast<jint>(source.maxPushConstantsSize));
    env->SetIntField(newObj, maxMemoryAllocationCountField,
                     static_cast<jint>(source.maxMemoryAllocationCount));
    env->SetIntField(newObj, maxSamplerAllocationCountField,
                     static_cast<jint>(source.maxSamplerAllocationCount));
    env->SetLongField(newObj, bufferImageGranularityField,
                      static_cast<jlong>(source.bufferImageGranularity));
    env->SetLongField(newObj, sparseAddressSpaceSizeField,
                      static_cast<jlong>(source.sparseAddressSpaceSize));
    env->SetIntField(newObj, maxBoundDescriptorSetsField,
                     static_cast<jint>(source.maxBoundDescriptorSets));
    env->SetIntField(newObj, maxPerStageDescriptorSamplersField,
                     static_cast<jint>(source.maxPerStageDescriptorSamplers));
    env->SetIntField(newObj, maxPerStageDescriptorUniformBuffersField,
                     static_cast<jint>(source.maxPerStageDescriptorUniformBuffers));
    env->SetIntField(newObj, maxPerStageDescriptorStorageBuffersField,
                     static_cast<jint>(source.maxPerStageDescriptorStorageBuffers));
    env->SetIntField(newObj, maxPerStageDescriptorSampledImagesField,
                     static_cast<jint>(source.maxPerStageDescriptorSampledImages));
    env->SetIntField(newObj, maxPerStageDescriptorStorageImagesField,
                     static_cast<jint>(source.maxPerStageDescriptorStorageImages));
    env->SetIntField(newObj, maxPerStageDescriptorInputAttachmentsField,
                     static_cast<jint>(source.maxPerStageDescriptorInputAttachments));
    env->SetIntField(newObj, maxPerStageResourcesField,
                     static_cast<jint>(source.maxPerStageResources));
    env->SetIntField(newObj, maxDescriptorSetSamplersField,
                     static_cast<jint>(source.maxDescriptorSetSamplers));
    env->SetIntField(newObj, maxDescriptorSetUniformBuffersField,
                     static_cast<jint>(source.maxDescriptorSetUniformBuffers));
    env->SetIntField(newObj, maxDescriptorSetUniformBuffersDynamicField,
                     static_cast<jint>(source.maxDescriptorSetUniformBuffersDynamic));
    env->SetIntField(newObj, maxDescriptorSetStorageBuffersField,
                     static_cast<jint>(source.maxDescriptorSetStorageBuffers));
    env->SetIntField(newObj, maxDescriptorSetStorageBuffersDynamicField,
                     static_cast<jint>(source.maxDescriptorSetStorageBuffersDynamic));
    env->SetIntField(newObj, maxDescriptorSetSampledImagesField,
                     static_cast<jint>(source.maxDescriptorSetSampledImages));
    env->SetIntField(newObj, maxDescriptorSetStorageImagesField,
                     static_cast<jint>(source.maxDescriptorSetStorageImages));
    env->SetIntField(newObj, maxDescriptorSetInputAttachmentsField,
                     static_cast<jint>(source.maxDescriptorSetInputAttachments));
    env->SetIntField(newObj, maxVertexInputAttributesField,
                     static_cast<jint>(source.maxVertexInputAttributes));
    env->SetIntField(newObj, maxVertexInputBindingsField,
                     static_cast<jint>(source.maxVertexInputBindings));
    env->SetIntField(newObj, maxVertexInputAttributeOffsetField,
                     static_cast<jint>(source.maxVertexInputAttributeOffset));
    env->SetIntField(newObj, maxVertexInputBindingStrideField,
                     static_cast<jint>(source.maxVertexInputBindingStride));
    env->SetIntField(newObj, maxVertexOutputComponentsField,
                     static_cast<jint>(source.maxVertexOutputComponents));
    env->SetIntField(newObj, maxTessellationGenerationLevelField,
                     static_cast<jint>(source.maxTessellationGenerationLevel));
    env->SetIntField(newObj, maxTessellationPatchSizeField,
                     static_cast<jint>(source.maxTessellationPatchSize));
    env->SetIntField(newObj, maxTessellationControlPerVertexInputComponentsField,
                     static_cast<jint>(source.maxTessellationControlPerVertexInputComponents));
    env->SetIntField(newObj, maxTessellationControlPerVertexOutputComponentsField,
                     static_cast<jint>(source.maxTessellationControlPerVertexOutputComponents));
    env->SetIntField(newObj, maxTessellationControlPerPatchOutputComponentsField,
                     static_cast<jint>(source.maxTessellationControlPerPatchOutputComponents));
    env->SetIntField(newObj, maxTessellationControlTotalOutputComponentsField,
                     static_cast<jint>(source.maxTessellationControlTotalOutputComponents));
    env->SetIntField(newObj, maxTessellationEvaluationInputComponentsField,
                     static_cast<jint>(source.maxTessellationEvaluationInputComponents));
    env->SetIntField(newObj, maxTessellationEvaluationOutputComponentsField,
                     static_cast<jint>(source.maxTessellationEvaluationOutputComponents));
    env->SetIntField(newObj, maxGeometryShaderInvocationsField,
                     static_cast<jint>(source.maxGeometryShaderInvocations));
    env->SetIntField(newObj, maxGeometryInputComponentsField,
                     static_cast<jint>(source.maxGeometryInputComponents));
    env->SetIntField(newObj, maxGeometryOutputComponentsField,
                     static_cast<jint>(source.maxGeometryOutputComponents));
    env->SetIntField(newObj, maxGeometryOutputVerticesField,
                     static_cast<jint>(source.maxGeometryOutputVertices));
    env->SetIntField(newObj, maxGeometryTotalOutputComponentsField,
                     static_cast<jint>(source.maxGeometryTotalOutputComponents));
    env->SetIntField(newObj, maxFragmentInputComponentsField,
                     static_cast<jint>(source.maxFragmentInputComponents));
    env->SetIntField(newObj, maxFragmentOutputAttachmentsField,
                     static_cast<jint>(source.maxFragmentOutputAttachments));
    env->SetIntField(newObj, maxFragmentDualSrcAttachmentsField,
                     static_cast<jint>(source.maxFragmentDualSrcAttachments));
    env->SetIntField(newObj, maxFragmentCombinedOutputResourcesField,
                     static_cast<jint>(source.maxFragmentCombinedOutputResources));
    env->SetIntField(newObj, maxComputeSharedMemorySizeField,
                     static_cast<jint>(source.maxComputeSharedMemorySize));
    // processing primitive array
    jintArray maxComputeWorkGroupCount = env->NewIntArray(3);
    env->SetIntArrayRegion(maxComputeWorkGroupCount, 0, 3,
                           reinterpret_cast<jint *>(source.maxComputeWorkGroupCount ));
    env->SetObjectField(newObj, maxComputeWorkGroupCountField, maxComputeWorkGroupCount);
    env->DeleteLocalRef(maxComputeWorkGroupCount);
    env->SetIntField(newObj, maxComputeWorkGroupInvocationsField,
                     static_cast<jint>(source.maxComputeWorkGroupInvocations));
    // processing primitive array
    jintArray maxComputeWorkGroupSize = env->NewIntArray(3);
    env->SetIntArrayRegion(maxComputeWorkGroupSize, 0, 3,
                           reinterpret_cast<jint *>(source.maxComputeWorkGroupSize ));
    env->SetObjectField(newObj, maxComputeWorkGroupSizeField, maxComputeWorkGroupSize);
    env->DeleteLocalRef(maxComputeWorkGroupSize);
    env->SetIntField(newObj, subPixelPrecisionBitsField,
                     static_cast<jint>(source.subPixelPrecisionBits));
    env->SetIntField(newObj, subTexelPrecisionBitsField,
                     static_cast<jint>(source.subTexelPrecisionBits));
    env->SetIntField(newObj, mipmapPrecisionBitsField,
                     static_cast<jint>(source.mipmapPrecisionBits));
    env->SetIntField(newObj, maxDrawIndexedIndexValueField,
                     static_cast<jint>(source.maxDrawIndexedIndexValue));
    env->SetIntField(newObj, maxDrawIndirectCountField,
                     static_cast<jint>(source.maxDrawIndirectCount));
    env->SetFloatField(newObj, maxSamplerLodBiasField,
                       static_cast<jfloat>(source.maxSamplerLodBias));
    env->SetFloatField(newObj, maxSamplerAnisotropyField,
                       static_cast<jfloat>(source.maxSamplerAnisotropy));
    env->SetIntField(newObj, maxViewportsField, static_cast<jint>(source.maxViewports));
    // processing primitive array
    jintArray maxViewportDimensions = env->NewIntArray(2);
    env->SetIntArrayRegion(maxViewportDimensions, 0, 2,
                           reinterpret_cast<jint *>(source.maxViewportDimensions ));
    env->SetObjectField(newObj, maxViewportDimensionsField, maxViewportDimensions);
    env->DeleteLocalRef(maxViewportDimensions);
    // processing primitive array
    jfloatArray viewportBoundsRange = env->NewFloatArray(2);
    env->SetFloatArrayRegion(viewportBoundsRange, 0, 2,
                             reinterpret_cast<jfloat *>(source.viewportBoundsRange ));
    env->SetObjectField(newObj, viewportBoundsRangeField, viewportBoundsRange);
    env->DeleteLocalRef(viewportBoundsRange);
    env->SetIntField(newObj, viewportSubPixelBitsField,
                     static_cast<jint>(source.viewportSubPixelBits));
    env->SetLongField(newObj, minMemoryMapAlignmentField,
                      static_cast<jlong>(source.minMemoryMapAlignment));
    env->SetLongField(newObj, minTexelBufferOffsetAlignmentField,
                      static_cast<jlong>(source.minTexelBufferOffsetAlignment));
    env->SetLongField(newObj, minUniformBufferOffsetAlignmentField,
                      static_cast<jlong>(source.minUniformBufferOffsetAlignment));
    env->SetLongField(newObj, minStorageBufferOffsetAlignmentField,
                      static_cast<jlong>(source.minStorageBufferOffsetAlignment));
    env->SetIntField(newObj, minTexelOffsetField, static_cast<jint>(source.minTexelOffset));
    env->SetIntField(newObj, maxTexelOffsetField, static_cast<jint>(source.maxTexelOffset));
    env->SetIntField(newObj, minTexelGatherOffsetField,
                     static_cast<jint>(source.minTexelGatherOffset));
    env->SetIntField(newObj, maxTexelGatherOffsetField,
                     static_cast<jint>(source.maxTexelGatherOffset));
    env->SetFloatField(newObj, minInterpolationOffsetField,
                       static_cast<jfloat>(source.minInterpolationOffset));
    env->SetFloatField(newObj, maxInterpolationOffsetField,
                       static_cast<jfloat>(source.maxInterpolationOffset));
    env->SetIntField(newObj, subPixelInterpolationOffsetBitsField,
                     static_cast<jint>(source.subPixelInterpolationOffsetBits));
    env->SetIntField(newObj, maxFramebufferWidthField,
                     static_cast<jint>(source.maxFramebufferWidth));
    env->SetIntField(newObj, maxFramebufferHeightField,
                     static_cast<jint>(source.maxFramebufferHeight));
    env->SetIntField(newObj, maxFramebufferLayersField,
                     static_cast<jint>(source.maxFramebufferLayers));
    env->SetIntField(newObj, framebufferColorSampleCountsField,
                     static_cast<jint>(source.framebufferColorSampleCounts));
    env->SetIntField(newObj, framebufferDepthSampleCountsField,
                     static_cast<jint>(source.framebufferDepthSampleCounts));
    env->SetIntField(newObj, framebufferStencilSampleCountsField,
                     static_cast<jint>(source.framebufferStencilSampleCounts));
    env->SetIntField(newObj, framebufferNoAttachmentsSampleCountsField,
                     static_cast<jint>(source.framebufferNoAttachmentsSampleCounts));
    env->SetIntField(newObj, maxColorAttachmentsField,
                     static_cast<jint>(source.maxColorAttachments));
    env->SetIntField(newObj, sampledImageColorSampleCountsField,
                     static_cast<jint>(source.sampledImageColorSampleCounts));
    env->SetIntField(newObj, sampledImageIntegerSampleCountsField,
                     static_cast<jint>(source.sampledImageIntegerSampleCounts));
    env->SetIntField(newObj, sampledImageDepthSampleCountsField,
                     static_cast<jint>(source.sampledImageDepthSampleCounts));
    env->SetIntField(newObj, sampledImageStencilSampleCountsField,
                     static_cast<jint>(source.sampledImageStencilSampleCounts));
    env->SetIntField(newObj, storageImageSampleCountsField,
                     static_cast<jint>(source.storageImageSampleCounts));
    env->SetIntField(newObj, maxSampleMaskWordsField, static_cast<jint>(source.maxSampleMaskWords));
    env->SetBooleanField(newObj, timestampComputeAndGraphicsField,
                         static_cast<jboolean>(source.timestampComputeAndGraphics));
    env->SetFloatField(newObj, timestampPeriodField, static_cast<jfloat>(source.timestampPeriod));
    env->SetIntField(newObj, maxClipDistancesField, static_cast<jint>(source.maxClipDistances));
    env->SetIntField(newObj, maxCullDistancesField, static_cast<jint>(source.maxCullDistances));
    env->SetIntField(newObj, maxCombinedClipAndCullDistancesField,
                     static_cast<jint>(source.maxCombinedClipAndCullDistances));
    env->SetIntField(newObj, discreteQueuePrioritiesField,
                     static_cast<jint>(source.discreteQueuePriorities));
    // processing primitive array
    jfloatArray pointSizeRange = env->NewFloatArray(2);
    env->SetFloatArrayRegion(pointSizeRange, 0, 2,
                             reinterpret_cast<jfloat *>(source.pointSizeRange ));
    env->SetObjectField(newObj, pointSizeRangeField, pointSizeRange);
    env->DeleteLocalRef(pointSizeRange);
    // processing primitive array
    jfloatArray lineWidthRange = env->NewFloatArray(2);
    env->SetFloatArrayRegion(lineWidthRange, 0, 2,
                             reinterpret_cast<jfloat *>(source.lineWidthRange ));
    env->SetObjectField(newObj, lineWidthRangeField, lineWidthRange);
    env->DeleteLocalRef(lineWidthRange);
    env->SetFloatField(newObj, pointSizeGranularityField,
                       static_cast<jfloat>(source.pointSizeGranularity));
    env->SetFloatField(newObj, lineWidthGranularityField,
                       static_cast<jfloat>(source.lineWidthGranularity));
    env->SetBooleanField(newObj, strictLinesField, static_cast<jboolean>(source.strictLines));
    env->SetBooleanField(newObj, standardSampleLocationsField,
                         static_cast<jboolean>(source.standardSampleLocations));
    env->SetLongField(newObj, optimalBufferCopyOffsetAlignmentField,
                      static_cast<jlong>(source.optimalBufferCopyOffsetAlignment));
    env->SetLongField(newObj, optimalBufferCopyRowPitchAlignmentField,
                      static_cast<jlong>(source.optimalBufferCopyRowPitchAlignment));
    env->SetLongField(newObj, nonCoherentAtomSizeField,
                      static_cast<jlong>(source.nonCoherentAtomSize));
    return newObj;
}

VkPhysicalDeviceLimitsMutator::~VkPhysicalDeviceLimitsMutator() {
    // env->DeleteGlobalRef(clazz);
}

