/*
 *  VkPhysicalDeviceFeaturesAccessor.cpp
 *  Vulkan accessor for VkPhysicalDeviceFeatures
 *  Created by Ron June Valdoz */

#include <includes/VkPhysicalDeviceFeaturesAccessor.h>

VkPhysicalDeviceFeaturesAccessor::VkPhysicalDeviceFeaturesAccessor(JNIEnv *env, jobject obj) : env(
        env), obj(obj) {
    clazz = env->GetObjectClass(obj);
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

VkBool32
VkPhysicalDeviceFeaturesAccessor::getdepthBiasClamp() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, depthBiasClampField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getmultiDrawIndirect() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, multiDrawIndirectField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getdrawIndirectFirstInstance() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      drawIndirectFirstInstanceField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getimageCubeArray() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, imageCubeArrayField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getmultiViewport() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, multiViewportField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidencyBuffer() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, sparseResidencyBufferField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseBinding() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, sparseBindingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::gettextureCompressionETC2() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      textureCompressionETC2Field); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageImageMultisample() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageImageMultisampleField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getfillModeNonSolid() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, fillModeNonSolidField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getalphaToOne() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, alphaToOneField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getindependentBlend() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, independentBlendField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderClipDistance() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderClipDistanceField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderFloat64() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderFloat64Field); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidency16Samples() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidency16SamplesField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getwideLines() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, wideLinesField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderInt64() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderInt64Field); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidency8Samples() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidency8SamplesField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getdepthClamp() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, depthClampField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidency2Samples() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidency2SamplesField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsampleRateShading() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, sampleRateShadingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsamplerAnisotropy() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, samplerAnisotropyField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageImageWriteWithoutFormat() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageImageWriteWithoutFormatField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidencyImage2D() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidencyImage2DField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getvariableMultisampleRate() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      variableMultisampleRateField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getocclusionQueryPrecise() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, occlusionQueryPreciseField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getdualSrcBlend() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, dualSrcBlendField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getlargePoints() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, largePointsField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getdepthBounds() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, depthBoundsField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderImageGatherExtended() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderImageGatherExtendedField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderSampledImageArrayDynamicIndexing() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderSampledImageArrayDynamicIndexingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderTessellationAndGeometryPointSize() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderTessellationAndGeometryPointSizeField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getpipelineStatisticsQuery() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      pipelineStatisticsQueryField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getfragmentStoresAndAtomics() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      fragmentStoresAndAtomicsField); // primitive
}

void
VkPhysicalDeviceFeaturesAccessor::fromObject(VkPhysicalDeviceFeatures &clazzInfo) {
    clazzInfo.robustBufferAccess = getrobustBufferAccess(); // Primitive VkBool32
    clazzInfo.fullDrawIndexUint32 = getfullDrawIndexUint32(); // Primitive VkBool32
    clazzInfo.imageCubeArray = getimageCubeArray(); // Primitive VkBool32
    clazzInfo.independentBlend = getindependentBlend(); // Primitive VkBool32
    clazzInfo.geometryShader = getgeometryShader(); // Primitive VkBool32
    clazzInfo.tessellationShader = gettessellationShader(); // Primitive VkBool32
    clazzInfo.sampleRateShading = getsampleRateShading(); // Primitive VkBool32
    clazzInfo.dualSrcBlend = getdualSrcBlend(); // Primitive VkBool32
    clazzInfo.logicOp = getlogicOp(); // Primitive VkBool32
    clazzInfo.multiDrawIndirect = getmultiDrawIndirect(); // Primitive VkBool32
    clazzInfo.drawIndirectFirstInstance = getdrawIndirectFirstInstance(); // Primitive VkBool32
    clazzInfo.depthClamp = getdepthClamp(); // Primitive VkBool32
    clazzInfo.depthBiasClamp = getdepthBiasClamp(); // Primitive VkBool32
    clazzInfo.fillModeNonSolid = getfillModeNonSolid(); // Primitive VkBool32
    clazzInfo.depthBounds = getdepthBounds(); // Primitive VkBool32
    clazzInfo.wideLines = getwideLines(); // Primitive VkBool32
    clazzInfo.largePoints = getlargePoints(); // Primitive VkBool32
    clazzInfo.alphaToOne = getalphaToOne(); // Primitive VkBool32
    clazzInfo.multiViewport = getmultiViewport(); // Primitive VkBool32
    clazzInfo.samplerAnisotropy = getsamplerAnisotropy(); // Primitive VkBool32
    clazzInfo.textureCompressionETC2 = gettextureCompressionETC2(); // Primitive VkBool32
    clazzInfo.textureCompressionASTC_LDR = gettextureCompressionASTC_LDR(); // Primitive VkBool32
    clazzInfo.textureCompressionBC = gettextureCompressionBC(); // Primitive VkBool32
    clazzInfo.occlusionQueryPrecise = getocclusionQueryPrecise(); // Primitive VkBool32
    clazzInfo.pipelineStatisticsQuery = getpipelineStatisticsQuery(); // Primitive VkBool32
    clazzInfo.vertexPipelineStoresAndAtomics = getvertexPipelineStoresAndAtomics(); // Primitive VkBool32
    clazzInfo.fragmentStoresAndAtomics = getfragmentStoresAndAtomics(); // Primitive VkBool32
    clazzInfo.shaderTessellationAndGeometryPointSize = getshaderTessellationAndGeometryPointSize(); // Primitive VkBool32
    clazzInfo.shaderImageGatherExtended = getshaderImageGatherExtended(); // Primitive VkBool32
    clazzInfo.shaderStorageImageExtendedFormats = getshaderStorageImageExtendedFormats(); // Primitive VkBool32
    clazzInfo.shaderStorageImageMultisample = getshaderStorageImageMultisample(); // Primitive VkBool32
    clazzInfo.shaderStorageImageReadWithoutFormat = getshaderStorageImageReadWithoutFormat(); // Primitive VkBool32
    clazzInfo.shaderStorageImageWriteWithoutFormat = getshaderStorageImageWriteWithoutFormat(); // Primitive VkBool32
    clazzInfo.shaderUniformBufferArrayDynamicIndexing = getshaderUniformBufferArrayDynamicIndexing(); // Primitive VkBool32
    clazzInfo.shaderSampledImageArrayDynamicIndexing = getshaderSampledImageArrayDynamicIndexing(); // Primitive VkBool32
    clazzInfo.shaderStorageBufferArrayDynamicIndexing = getshaderStorageBufferArrayDynamicIndexing(); // Primitive VkBool32
    clazzInfo.shaderStorageImageArrayDynamicIndexing = getshaderStorageImageArrayDynamicIndexing(); // Primitive VkBool32
    clazzInfo.shaderClipDistance = getshaderClipDistance(); // Primitive VkBool32
    clazzInfo.shaderCullDistance = getshaderCullDistance(); // Primitive VkBool32
    clazzInfo.shaderFloat64 = getshaderFloat64(); // Primitive VkBool32
    clazzInfo.shaderInt64 = getshaderInt64(); // Primitive VkBool32
    clazzInfo.shaderInt16 = getshaderInt16(); // Primitive VkBool32
    clazzInfo.shaderResourceResidency = getshaderResourceResidency(); // Primitive VkBool32
    clazzInfo.shaderResourceMinLod = getshaderResourceMinLod(); // Primitive VkBool32
    clazzInfo.sparseBinding = getsparseBinding(); // Primitive VkBool32
    clazzInfo.sparseResidencyBuffer = getsparseResidencyBuffer(); // Primitive VkBool32
    clazzInfo.sparseResidencyImage2D = getsparseResidencyImage2D(); // Primitive VkBool32
    clazzInfo.sparseResidencyImage3D = getsparseResidencyImage3D(); // Primitive VkBool32
    clazzInfo.sparseResidency2Samples = getsparseResidency2Samples(); // Primitive VkBool32
    clazzInfo.sparseResidency4Samples = getsparseResidency4Samples(); // Primitive VkBool32
    clazzInfo.sparseResidency8Samples = getsparseResidency8Samples(); // Primitive VkBool32
    clazzInfo.sparseResidency16Samples = getsparseResidency16Samples(); // Primitive VkBool32
    clazzInfo.sparseResidencyAliased = getsparseResidencyAliased(); // Primitive VkBool32
    clazzInfo.variableMultisampleRate = getvariableMultisampleRate(); // Primitive VkBool32
    clazzInfo.inheritedQueries = getinheritedQueries(); // Primitive VkBool32
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderUniformBufferArrayDynamicIndexing() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderUniformBufferArrayDynamicIndexingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageImageArrayDynamicIndexing() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageImageArrayDynamicIndexingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageImageReadWithoutFormat() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageImageReadWithoutFormatField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderResourceMinLod() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderResourceMinLodField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::gettextureCompressionASTC_LDR() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      textureCompressionASTC_LDRField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getfullDrawIndexUint32() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, fullDrawIndexUint32Field); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getvertexPipelineStoresAndAtomics() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      vertexPipelineStoresAndAtomicsField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageBufferArrayDynamicIndexing() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageBufferArrayDynamicIndexingField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderInt16() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderInt16Field); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidency4Samples() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidency4SamplesField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::gettextureCompressionBC() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, textureCompressionBCField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderResourceResidency() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderResourceResidencyField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidencyImage3D() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidencyImage3DField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderCullDistance() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, shaderCullDistanceField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getrobustBufferAccess() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, robustBufferAccessField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getlogicOp() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, logicOpField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getsparseResidencyAliased() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      sparseResidencyAliasedField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getshaderStorageImageExtendedFormats() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj,
                                                      shaderStorageImageExtendedFormatsField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getgeometryShader() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, geometryShaderField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::gettessellationShader() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, tessellationShaderField); // primitive
}

VkBool32
VkPhysicalDeviceFeaturesAccessor::getinheritedQueries() {
    return (VkBool32) (jboolean) env->GetBooleanField(obj, inheritedQueriesField); // primitive
}

VkPhysicalDeviceFeaturesAccessor::~VkPhysicalDeviceFeaturesAccessor() {
    env->DeleteLocalRef(clazz);
}

