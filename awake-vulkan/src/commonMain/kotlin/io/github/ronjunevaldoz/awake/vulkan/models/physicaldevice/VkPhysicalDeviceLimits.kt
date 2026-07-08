/*
 * Awake
 * Awake.awake-vulkan.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ronjunevaldoz.awake.vulkan.models.physicaldevice

import io.github.ronjunevaldoz.awake.vulkan.VkBool32
import io.github.ronjunevaldoz.awake.vulkan.VkConstArray
import io.github.ronjunevaldoz.awake.vulkan.VkDeviceSize
import io.github.ronjunevaldoz.awake.vulkan.VkMutator
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSampleCountFlags

@VkMutator
class VkPhysicalDeviceLimits @JvmOverloads constructor(
    val maxImageDimension1D: UInt = 0u,
    val maxImageDimension2D: UInt = 0u,
    val maxImageDimension3D: UInt = 0u,
    val maxImageDimensionCube: UInt = 0u,
    val maxImageArrayLayers: UInt = 0u,
    val maxTexelBufferElements: UInt = 0u,
    val maxUniformBufferRange: UInt = 0u,
    val maxStorageBufferRange: UInt = 0u,
    val maxPushConstantsSize: UInt = 0u,
    val maxMemoryAllocationCount: UInt = 0u,
    val maxSamplerAllocationCount: UInt = 0u,
    val bufferImageGranularity: VkDeviceSize = 0,
    val sparseAddressSpaceSize: VkDeviceSize = 0,
    val maxBoundDescriptorSets: UInt = 0u,
    val maxPerStageDescriptorSamplers: UInt = 0u,
    val maxPerStageDescriptorUniformBuffers: UInt = 0u,
    val maxPerStageDescriptorStorageBuffers: UInt = 0u,
    val maxPerStageDescriptorSampledImages: UInt = 0u,
    val maxPerStageDescriptorStorageImages: UInt = 0u,
    val maxPerStageDescriptorInputAttachments: UInt = 0u,
    val maxPerStageResources: UInt = 0u,
    val maxDescriptorSetSamplers: UInt = 0u,
    val maxDescriptorSetUniformBuffers: UInt = 0u,
    val maxDescriptorSetUniformBuffersDynamic: UInt = 0u,
    val maxDescriptorSetStorageBuffers: UInt = 0u,
    val maxDescriptorSetStorageBuffersDynamic: UInt = 0u,
    val maxDescriptorSetSampledImages: UInt = 0u,
    val maxDescriptorSetStorageImages: UInt = 0u,
    val maxDescriptorSetInputAttachments: UInt = 0u,
    val maxVertexInputAttributes: UInt = 0u,
    val maxVertexInputBindings: UInt = 0u,
    val maxVertexInputAttributeOffset: UInt = 0u,
    val maxVertexInputBindingStride: UInt = 0u,
    val maxVertexOutputComponents: UInt = 0u,
    val maxTessellationGenerationLevel: UInt = 0u,
    val maxTessellationPatchSize: UInt = 0u,
    val maxTessellationControlPerVertexInputComponents: UInt = 0u,
    val maxTessellationControlPerVertexOutputComponents: UInt = 0u,
    val maxTessellationControlPerPatchOutputComponents: UInt = 0u,
    val maxTessellationControlTotalOutputComponents: UInt = 0u,
    val maxTessellationEvaluationInputComponents: UInt = 0u,
    val maxTessellationEvaluationOutputComponents: UInt = 0u,
    val maxGeometryShaderInvocations: UInt = 0u,
    val maxGeometryInputComponents: UInt = 0u,
    val maxGeometryOutputComponents: UInt = 0u,
    val maxGeometryOutputVertices: UInt = 0u,
    val maxGeometryTotalOutputComponents: UInt = 0u,
    val maxFragmentInputComponents: UInt = 0u,
    val maxFragmentOutputAttachments: UInt = 0u,
    val maxFragmentDualSrcAttachments: UInt = 0u,
    val maxFragmentCombinedOutputResources: UInt = 0u,
    val maxComputeSharedMemorySize: UInt = 0u,
    @VkConstArray("3")
    val maxComputeWorkGroupCount: IntArray = IntArray(3), // max 3
    val maxComputeWorkGroupInvocations: UInt = 0u,
    @VkConstArray("3")
    val maxComputeWorkGroupSize: IntArray = IntArray(3), // max 3
    val subPixelPrecisionBits: UInt = 0u,
    val subTexelPrecisionBits: UInt = 0u,
    val mipmapPrecisionBits: UInt = 0u,
    val maxDrawIndexedIndexValue: UInt = 0u,
    val maxDrawIndirectCount: UInt = 0u,
    val maxSamplerLodBias: Float = 0f,
    val maxSamplerAnisotropy: Float = 0f,
    val maxViewports: UInt = 0u,
    @VkConstArray("2")
    val maxViewportDimensions: IntArray = IntArray(2), // max 2
    @VkConstArray("2")
    val viewportBoundsRange: FloatArray = FloatArray(2), // max 2
    val viewportSubPixelBits: UInt = 0u,
    val minMemoryMapAlignment: ULong = 0u,
    val minTexelBufferOffsetAlignment: VkDeviceSize = 0,
    val minUniformBufferOffsetAlignment: VkDeviceSize = 0,
    val minStorageBufferOffsetAlignment: VkDeviceSize = 0,
    val minTexelOffset: Int = 0,
    val maxTexelOffset: UInt = 0u,
    val minTexelGatherOffset: Int = 0,
    val maxTexelGatherOffset: UInt = 0u,
    val minInterpolationOffset: Float = 0f,
    val maxInterpolationOffset: Float = 0f,
    val subPixelInterpolationOffsetBits: UInt = 0u,
    val maxFramebufferWidth: UInt = 0u,
    val maxFramebufferHeight: UInt = 0u,
    val maxFramebufferLayers: UInt = 0u,
    val framebufferColorSampleCounts: VkSampleCountFlags = 0,
    val framebufferDepthSampleCounts: VkSampleCountFlags = 0,
    val framebufferStencilSampleCounts: VkSampleCountFlags = 0,
    val framebufferNoAttachmentsSampleCounts: VkSampleCountFlags = 0,
    val maxColorAttachments: UInt = 0u,
    val sampledImageColorSampleCounts: VkSampleCountFlags = 0,
    val sampledImageIntegerSampleCounts: VkSampleCountFlags = 0,
    val sampledImageDepthSampleCounts: VkSampleCountFlags = 0,
    val sampledImageStencilSampleCounts: VkSampleCountFlags = 0,
    val storageImageSampleCounts: VkSampleCountFlags = 0,
    val maxSampleMaskWords: UInt = 0u,
    val timestampComputeAndGraphics: VkBool32 = false,
    val timestampPeriod: Float = 0f,
    val maxClipDistances: UInt = 0u,
    val maxCullDistances: UInt = 0u,
    val maxCombinedClipAndCullDistances: UInt = 0u,
    val discreteQueuePriorities: UInt = 0u,
    @VkConstArray("2")
    val pointSizeRange: FloatArray = FloatArray(2), // max size 2
    @VkConstArray("2")
    val lineWidthRange: FloatArray = FloatArray(2), // max size 2
    val pointSizeGranularity: Float = 0f,
    val lineWidthGranularity: Float = 0f,
    val strictLines: VkBool32 = false,
    val standardSampleLocations: VkBool32 = false,
    val optimalBufferCopyOffsetAlignment: VkDeviceSize = 0,
    val optimalBufferCopyRowPitchAlignment: VkDeviceSize = 0,
    val nonCoherentAtomSize: VkDeviceSize = 0
)