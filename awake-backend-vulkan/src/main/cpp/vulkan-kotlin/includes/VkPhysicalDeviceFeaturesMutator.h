/*
 *  VkPhysicalDeviceFeaturesMutator.h
 *  Vulkan mutator for VkPhysicalDeviceFeatures
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICEFEATURESMUTATOR_H
#define VKPHYSICALDEVICEFEATURESMUTATOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceFeaturesMutator {
private:
    JNIEnv *env;
    jclass clazz;
    jfieldID robustBufferAccessField;
    jfieldID fullDrawIndexUint32Field;
    jfieldID imageCubeArrayField;
    jfieldID independentBlendField;
    jfieldID geometryShaderField;
    jfieldID tessellationShaderField;
    jfieldID sampleRateShadingField;
    jfieldID dualSrcBlendField;
    jfieldID logicOpField;
    jfieldID multiDrawIndirectField;
    jfieldID drawIndirectFirstInstanceField;
    jfieldID depthClampField;
    jfieldID depthBiasClampField;
    jfieldID fillModeNonSolidField;
    jfieldID depthBoundsField;
    jfieldID wideLinesField;
    jfieldID largePointsField;
    jfieldID alphaToOneField;
    jfieldID multiViewportField;
    jfieldID samplerAnisotropyField;
    jfieldID textureCompressionETC2Field;
    jfieldID textureCompressionASTC_LDRField;
    jfieldID textureCompressionBCField;
    jfieldID occlusionQueryPreciseField;
    jfieldID pipelineStatisticsQueryField;
    jfieldID vertexPipelineStoresAndAtomicsField;
    jfieldID fragmentStoresAndAtomicsField;
    jfieldID shaderTessellationAndGeometryPointSizeField;
    jfieldID shaderImageGatherExtendedField;
    jfieldID shaderStorageImageExtendedFormatsField;
    jfieldID shaderStorageImageMultisampleField;
    jfieldID shaderStorageImageReadWithoutFormatField;
    jfieldID shaderStorageImageWriteWithoutFormatField;
    jfieldID shaderUniformBufferArrayDynamicIndexingField;
    jfieldID shaderSampledImageArrayDynamicIndexingField;
    jfieldID shaderStorageBufferArrayDynamicIndexingField;
    jfieldID shaderStorageImageArrayDynamicIndexingField;
    jfieldID shaderClipDistanceField;
    jfieldID shaderCullDistanceField;
    jfieldID shaderFloat64Field;
    jfieldID shaderInt64Field;
    jfieldID shaderInt16Field;
    jfieldID shaderResourceResidencyField;
    jfieldID shaderResourceMinLodField;
    jfieldID sparseBindingField;
    jfieldID sparseResidencyBufferField;
    jfieldID sparseResidencyImage2DField;
    jfieldID sparseResidencyImage3DField;
    jfieldID sparseResidency2SamplesField;
    jfieldID sparseResidency4SamplesField;
    jfieldID sparseResidency8SamplesField;
    jfieldID sparseResidency16SamplesField;
    jfieldID sparseResidencyAliasedField;
    jfieldID variableMultisampleRateField;
    jfieldID inheritedQueriesField;
public:
    VkPhysicalDeviceFeaturesMutator(JNIEnv *env);

    jobject toObject(VkPhysicalDeviceFeatures source);
    ~VkPhysicalDeviceFeaturesMutator();
};
#endif // VKPHYSICALDEVICEFEATURESMUTATOR_H