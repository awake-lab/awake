/*
 *  VkPhysicalDeviceFeaturesMutator.cpp
 *  Vulkan mutator for VkPhysicalDeviceFeatures
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceFeaturesMutator.h>

VkPhysicalDeviceFeaturesMutator::VkPhysicalDeviceFeaturesMutator(JNIEnv *env) : env(env) {
    this->env = env;
    clazz = env->FindClass(
            "io/github/ronjunevaldoz/awake/vulkan/models/physicaldevice/VkPhysicalDeviceFeatures");
    robustBufferAccessField = env->GetFieldID(clazz, "robustBufferAccess", "Z");
    fullDrawIndexUint32Field = env->GetFieldID(clazz, "fullDrawIndexUint32", "Z");
    imageCubeArrayField = env->GetFieldID(clazz, "imageCubeArray", "Z");
    independentBlendField = env->GetFieldID(clazz, "independentBlend", "Z");
    geometryShaderField = env->GetFieldID(clazz, "geometryShader", "Z");
    tessellationShaderField = env->GetFieldID(clazz, "tessellationShader", "Z");
    sampleRateShadingField = env->GetFieldID(clazz, "sampleRateShading", "Z");
    dualSrcBlendField = env->GetFieldID(clazz, "dualSrcBlend", "Z");
    logicOpField = env->GetFieldID(clazz, "logicOp", "Z");
    multiDrawIndirectField = env->GetFieldID(clazz, "multiDrawIndirect", "Z");
    drawIndirectFirstInstanceField = env->GetFieldID(clazz, "drawIndirectFirstInstance", "Z");
    depthClampField = env->GetFieldID(clazz, "depthClamp", "Z");
    depthBiasClampField = env->GetFieldID(clazz, "depthBiasClamp", "Z");
    fillModeNonSolidField = env->GetFieldID(clazz, "fillModeNonSolid", "Z");
    depthBoundsField = env->GetFieldID(clazz, "depthBounds", "Z");
    wideLinesField = env->GetFieldID(clazz, "wideLines", "Z");
    largePointsField = env->GetFieldID(clazz, "largePoints", "Z");
    alphaToOneField = env->GetFieldID(clazz, "alphaToOne", "Z");
    multiViewportField = env->GetFieldID(clazz, "multiViewport", "Z");
    samplerAnisotropyField = env->GetFieldID(clazz, "samplerAnisotropy", "Z");
    textureCompressionETC2Field = env->GetFieldID(clazz, "textureCompressionETC2", "Z");
    textureCompressionASTC_LDRField = env->GetFieldID(clazz, "textureCompressionASTC_LDR", "Z");
    textureCompressionBCField = env->GetFieldID(clazz, "textureCompressionBC", "Z");
    occlusionQueryPreciseField = env->GetFieldID(clazz, "occlusionQueryPrecise", "Z");
    pipelineStatisticsQueryField = env->GetFieldID(clazz, "pipelineStatisticsQuery", "Z");
    vertexPipelineStoresAndAtomicsField = env->GetFieldID(clazz, "vertexPipelineStoresAndAtomics",
                                                          "Z");
    fragmentStoresAndAtomicsField = env->GetFieldID(clazz, "fragmentStoresAndAtomics", "Z");
    shaderTessellationAndGeometryPointSizeField = env->GetFieldID(clazz,
                                                                  "shaderTessellationAndGeometryPointSize",
                                                                  "Z");
    shaderImageGatherExtendedField = env->GetFieldID(clazz, "shaderImageGatherExtended", "Z");
    shaderStorageImageExtendedFormatsField = env->GetFieldID(clazz,
                                                             "shaderStorageImageExtendedFormats",
                                                             "Z");
    shaderStorageImageMultisampleField = env->GetFieldID(clazz, "shaderStorageImageMultisample",
                                                         "Z");
    shaderStorageImageReadWithoutFormatField = env->GetFieldID(clazz,
                                                               "shaderStorageImageReadWithoutFormat",
                                                               "Z");
    shaderStorageImageWriteWithoutFormatField = env->GetFieldID(clazz,
                                                                "shaderStorageImageWriteWithoutFormat",
                                                                "Z");
    shaderUniformBufferArrayDynamicIndexingField = env->GetFieldID(clazz,
                                                                   "shaderUniformBufferArrayDynamicIndexing",
                                                                   "Z");
    shaderSampledImageArrayDynamicIndexingField = env->GetFieldID(clazz,
                                                                  "shaderSampledImageArrayDynamicIndexing",
                                                                  "Z");
    shaderStorageBufferArrayDynamicIndexingField = env->GetFieldID(clazz,
                                                                   "shaderStorageBufferArrayDynamicIndexing",
                                                                   "Z");
    shaderStorageImageArrayDynamicIndexingField = env->GetFieldID(clazz,
                                                                  "shaderStorageImageArrayDynamicIndexing",
                                                                  "Z");
    shaderClipDistanceField = env->GetFieldID(clazz, "shaderClipDistance", "Z");
    shaderCullDistanceField = env->GetFieldID(clazz, "shaderCullDistance", "Z");
    shaderFloat64Field = env->GetFieldID(clazz, "shaderFloat64", "Z");
    shaderInt64Field = env->GetFieldID(clazz, "shaderInt64", "Z");
    shaderInt16Field = env->GetFieldID(clazz, "shaderInt16", "Z");
    shaderResourceResidencyField = env->GetFieldID(clazz, "shaderResourceResidency", "Z");
    shaderResourceMinLodField = env->GetFieldID(clazz, "shaderResourceMinLod", "Z");
    sparseBindingField = env->GetFieldID(clazz, "sparseBinding", "Z");
    sparseResidencyBufferField = env->GetFieldID(clazz, "sparseResidencyBuffer", "Z");
    sparseResidencyImage2DField = env->GetFieldID(clazz, "sparseResidencyImage2D", "Z");
    sparseResidencyImage3DField = env->GetFieldID(clazz, "sparseResidencyImage3D", "Z");
    sparseResidency2SamplesField = env->GetFieldID(clazz, "sparseResidency2Samples", "Z");
    sparseResidency4SamplesField = env->GetFieldID(clazz, "sparseResidency4Samples", "Z");
    sparseResidency8SamplesField = env->GetFieldID(clazz, "sparseResidency8Samples", "Z");
    sparseResidency16SamplesField = env->GetFieldID(clazz, "sparseResidency16Samples", "Z");
    sparseResidencyAliasedField = env->GetFieldID(clazz, "sparseResidencyAliased", "Z");
    variableMultisampleRateField = env->GetFieldID(clazz, "variableMultisampleRate", "Z");
    inheritedQueriesField = env->GetFieldID(clazz, "inheritedQueries", "Z");
}

jobject
VkPhysicalDeviceFeaturesMutator::toObject(VkPhysicalDeviceFeatures source) {
    auto constructor = env->GetMethodID(clazz, "<init>", "()V");
    auto newObj = env->NewObject(clazz, constructor);
    env->SetBooleanField(newObj, robustBufferAccessField,
                         static_cast<jboolean>(source.robustBufferAccess));
    env->SetBooleanField(newObj, fullDrawIndexUint32Field,
                         static_cast<jboolean>(source.fullDrawIndexUint32));
    env->SetBooleanField(newObj, imageCubeArrayField, static_cast<jboolean>(source.imageCubeArray));
    env->SetBooleanField(newObj, independentBlendField,
                         static_cast<jboolean>(source.independentBlend));
    env->SetBooleanField(newObj, geometryShaderField, static_cast<jboolean>(source.geometryShader));
    env->SetBooleanField(newObj, tessellationShaderField,
                         static_cast<jboolean>(source.tessellationShader));
    env->SetBooleanField(newObj, sampleRateShadingField,
                         static_cast<jboolean>(source.sampleRateShading));
    env->SetBooleanField(newObj, dualSrcBlendField, static_cast<jboolean>(source.dualSrcBlend));
    env->SetBooleanField(newObj, logicOpField, static_cast<jboolean>(source.logicOp));
    env->SetBooleanField(newObj, multiDrawIndirectField,
                         static_cast<jboolean>(source.multiDrawIndirect));
    env->SetBooleanField(newObj, drawIndirectFirstInstanceField,
                         static_cast<jboolean>(source.drawIndirectFirstInstance));
    env->SetBooleanField(newObj, depthClampField, static_cast<jboolean>(source.depthClamp));
    env->SetBooleanField(newObj, depthBiasClampField, static_cast<jboolean>(source.depthBiasClamp));
    env->SetBooleanField(newObj, fillModeNonSolidField,
                         static_cast<jboolean>(source.fillModeNonSolid));
    env->SetBooleanField(newObj, depthBoundsField, static_cast<jboolean>(source.depthBounds));
    env->SetBooleanField(newObj, wideLinesField, static_cast<jboolean>(source.wideLines));
    env->SetBooleanField(newObj, largePointsField, static_cast<jboolean>(source.largePoints));
    env->SetBooleanField(newObj, alphaToOneField, static_cast<jboolean>(source.alphaToOne));
    env->SetBooleanField(newObj, multiViewportField, static_cast<jboolean>(source.multiViewport));
    env->SetBooleanField(newObj, samplerAnisotropyField,
                         static_cast<jboolean>(source.samplerAnisotropy));
    env->SetBooleanField(newObj, textureCompressionETC2Field,
                         static_cast<jboolean>(source.textureCompressionETC2));
    env->SetBooleanField(newObj, textureCompressionASTC_LDRField,
                         static_cast<jboolean>(source.textureCompressionASTC_LDR));
    env->SetBooleanField(newObj, textureCompressionBCField,
                         static_cast<jboolean>(source.textureCompressionBC));
    env->SetBooleanField(newObj, occlusionQueryPreciseField,
                         static_cast<jboolean>(source.occlusionQueryPrecise));
    env->SetBooleanField(newObj, pipelineStatisticsQueryField,
                         static_cast<jboolean>(source.pipelineStatisticsQuery));
    env->SetBooleanField(newObj, vertexPipelineStoresAndAtomicsField,
                         static_cast<jboolean>(source.vertexPipelineStoresAndAtomics));
    env->SetBooleanField(newObj, fragmentStoresAndAtomicsField,
                         static_cast<jboolean>(source.fragmentStoresAndAtomics));
    env->SetBooleanField(newObj, shaderTessellationAndGeometryPointSizeField,
                         static_cast<jboolean>(source.shaderTessellationAndGeometryPointSize));
    env->SetBooleanField(newObj, shaderImageGatherExtendedField,
                         static_cast<jboolean>(source.shaderImageGatherExtended));
    env->SetBooleanField(newObj, shaderStorageImageExtendedFormatsField,
                         static_cast<jboolean>(source.shaderStorageImageExtendedFormats));
    env->SetBooleanField(newObj, shaderStorageImageMultisampleField,
                         static_cast<jboolean>(source.shaderStorageImageMultisample));
    env->SetBooleanField(newObj, shaderStorageImageReadWithoutFormatField,
                         static_cast<jboolean>(source.shaderStorageImageReadWithoutFormat));
    env->SetBooleanField(newObj, shaderStorageImageWriteWithoutFormatField,
                         static_cast<jboolean>(source.shaderStorageImageWriteWithoutFormat));
    env->SetBooleanField(newObj, shaderUniformBufferArrayDynamicIndexingField,
                         static_cast<jboolean>(source.shaderUniformBufferArrayDynamicIndexing));
    env->SetBooleanField(newObj, shaderSampledImageArrayDynamicIndexingField,
                         static_cast<jboolean>(source.shaderSampledImageArrayDynamicIndexing));
    env->SetBooleanField(newObj, shaderStorageBufferArrayDynamicIndexingField,
                         static_cast<jboolean>(source.shaderStorageBufferArrayDynamicIndexing));
    env->SetBooleanField(newObj, shaderStorageImageArrayDynamicIndexingField,
                         static_cast<jboolean>(source.shaderStorageImageArrayDynamicIndexing));
    env->SetBooleanField(newObj, shaderClipDistanceField,
                         static_cast<jboolean>(source.shaderClipDistance));
    env->SetBooleanField(newObj, shaderCullDistanceField,
                         static_cast<jboolean>(source.shaderCullDistance));
    env->SetBooleanField(newObj, shaderFloat64Field, static_cast<jboolean>(source.shaderFloat64));
    env->SetBooleanField(newObj, shaderInt64Field, static_cast<jboolean>(source.shaderInt64));
    env->SetBooleanField(newObj, shaderInt16Field, static_cast<jboolean>(source.shaderInt16));
    env->SetBooleanField(newObj, shaderResourceResidencyField,
                         static_cast<jboolean>(source.shaderResourceResidency));
    env->SetBooleanField(newObj, shaderResourceMinLodField,
                         static_cast<jboolean>(source.shaderResourceMinLod));
    env->SetBooleanField(newObj, sparseBindingField, static_cast<jboolean>(source.sparseBinding));
    env->SetBooleanField(newObj, sparseResidencyBufferField,
                         static_cast<jboolean>(source.sparseResidencyBuffer));
    env->SetBooleanField(newObj, sparseResidencyImage2DField,
                         static_cast<jboolean>(source.sparseResidencyImage2D));
    env->SetBooleanField(newObj, sparseResidencyImage3DField,
                         static_cast<jboolean>(source.sparseResidencyImage3D));
    env->SetBooleanField(newObj, sparseResidency2SamplesField,
                         static_cast<jboolean>(source.sparseResidency2Samples));
    env->SetBooleanField(newObj, sparseResidency4SamplesField,
                         static_cast<jboolean>(source.sparseResidency4Samples));
    env->SetBooleanField(newObj, sparseResidency8SamplesField,
                         static_cast<jboolean>(source.sparseResidency8Samples));
    env->SetBooleanField(newObj, sparseResidency16SamplesField,
                         static_cast<jboolean>(source.sparseResidency16Samples));
    env->SetBooleanField(newObj, sparseResidencyAliasedField,
                         static_cast<jboolean>(source.sparseResidencyAliased));
    env->SetBooleanField(newObj, variableMultisampleRateField,
                         static_cast<jboolean>(source.variableMultisampleRate));
    env->SetBooleanField(newObj, inheritedQueriesField,
                         static_cast<jboolean>(source.inheritedQueries));
    return newObj;
}

VkPhysicalDeviceFeaturesMutator::~VkPhysicalDeviceFeaturesMutator() {
    // env->DeleteGlobalRef(clazz);
}

