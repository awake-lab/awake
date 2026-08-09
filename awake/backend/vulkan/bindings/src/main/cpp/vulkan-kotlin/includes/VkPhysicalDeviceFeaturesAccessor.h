/*
 *  VkPhysicalDeviceFeaturesAccessor.h
 *  Vulkan accessor for VkPhysicalDeviceFeatures
 *  Created by Ron June Valdoz */

#ifndef VKPHYSICALDEVICEFEATURESACCESSOR_H
#define VKPHYSICALDEVICEFEATURESACCESSOR_H

#include <jni.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <enum_utils.h>

class VkPhysicalDeviceFeaturesAccessor {
private:
    JNIEnv *env;
    jobject obj;
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
    VkPhysicalDeviceFeaturesAccessor(JNIEnv *env, jobject obj);

    VkBool32 getdepthBiasClamp();
    VkBool32 getmultiDrawIndirect();
    VkBool32 getdrawIndirectFirstInstance();
    VkBool32 getimageCubeArray();
    VkBool32 getmultiViewport();
    VkBool32 getsparseResidencyBuffer();
    VkBool32 getsparseBinding();
    VkBool32 gettextureCompressionETC2();
    VkBool32 getshaderStorageImageMultisample();
    VkBool32 getfillModeNonSolid();
    VkBool32 getalphaToOne();
    VkBool32 getindependentBlend();
    VkBool32 getshaderClipDistance();
    VkBool32 getshaderFloat64();
    VkBool32 getsparseResidency16Samples();
    VkBool32 getwideLines();
    VkBool32 getshaderInt64();
    VkBool32 getsparseResidency8Samples();
    VkBool32 getdepthClamp();
    VkBool32 getsparseResidency2Samples();
    VkBool32 getsampleRateShading();
    VkBool32 getsamplerAnisotropy();
    VkBool32 getshaderStorageImageWriteWithoutFormat();
    VkBool32 getsparseResidencyImage2D();
    VkBool32 getvariableMultisampleRate();
    VkBool32 getocclusionQueryPrecise();
    VkBool32 getdualSrcBlend();
    VkBool32 getlargePoints();
    VkBool32 getdepthBounds();
    VkBool32 getshaderImageGatherExtended();
    VkBool32 getshaderSampledImageArrayDynamicIndexing();
    VkBool32 getshaderTessellationAndGeometryPointSize();
    VkBool32 getpipelineStatisticsQuery();

    VkBool32 getfragmentStoresAndAtomics();

    void fromObject(VkPhysicalDeviceFeatures &clazzInfo);

    VkBool32 getshaderUniformBufferArrayDynamicIndexing();
    VkBool32 getshaderStorageImageArrayDynamicIndexing();
    VkBool32 getshaderStorageImageReadWithoutFormat();
    VkBool32 getshaderResourceMinLod();
    VkBool32 gettextureCompressionASTC_LDR();
    VkBool32 getfullDrawIndexUint32();
    VkBool32 getvertexPipelineStoresAndAtomics();
    VkBool32 getshaderStorageBufferArrayDynamicIndexing();
    VkBool32 getshaderInt16();
    VkBool32 getsparseResidency4Samples();
    VkBool32 gettextureCompressionBC();
    VkBool32 getshaderResourceResidency();
    VkBool32 getsparseResidencyImage3D();
    VkBool32 getshaderCullDistance();
    VkBool32 getrobustBufferAccess();
    VkBool32 getlogicOp();
    VkBool32 getsparseResidencyAliased();
    VkBool32 getshaderStorageImageExtendedFormats();
    VkBool32 getgeometryShader();
    VkBool32 gettessellationShader();
    VkBool32 getinheritedQueries();
    ~VkPhysicalDeviceFeaturesAccessor();
};
#endif // VKPHYSICALDEVICEFEATURESACCESSOR_H