// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.VK_SUBPASS_EXTERNAL
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkAttachmentStoreOp
import io.github.ronjunevaldoz.awake.vulkan.enums.VkBlendFactor
import io.github.ronjunevaldoz.awake.vulkan.enums.VkColorComponentFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkDynamicState
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFormat
import io.github.ronjunevaldoz.awake.vulkan.enums.VkFrontFace
import io.github.ronjunevaldoz.awake.vulkan.enums.VkImageLayout
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPipelineBindPoint
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPolygonMode
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPrimitiveTopology
import io.github.ronjunevaldoz.awake.vulkan.enums.VkShaderStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkVertexInputRate
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkAccessFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentDescription
import io.github.ronjunevaldoz.awake.vulkan.models.VkAttachmentReference
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkSubpassDependency
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkGraphicsPipelineCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkShaderModuleCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubpassDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineCacheCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendAttachmentState
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineColorBlendStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDepthStencilStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineDynamicStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineInputAssemblyStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineLayoutCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineMultisampleStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineRasterizationStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineShaderStageCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineVertexInputStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkPipelineViewportStateCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputAttributeDescription
import io.github.ronjunevaldoz.awake.vulkan.models.info.pipeline.VkVertexInputBindingDescription
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/**
 * Phase 2 (renderer abstraction): owns the render pass + graphics pipeline -- extracted
 * verbatim from `VulkanApplication`'s `createRenderPass`/`createGraphicsPipeline`/
 * `createShaderModule` functions and their backing fields (`renderPass`, `pipelineLayout`,
 * `pipelineCache`, `graphicsPipeline`). Same two color/depth attachments, same fixed
 * position/color vertex attribute layout, same `NONE` cull mode (see the rasterization state's
 * comment for why) -- this is a structural move, not a behavior change.
 *
 * Takes compiled SPIR-V bytecode directly (a [ShaderPair]) rather than loading it itself:
 * reading a shader asset off disk is a platform/resource concern
 * (`readResourceBytes`), not a pipeline concern, matching how [Mesh][io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh]
 * takes raw vertex/index data instead of loading a model file itself.
 *
 * Takes [Material]'s [DescriptorSetLayoutHandle] (not the whole [Material] instance) because
 * that's the only thing the pipeline layout actually needs from it -- a real
 * `Material`/`Shader` system with more than one descriptor set layout would change this
 * signature, but there's only one material in this demo today.
 *
 * The vertex attribute layout is driven by [vertexFormat]
 * ([io.github.ronjunevaldoz.awake.render.mesh.VertexFormat]) -- each of its entries becomes one
 * [VkVertexInputAttributeDescription], location/offset/format read straight off the entry
 * instead of a hand-written table. Defaults to
 * [VertexFormat.PositionColorUv][io.github.ronjunevaldoz.awake.render.mesh.VertexFormat.PositionColorUv],
 * bit-for-bit the layout this class hardcoded before the abstraction existed, so every
 * pre-existing caller/shader pair keeps working unchanged.
 */
class RenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    shaders: ShaderPair,
    val vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    vertexEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    fragmentEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    /** `VK_POLYGON_MODE_LINE` builds a wireframe companion of an otherwise-identical pipeline
     * (same shaders/vertex layout/render pass) -- see `Renderer.wireframe`'s doc comment.
     * Requires the device's `fillModeNonSolid` feature; not requested separately here because
     * `GraphicsDevice.createLogicalDevice` already enables every feature the physical device
     * reports as available (`pEnabledFeatures = arrayOf(features)`, not a hand-picked subset),
     * so this is already on whenever the GPU supports it -- confirmed by reading that
     * function, not assumed. */
    polygonMode: VkPolygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL,
    /** Adds a SECOND vertex binding (binding 1, `VK_VERTEX_INPUT_RATE_INSTANCE`, stride 64)
     * carrying one `mat4` model matrix per instance as 4 consecutive `R32G32B32A32_SFLOAT`
     * attributes -- see `instanced.wgsl`, which declares exactly those. Purely additive:
     * binding 0 and [vertexFormat]'s own attributes are untouched, so a `false` (default)
     * pipeline is byte-for-byte the one this class always built. The 4 attribute locations
     * continue past [vertexFormat]'s own highest one, so no format needs its locations
     * renumbered to make room. */
    instanced: Boolean = false,
    /** Only meaningful alongside [instanced]. Adds a THIRD vertex binding (binding 2,
     * `VK_VERTEX_INPUT_RATE_INSTANCE`, stride 4, one `R32_SFLOAT` attribute) carrying one
     * `f32` alpha per instance -- see `particle.wgsl` and `DrawCall.instanceAlphas`' own doc
     * comment. Purely additive, same as [instanced] itself: `false` (default) leaves the
     * pipeline byte-for-byte what it always built. */
    instanceAlpha: Boolean = false,
    /** `false` (default) is byte-for-byte the opaque pipeline this class always built. `true`
     * enables standard straight-alpha blending (`SRC_ALPHA`/`ONE_MINUS_SRC_ALPHA`, both color
     * and alpha) -- the same blend state [io.github.ronjunevaldoz.awake.vulkan.ui
     * .UiTextureRenderPipeline] already uses for its own alpha-blended textured quads. */
    blendEnabled: Boolean = false,
    /** `true` (default) is byte-for-byte what this class always built. `false` disables depth
     * WRITE only (depth TEST stays on, so blended content still hides behind real geometry) --
     * for content drawn back-to-front or order-independent, where multiple overlapping
     * instances writing depth against each other would fight/self-occlude (e.g. particles). */
    depthWriteEnabled: Boolean = true,
    /** Descriptor set layouts appended AFTER [descriptorSetLayout] (which stays set 0), one per
     * additional set the shader declares. Today's only user is the skinned-instanced pipeline,
     * whose `@group(1)` joint-palette storage buffer is owned per draw call rather than per
     * material -- see `SkinnedInstanceBuffer`'s doc comment. Empty (default) leaves the pipeline
     * layout exactly the single-set one this class always built. */
    extraDescriptorSetLayouts: List<DescriptorSetLayoutHandle> = emptyList(),
) {
    // Read by createGraphicsPipeline through the field rather than passed to it: that function
    // is already at detekt's parameter ceiling.
    private val extraDescriptorSetLayouts = extraDescriptorSetLayouts
    private val graphicsDevice = graphicsDevice
    private val swapchainManager = swapchainManager
    private val device get() = graphicsDevice.device

    var renderPass: Long = 0
    var pipelineLayout: Long = 0
    var pipelineCache: Long = 0
    var graphicsPipeline: LongArray = longArrayOf()

    init {
        renderPass = createRenderPass()
        createGraphicsPipeline(
            descriptorSetLayout = descriptorSetLayout,
            vertShaderCode = shaders.vertex,
            fragShaderCode = shaders.fragment,
            vertexFormat = vertexFormat,
            vertexEntryPoint = vertexEntryPoint,
            fragmentEntryPoint = fragmentEntryPoint,
            polygonMode = polygonMode,
            instanced = instanced,
            instanceAlpha = instanceAlpha,
            blendEnabled = blendEnabled,
            depthWriteEnabled = depthWriteEnabled,
        )
    }

    private fun createRenderPass(): Long = Vulkan.vkCreateRenderPass(
        device,
        VkRenderPassCreateInfo(
            pAttachments = arrayOf(
                VkAttachmentDescription(
                    format = swapchainManager.imageFormat,
                    initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                    // COLOR_ATTACHMENT_OPTIMAL, not PRESENT_SRC_KHR: the UI overlay pass
                    // (UiRenderPipeline) draws on top of this pass's output before the
                    // final present-layout transition happens, at the end of that pass
                    // instead. See docs/MVP_PLAN.md's custom-UI decision log entry.
                    finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                ),
                VkAttachmentDescription(
                    format = VkFormat.VK_FORMAT_D32_SFLOAT,
                    storeOp = VkAttachmentStoreOp.DONT_CARE,
                    initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED,
                    finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                ),
            ),
            pSubpasses = arrayOf(
                VkSubpassDescription(
                    pipelineBindPoint = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pColorAttachments = arrayOf(
                        VkAttachmentReference(
                            attachment = 0,
                            layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                        ),
                    ),
                    pDepthStencilAttachment = arrayOf(
                        VkAttachmentReference(
                            attachment = 1,
                            layout = VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                        ),
                    ),
                ),
            ),
            pDependencies = arrayOf(
                VkSubpassDependency(
                    srcSubpass = VK_SUBPASS_EXTERNAL,
                    dstSubpass = 0,
                    srcStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                        VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                    srcAccessMask = 0,
                    dstStageMask = VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value or
                        VkPipelineStageFlagBits.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT.value,
                    dstAccessMask = VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT.value or
                        VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.value or
                        VkAccessFlagBits.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT.value,
                ),
            ),
        ),
    )

    private fun createShaderModule(code: IntArray): Long {
        val createInfo = VkShaderModuleCreateInfo(pCode = code)
        return Vulkan.vkCreateShaderModule(device, createInfo)
    }

    private fun ByteArray.toIntArray(): IntArray = IntArray(this.size / 4) { i ->
        (this[i * 4].toInt() and 0xFF) or
            ((this[i * 4 + 1].toInt() and 0xFF) shl 8) or
            ((this[i * 4 + 2].toInt() and 0xFF) shl 16) or
            ((this[i * 4 + 3].toInt() and 0xFF) shl 24)
    }

    private fun createGraphicsPipeline(
        descriptorSetLayout: DescriptorSetLayoutHandle,
        vertShaderCode: ByteArray,
        fragShaderCode: ByteArray,
        vertexFormat: VertexFormat,
        vertexEntryPoint: String,
        fragmentEntryPoint: String,
        polygonMode: VkPolygonMode,
        instanced: Boolean,
        instanceAlpha: Boolean,
        blendEnabled: Boolean,
        depthWriteEnabled: Boolean,
    ) {
        // WARNING: make sure the .spv vulkan version match, this might cause out of memory
        val fragShaderModule = createShaderModule(fragShaderCode.toIntArray())
        val vertShaderModule = createShaderModule(vertShaderCode.toIntArray())

        // process shader
        val fragShaderStageInfo = VkPipelineShaderStageCreateInfo(
            stage = VkShaderStageFlagBits.FRAGMENT,
            module = fragShaderModule,
            pName = fragmentEntryPoint,
        )
        val vertShaderStageInfo = VkPipelineShaderStageCreateInfo(
            stage = VkShaderStageFlagBits.VERTEX,
            module = vertShaderModule,
            pName = vertexEntryPoint,
        )
        val shaderStages = arrayOf(fragShaderStageInfo, vertShaderStageInfo)

        val vertexBindings = mutableListOf(
            VkVertexInputBindingDescription(
                binding = 0,
                stride = vertexFormat.strideBytes,
                inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX,
            ),
        )
        val vertexAttributes = vertexFormat.entries.mapTo(mutableListOf()) { entry ->
            VkVertexInputAttributeDescription(
                location = entry.attribute.location,
                binding = 0,
                format = entry.attribute.format.toVkFormat(),
                offset = entry.offsetBytes,
            )
        }
        if (instanced) {
            vertexBindings += VkVertexInputBindingDescription(
                binding = INSTANCE_BINDING,
                stride = INSTANCE_MATRIX_BYTES,
                inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE,
            )
            val firstLocation = vertexFormat.attributes.maxOf { it.location } + 1
            repeat(MATRIX_ROWS) { row ->
                vertexAttributes += VkVertexInputAttributeDescription(
                    location = firstLocation + row,
                    binding = INSTANCE_BINDING,
                    format = VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT,
                    offset = row * VEC4_BYTES,
                )
            }
            if (instanceAlpha) {
                vertexBindings += VkVertexInputBindingDescription(
                    binding = INSTANCE_ALPHA_BINDING,
                    stride = Float.SIZE_BYTES,
                    inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE,
                )
                vertexAttributes += VkVertexInputAttributeDescription(
                    location = firstLocation + MATRIX_ROWS,
                    binding = INSTANCE_ALPHA_BINDING,
                    format = VkFormat.VK_FORMAT_R32_SFLOAT,
                    offset = 0,
                )
            }
        }
        val vertexInputInfo = arrayOf(
            VkPipelineVertexInputStateCreateInfo(
                pVertexBindingDescriptions = vertexBindings.toTypedArray(),
                pVertexAttributeDescriptions = vertexAttributes.toTypedArray(),
            ),
        )

        val dynamicInfo = arrayOf(
            VkPipelineDynamicStateCreateInfo(
                pDynamicStates = arrayOf(
                    VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT,
                    VkDynamicState.VK_DYNAMIC_STATE_SCISSOR,
                ),
            ),
        )

        val viewportInfo = arrayOf(
            VkPipelineViewportStateCreateInfo(
                pViewports = arrayOf(
                    VkViewport(
                        width = swapchainManager.extent.width.toFloat(),
                        height = swapchainManager.extent.height.toFloat(),
                    ),
                ),
                pScissors = arrayOf(
                    VkRect2D(
                        offset = VkOffset2D(),
                        extent = swapchainManager.extent,
                    ),
                ),
            ),
        )

        val depthStencil = arrayOf(
            VkPipelineDepthStencilStateCreateInfo(depthWriteEnable = depthWriteEnabled),
        )

        val multisamplingInfo = arrayOf(
            VkPipelineMultisampleStateCreateInfo(),
        )
        // Specify we will use triangle lists to draw geometry.
        val inputAssemblyInfo = arrayOf(
            VkPipelineInputAssemblyStateCreateInfo(
                topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
                primitiveRestartEnable = false,
            ),
        )
        // Specify rasterization state.
        val rasterizationInfo = arrayOf(
            VkPipelineRasterizationStateCreateInfo(
                // NONE, deliberately: the demo cube's index winding isn't guaranteed
                // outward-consistent per face -- depth testing alone resolves correct
                // occlusion regardless of triangle winding. Revisit once per-face vertex
                // duplication makes winding order meaningful.
                cullMode = VkCullModeFlagBits.VK_CULL_MODE_NONE.value,
                frontFace = VkFrontFace.VK_FRONT_FACE_CLOCKWISE,
                polygonMode = polygonMode,
                lineWidth = 1f,
            ),
        )

        val blendAttachment = VkPipelineColorBlendAttachmentState(
            blendEnable = blendEnabled,
            srcColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA,
            dstColorBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
            srcAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA,
            dstAlphaBlendFactor = VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
            colorWriteMask = VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT.value or
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT.value,
        )

        val colorBlendInfo = arrayOf(
            VkPipelineColorBlendStateCreateInfo(
                pAttachments = arrayOf(blendAttachment),
            ),
        )

        pipelineLayout = Vulkan.vkCreatePipelineLayout(
            device,
            VkPipelineLayoutCreateInfo(
                pSetLayouts = (
                    listOf(descriptorSetLayout.handle) + extraDescriptorSetLayouts.map { it.handle }
                    ).toTypedArray(),
            ),
        )

        val createInfos = arrayOf(
            VkGraphicsPipelineCreateInfo(
                pStages = shaderStages,
                pVertexInputState = vertexInputInfo,
                pInputAssemblyState = inputAssemblyInfo,
                pViewportState = viewportInfo,
                pRasterizationState = rasterizationInfo,
                pMultisampleState = multisamplingInfo,
                pColorBlendState = colorBlendInfo,
                pDepthStencilState = depthStencil,
                pDynamicState = dynamicInfo,
                layout = pipelineLayout,
                renderPass = renderPass,
                subpass = 0,
                basePipelineHandle = 0, // Optional
                basePipelineIndex = -1, // Optional
            ),
        )
        pipelineCache = Vulkan.vkCreatePipelineCache(device, VkPipelineCacheCreateInfo())
        graphicsPipeline = Vulkan.vkCreateGraphicsPipelines(device, pipelineCache, createInfos)

        Vulkan.vkDestroyShaderModule(device, fragShaderModule)
        Vulkan.vkDestroyShaderModule(device, vertShaderModule)
    }

    /** Binds the (single) graphics pipeline. Render-pass begin/end and descriptor-set
     * binding (a [Material] concern) happen around/after this, not here. */
    fun bind(commandBuffer: Long) {
        Vulkan.vkCmdBindPipeline(
            commandBuffer,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            graphicsPipeline[0],
        )
    }

    fun destroy() {
        graphicsPipeline.forEach { pipeline ->
            Vulkan.vkDestroyPipeline(device, pipeline)
        }
        Vulkan.vkDestroyPipelineLayout(device, pipelineLayout)
        Vulkan.vkDestroyRenderPass(device, renderPass)
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
    }

    private companion object {
        const val DEFAULT_SHADER_ENTRY_POINT = "main"

        /** See the `instanced` constructor parameter. Binding 1 (binding 0 is the mesh's own
         * per-vertex buffer); one `mat4` = 4 `vec4` rows = 64 bytes per instance. */
        const val INSTANCE_BINDING = 1
        const val MATRIX_ROWS = 4
        const val VEC4_BYTES = 16
        const val INSTANCE_MATRIX_BYTES = MATRIX_ROWS * VEC4_BYTES

        /** See the `instanceAlpha` constructor parameter. Binding 2 -- one binding past the
         * instance matrix's own binding 1. */
        const val INSTANCE_ALPHA_BINDING = 2
    }
}

private fun GpuDataShape.toVkFormat(): VkFormat = when (this) {
    GpuDataShape.Float -> VkFormat.VK_FORMAT_R32_SFLOAT
    GpuDataShape.Vec2 -> VkFormat.VK_FORMAT_R32G32_SFLOAT
    GpuDataShape.Vec3 -> VkFormat.VK_FORMAT_R32G32B32_SFLOAT
    GpuDataShape.Vec4 -> VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT
    GpuDataShape.UInt4 -> VkFormat.VK_FORMAT_R32G32B32A32_UINT
    GpuDataShape.Mat4 -> error("Mat4 is not a valid vertex-attribute format.")
}
