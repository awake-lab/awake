/*
 *  VkPhysicalDeviceLimitsMutator.h
 *  Vulkan mutator for VkPhysicalDeviceLimits
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICELIMITSMUTATOR_H
#define VKPHYSICALDEVICELIMITSMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceLimitsMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID maxImageDimension1DField;
    jfieldID maxImageDimension2DField;
    jfieldID maxImageDimension3DField;
    jfieldID maxImageDimensionCubeField;
    jfieldID maxImageArrayLayersField;
    jfieldID maxTexelBufferElementsField;
    jfieldID maxUniformBufferRangeField;
    jfieldID maxStorageBufferRangeField;
    jfieldID maxPushConstantsSizeField;
    jfieldID maxMemoryAllocationCountField;
    jfieldID maxSamplerAllocationCountField;
    jfieldID bufferImageGranularityField;
    jfieldID sparseAddressSpaceSizeField;
    jfieldID maxBoundDescriptorSetsField;
    jfieldID maxPerStageDescriptorSamplersField;
    jfieldID maxPerStageDescriptorUniformBuffersField;
    jfieldID maxPerStageDescriptorStorageBuffersField;
    jfieldID maxPerStageDescriptorSampledImagesField;
    jfieldID maxPerStageDescriptorStorageImagesField;
    jfieldID maxPerStageDescriptorInputAttachmentsField;
    jfieldID maxPerStageResourcesField;
    jfieldID maxDescriptorSetSamplersField;
    jfieldID maxDescriptorSetUniformBuffersField;
    jfieldID maxDescriptorSetUniformBuffersDynamicField;
    jfieldID maxDescriptorSetStorageBuffersField;
    jfieldID maxDescriptorSetStorageBuffersDynamicField;
    jfieldID maxDescriptorSetSampledImagesField;
    jfieldID maxDescriptorSetStorageImagesField;
    jfieldID maxDescriptorSetInputAttachmentsField;
    jfieldID maxVertexInputAttributesField;
    jfieldID maxVertexInputBindingsField;
    jfieldID maxVertexInputAttributeOffsetField;
    jfieldID maxVertexInputBindingStrideField;
    jfieldID maxVertexOutputComponentsField;
    jfieldID maxTessellationGenerationLevelField;
    jfieldID maxTessellationPatchSizeField;
    jfieldID maxTessellationControlPerVertexInputComponentsField;
    jfieldID maxTessellationControlPerVertexOutputComponentsField;
    jfieldID maxTessellationControlPerPatchOutputComponentsField;
    jfieldID maxTessellationControlTotalOutputComponentsField;
    jfieldID maxTessellationEvaluationInputComponentsField;
    jfieldID maxTessellationEvaluationOutputComponentsField;
    jfieldID maxGeometryShaderInvocationsField;
    jfieldID maxGeometryInputComponentsField;
    jfieldID maxGeometryOutputComponentsField;
    jfieldID maxGeometryOutputVerticesField;
    jfieldID maxGeometryTotalOutputComponentsField;
    jfieldID maxFragmentInputComponentsField;
    jfieldID maxFragmentOutputAttachmentsField;
    jfieldID maxFragmentDualSrcAttachmentsField;
    jfieldID maxFragmentCombinedOutputResourcesField;
    jfieldID maxComputeSharedMemorySizeField;
    jfieldID maxComputeWorkGroupCountField;
    jfieldID maxComputeWorkGroupInvocationsField;
    jfieldID maxComputeWorkGroupSizeField;
    jfieldID subPixelPrecisionBitsField;
    jfieldID subTexelPrecisionBitsField;
    jfieldID mipmapPrecisionBitsField;
    jfieldID maxDrawIndexedIndexValueField;
    jfieldID maxDrawIndirectCountField;
    jfieldID maxSamplerLodBiasField;
    jfieldID maxSamplerAnisotropyField;
    jfieldID maxViewportsField;
    jfieldID maxViewportDimensionsField;
    jfieldID viewportBoundsRangeField;
    jfieldID viewportSubPixelBitsField;
    jfieldID minMemoryMapAlignmentField;
    jfieldID minTexelBufferOffsetAlignmentField;
    jfieldID minUniformBufferOffsetAlignmentField;
    jfieldID minStorageBufferOffsetAlignmentField;
    jfieldID minTexelOffsetField;
    jfieldID maxTexelOffsetField;
    jfieldID minTexelGatherOffsetField;
    jfieldID maxTexelGatherOffsetField;
    jfieldID minInterpolationOffsetField;
    jfieldID maxInterpolationOffsetField;
    jfieldID subPixelInterpolationOffsetBitsField;
    jfieldID maxFramebufferWidthField;
    jfieldID maxFramebufferHeightField;
    jfieldID maxFramebufferLayersField;
    jfieldID framebufferColorSampleCountsField;
    jfieldID framebufferDepthSampleCountsField;
    jfieldID framebufferStencilSampleCountsField;
    jfieldID framebufferNoAttachmentsSampleCountsField;
    jfieldID maxColorAttachmentsField;
    jfieldID sampledImageColorSampleCountsField;
    jfieldID sampledImageIntegerSampleCountsField;
    jfieldID sampledImageDepthSampleCountsField;
    jfieldID sampledImageStencilSampleCountsField;
    jfieldID storageImageSampleCountsField;
    jfieldID maxSampleMaskWordsField;
    jfieldID timestampComputeAndGraphicsField;
    jfieldID timestampPeriodField;
    jfieldID maxClipDistancesField;
    jfieldID maxCullDistancesField;
    jfieldID maxCombinedClipAndCullDistancesField;
    jfieldID discreteQueuePrioritiesField;
    jfieldID pointSizeRangeField;
    jfieldID lineWidthRangeField;
    jfieldID pointSizeGranularityField;
    jfieldID lineWidthGranularityField;
    jfieldID strictLinesField;
    jfieldID standardSampleLocationsField;
    jfieldID optimalBufferCopyOffsetAlignmentField;
    jfieldID optimalBufferCopyRowPitchAlignmentField;
    jfieldID nonCoherentAtomSizeField;
public:
    VkPhysicalDeviceLimitsMutator(JNIEnv *env);

    jobject toObject(VkPhysicalDeviceLimits source);
    ~VkPhysicalDeviceLimitsMutator();
};
#endif // VKPHYSICALDEVICELIMITSMUTATOR_H