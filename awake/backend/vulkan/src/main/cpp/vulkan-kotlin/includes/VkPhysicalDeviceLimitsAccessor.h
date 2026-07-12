/*
 *  VkPhysicalDeviceLimitsAccessor.h
 *  Vulkan accessor for VkPhysicalDeviceLimits
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICELIMITSACCESSOR_H
#define VKPHYSICALDEVICELIMITSACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceLimitsAccessor {
private:
    JNIEnv *env;
    jobject obj;
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
    VkPhysicalDeviceLimitsAccessor(JNIEnv *env, jobject obj);

    uint32_t getmaxDescriptorSetUniformBuffers();

    float gettimestampPeriod();

    void getmaxComputeWorkGroupCount(VkPhysicalDeviceLimits &clazzInfo);

    uint32_t getmaxCullDistances();
    uint32_t getmaxFragmentInputComponents();
    uint32_t getsubPixelPrecisionBits();
    uint32_t getmaxPerStageDescriptorStorageBuffers();
    uint32_t getmaxSamplerAllocationCount();
    uint32_t getmaxGeometryTotalOutputComponents();
    uint32_t getmaxFragmentDualSrcAttachments();
    uint64_t getbufferImageGranularity();
    uint32_t getsampledImageDepthSampleCounts();

    uint32_t getmaxDescriptorSetInputAttachments();

    void getviewportBoundsRange(VkPhysicalDeviceLimits &clazzInfo);

    uint64_t getminStorageBufferOffsetAlignment();
    uint32_t getframebufferDepthSampleCounts();

    uint64_t getnonCoherentAtomSize();

    void fromObject(VkPhysicalDeviceLimits &clazzInfo);

    uint32_t getmaxFramebufferWidth();
    uint32_t getmaxStorageBufferRange();
    uint32_t getmaxPerStageDescriptorInputAttachments();
    uint32_t getmaxDescriptorSetStorageBuffersDynamic();
    float getminInterpolationOffset();
    uint64_t getminUniformBufferOffsetAlignment();
    uint32_t getmaxPerStageDescriptorSamplers();
    uint32_t getmaxPerStageResources();
    uint64_t getminTexelBufferOffsetAlignment();
    uint32_t getmaxFramebufferHeight();

    uint32_t getmaxFramebufferLayers();

    void getlineWidthRange(VkPhysicalDeviceLimits &clazzInfo);

    uint32_t getmaxVertexInputBindings();
    VkBool32 gettimestampComputeAndGraphics();
    uint32_t getmaxImageDimension2D();
    uint32_t getsampledImageColorSampleCounts();
    uint32_t getmaxComputeWorkGroupInvocations();
    uint32_t getsampledImageIntegerSampleCounts();
    uint32_t getmaxImageDimensionCube();
    uint32_t getsampledImageStencilSampleCounts();
    uint32_t getmaxGeometryShaderInvocations();
    uint32_t getminTexelOffset();
    uint32_t getmaxTexelGatherOffset();
    uint32_t getmaxClipDistances();
    uint32_t getmaxTessellationControlTotalOutputComponents();
    uint32_t getmaxDrawIndexedIndexValue();
    uint32_t getmaxTessellationControlPerVertexInputComponents();
    uint32_t getmaxFragmentOutputAttachments();
    uint32_t getmaxTessellationEvaluationInputComponents();
    uint32_t getdiscreteQueuePriorities();
    uint32_t getviewportSubPixelBits();
    uint32_t getsubPixelInterpolationOffsetBits();
    VkBool32 getstrictLines();
    uint64_t getoptimalBufferCopyOffsetAlignment();
    uint32_t getmaxVertexInputBindingStride();
    float getmaxInterpolationOffset();
    uint32_t getmaxSampleMaskWords();
    uint32_t getmaxDrawIndirectCount();
    uint32_t getmaxBoundDescriptorSets();
    uint32_t getmaxMemoryAllocationCount();

    uint32_t getmaxGeometryOutputComponents();

    void getmaxComputeWorkGroupSize(VkPhysicalDeviceLimits &clazzInfo);

    uint32_t getmaxDescriptorSetUniformBuffersDynamic();
    uint32_t getmaxDescriptorSetSamplers();
    float getmaxSamplerLodBias();
    uint32_t getmaxUniformBufferRange();
    uint32_t getmaxGeometryOutputVertices();
    uint32_t getmaxPerStageDescriptorUniformBuffers();
    uint32_t getmaxTessellationPatchSize();
    uint32_t getminTexelGatherOffset();
    float getmaxSamplerAnisotropy();
    uint32_t getsubTexelPrecisionBits();
    uint32_t getmaxDescriptorSetStorageImages();
    uint32_t getmaxTessellationControlPerVertexOutputComponents();
    uint32_t getmaxTessellationEvaluationOutputComponents();
    uint32_t getmaxPerStageDescriptorSampledImages();
    uint32_t getmaxTessellationControlPerPatchOutputComponents();
    uint32_t getmaxDescriptorSetStorageBuffers();
    uint32_t getmaxGeometryInputComponents();
    uint32_t getframebufferStencilSampleCounts();
    uint32_t getmaxCombinedClipAndCullDistances();
    uint32_t getframebufferNoAttachmentsSampleCounts();
    uint32_t getmaxDescriptorSetSampledImages();
    uint32_t getmaxVertexInputAttributeOffset();
    uint64_t getoptimalBufferCopyRowPitchAlignment();
    uint32_t getmaxImageDimension3D();
    uint32_t getmaxTessellationGenerationLevel();
    uint32_t getmaxPushConstantsSize();
    uint32_t getmaxColorAttachments();
    uint32_t getmaxViewports();
    float getpointSizeGranularity();
    uint32_t getmaxComputeSharedMemorySize();
    uint64_t getminMemoryMapAlignment();
    uint32_t getmaxVertexOutputComponents();

    uint32_t getmaxImageArrayLayers();

    void getpointSizeRange(VkPhysicalDeviceLimits &clazzInfo);

    uint32_t getframebufferColorSampleCounts();
    uint32_t getmaxPerStageDescriptorStorageImages();
    uint32_t getmipmapPrecisionBits();
    uint32_t getmaxImageDimension1D();
    uint32_t getmaxFragmentCombinedOutputResources();
    uint32_t getmaxTexelOffset();

    uint32_t getmaxVertexInputAttributes();

    void getmaxViewportDimensions(VkPhysicalDeviceLimits &clazzInfo);

    float getlineWidthGranularity();
    VkBool32 getstandardSampleLocations();
    uint32_t getmaxTexelBufferElements();
    uint64_t getsparseAddressSpaceSize();
    uint32_t getstorageImageSampleCounts();
    ~VkPhysicalDeviceLimitsAccessor();
};
#endif // VKPHYSICALDEVICELIMITSACCESSOR_H