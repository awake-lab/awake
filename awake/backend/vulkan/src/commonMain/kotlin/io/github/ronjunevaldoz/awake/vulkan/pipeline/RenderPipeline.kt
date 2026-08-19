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
 * The pipeline-variant shapes [RenderPipeline] builds on top of its otherwise-fixed vertex/
 * render-pass setup -- a closed set of named presets, not 4 independent booleans, so a caller
 * can't write a combo nobody means (e.g. [instanceAlpha] without [instanced]). A new pipeline
 * shape (the next particle-style variant) adds one named object here instead of growing
 * [RenderPipeline]'s constructor and its private `createGraphicsPipeline`'s parameter list
 * again.
 */
sealed interface PipelineVariant {
    /** Adds a SECOND vertex binding (binding 1, `VK_VERTEX_INPUT_RATE_INSTANCE`, stride 64)
     * carrying one `mat4` model matrix per instance -- see `instanced.wgsl`. */
    val instanced: Boolean

    /** Only meaningful alongside [instanced]. Adds a THIRD vertex binding (binding 2, stride 16)
     * carrying one `vec4f` RGBA color+alpha per instance -- see `particle.wgsl` and
     * `DrawCall.instanceColors`. */
    val instanceAlpha: Boolean

    /** `true` enables standard straight-alpha blending (`SRC_ALPHA`/`ONE_MINUS_SRC_ALPHA`, both
     * color and alpha) -- the same blend state [io.github.ronjunevaldoz.awake.vulkan.ui
     * .UiTextureRenderPipeline] already uses. */
    val blendEnabled: Boolean

    /** `false` disables depth WRITE only (depth TEST stays on) -- for order-independent content
     * that shouldn't self-occlude (e.g. particles). */
    val depthWriteEnabled: Boolean

    /** Byte-for-byte the pipeline this class always built before any variant existed -- the
     * default for every non-instanced draw (primary/wireframe/textured/skinned/shadow). */
    data object Opaque : PipelineVariant {
        override val instanced = false
        override val instanceAlpha = false
        override val blendEnabled = false
        override val depthWriteEnabled = true
    }

    /** Plain GPU instancing (`InstancedMeshRenderer`/`InstancedSkinnedMeshRenderer`) -- one
     * model matrix per instance, opaque depth behavior unchanged. */
    data object Instanced : PipelineVariant {
        override val instanced = true
        override val instanceAlpha = false
        override val blendEnabled = false
        override val depthWriteEnabled = true
    }

    /** Billboard particles -- instanced + per-instance alpha + straight-alpha blend + no depth
     * write (order-independent draws don't self-occlude). See `particle.wgsl`. */
    data object AlphaBlendedParticle : PipelineVariant {
        override val instanced = true
        override val instanceAlpha = true
        override val blendEnabled = true
        override val depthWriteEnabled = false
    }
}

/**
 * Owns a render pass + graphics pipeline. Takes compiled SPIR-V ([ShaderPair]) rather than a
 * shader asset path -- loading shaders is a platform/resource concern, not a pipeline one.
 * Takes [Material]'s [DescriptorSetLayoutHandle], not the whole [Material] instance -- the
 * pipeline layout only needs the layout handle. Vertex attribute layout is driven by
 * [vertexFormat]; each entry becomes one [VkVertexInputAttributeDescription].
 */
class RenderPipeline(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    /** Shared across every [RenderPipeline] a caller builds against the same swapchain --
     * see the top-level [createSceneRenderPass] this class no longer creates itself. Not
     * owned/destroyed by this class; the caller that created it destroys it exactly once. */
    val renderPass: Long,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    shaders: ShaderPair,
    val vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    vertexEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    fragmentEntryPoint: String = DEFAULT_SHADER_ENTRY_POINT,
    /** `VK_POLYGON_MODE_LINE` builds a wireframe companion pipeline (see `Renderer.wireframe`).
     * Requires `fillModeNonSolid` -- already enabled, since `GraphicsDevice.createLogicalDevice`
     * enables every feature the device reports. */
    polygonMode: VkPolygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL,
    /** See [PipelineVariant]'s own doc comment. Defaults to [PipelineVariant.Opaque] -- the
     * pipeline this class always built before any variant existed. */
    variant: PipelineVariant = PipelineVariant.Opaque,
    /** Extra descriptor set layouts appended after [descriptorSetLayout] (set 0). Today's only
     * user: the skinned-instanced pipeline's `@group(1)` joint-palette buffer. */
    extraDescriptorSetLayouts: List<DescriptorSetLayoutHandle> = emptyList(),
) {
    // Read by the create* builders through the field rather than passed as a param: that's
    // exactly the parameter-count pressure this class's own doc comment on createPipelineLayout
    // exists to avoid growing further.
    private val extraDescriptorSetLayouts = extraDescriptorSetLayouts
    private val graphicsDevice = graphicsDevice
    private val swapchainManager = swapchainManager
    private val device get() = graphicsDevice.device

    var pipelineLayout: Long = 0
    var pipelineCache: Long = 0
    var graphicsPipeline: LongArray = longArrayOf()

    // destroy() is zero-handle-tolerant, so calling it on a partial-construction failure is
    // safe and avoids a leak. renderPass isn't touched -- it's caller-owned.
    init {
        try {
            createGraphicsPipeline(
                descriptorSetLayout = descriptorSetLayout,
                shaders = shaders,
                vertexFormat = vertexFormat,
                vertexEntryPoint = vertexEntryPoint,
                fragmentEntryPoint = fragmentEntryPoint,
                polygonMode = polygonMode,
                variant = variant,
            )
        } catch (e: Throwable) {
            destroy()
            throw e
        }
    }

    private fun createGraphicsPipeline(
        descriptorSetLayout: DescriptorSetLayoutHandle,
        shaders: ShaderPair,
        vertexFormat: VertexFormat,
        vertexEntryPoint: String,
        fragmentEntryPoint: String,
        polygonMode: VkPolygonMode,
        variant: PipelineVariant,
    ) {
        // WARNING: make sure the .spv vulkan version match, this might cause out of memory
        val fragShaderModule = createShaderModule(device, shaders.fragment.toShaderIntArray())
        val vertShaderModule = createShaderModule(device, shaders.vertex.toShaderIntArray())
        val shaderStages = arrayOf(
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.FRAGMENT,
                module = fragShaderModule,
                pName = fragmentEntryPoint,
            ),
            VkPipelineShaderStageCreateInfo(
                stage = VkShaderStageFlagBits.VERTEX,
                module = vertShaderModule,
                pName = vertexEntryPoint,
            ),
        )

        pipelineLayout = createPipelineLayout(device, descriptorSetLayout, extraDescriptorSetLayouts)

        val createInfos = arrayOf(
            VkGraphicsPipelineCreateInfo(
                pStages = shaderStages,
                pVertexInputState = vertexInputState(vertexFormat, variant),
                pInputAssemblyState = INPUT_ASSEMBLY_STATE,
                pViewportState = viewportState(swapchainManager),
                pRasterizationState = rasterizationState(polygonMode),
                pMultisampleState = MULTISAMPLE_STATE,
                pColorBlendState = colorBlendState(variant.blendEnabled),
                pDepthStencilState = depthStencilState(variant.depthWriteEnabled),
                pDynamicState = DYNAMIC_STATE,
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
        Vulkan.vkDestroyPipelineCache(device, pipelineCache)
    }

}

private const val DEFAULT_SHADER_ENTRY_POINT = "main"

/** Render pass shared by every [RenderPipeline] built against the same [swapchainManager] --
 * avoids one near-identical render pass per pipeline. Not owned by any pipeline; caller
 * destroys it exactly once. */
fun createSceneRenderPass(graphicsDevice: GraphicsDevice, swapchainManager: SwapchainManager): Long =
    Vulkan.vkCreateRenderPass(
        graphicsDevice.device,
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

/** See [PipelineVariant.instanced]. Binding 1 (binding 0 is the mesh's own per-vertex buffer);
 * one `mat4` = 4 `vec4` rows = 64 bytes per instance. */
private const val INSTANCE_BINDING = 1
private const val MATRIX_ROWS = 4
private const val VEC4_BYTES = 16
private const val INSTANCE_MATRIX_BYTES = MATRIX_ROWS * VEC4_BYTES

/** See [PipelineVariant.instanceAlpha]. Binding 2 -- one binding past the instance matrix's own
 * binding 1. */
private const val INSTANCE_ALPHA_BINDING = 2

/** Fixed structs that never vary per pipeline -- built once instead of reconstructed on every
 * `RenderPipeline`'s own `createGraphicsPipeline` call. */
private val DYNAMIC_STATE = arrayOf(
    VkPipelineDynamicStateCreateInfo(
        pDynamicStates = arrayOf(
            VkDynamicState.VK_DYNAMIC_STATE_VIEWPORT,
            VkDynamicState.VK_DYNAMIC_STATE_SCISSOR,
        ),
    ),
)
private val MULTISAMPLE_STATE = arrayOf(VkPipelineMultisampleStateCreateInfo())

// Specify we will use triangle lists to draw geometry.
private val INPUT_ASSEMBLY_STATE = arrayOf(
    VkPipelineInputAssemblyStateCreateInfo(
        topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
        primitiveRestartEnable = false,
    ),
)

/** One `VK_VERTEX_INPUT_RATE_INSTANCE` binding plus its attribute(s) -- [attributes] is
 * `(offsetBytes, format)` per attribute, at consecutive locations starting at [firstLocation].
 * Both the model-matrix binding (4 attributes, one per row) and the alpha binding (1
 * attribute) are this same shape, just different attribute counts/formats. Top-level, not a
 * [RenderPipeline] member: it's a pure function of its params, and every member here that
 * doesn't need instance state stays out of the class to keep it under detekt's
 * `TooManyFunctions` threshold. */
private fun instanceRateBinding(
    binding: Int,
    strideBytes: Int,
    firstLocation: Int,
    attributes: List<Pair<Int, VkFormat>>,
): Pair<VkVertexInputBindingDescription, List<VkVertexInputAttributeDescription>> {
    val bindingDescription = VkVertexInputBindingDescription(
        binding = binding,
        stride = strideBytes,
        inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE,
    )
    val attributeDescriptions = attributes.mapIndexed { index, (offset, format) ->
        VkVertexInputAttributeDescription(
            location = firstLocation + index,
            binding = binding,
            format = format,
            offset = offset,
        )
    }
    return bindingDescription to attributeDescriptions
}

private fun vertexInputState(
    vertexFormat: VertexFormat,
    variant: PipelineVariant,
): Array<VkPipelineVertexInputStateCreateInfo> {
    val bindings = mutableListOf(
        VkVertexInputBindingDescription(
            binding = 0,
            stride = vertexFormat.strideBytes,
            inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX,
        ),
    )
    val attributes = vertexFormat.entries.mapTo(mutableListOf()) { entry ->
        VkVertexInputAttributeDescription(
            location = entry.attribute.location,
            binding = 0,
            format = entry.attribute.format.toVkFormat(),
            offset = entry.offsetBytes,
        )
    }
    if (variant.instanced) {
        val firstLocation = vertexFormat.attributes.maxOf { it.location } + 1
        val (matrixBinding, matrixAttributes) = instanceRateBinding(
            INSTANCE_BINDING,
            INSTANCE_MATRIX_BYTES,
            firstLocation,
            (0 until MATRIX_ROWS).map { it * VEC4_BYTES to VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT },
        )
        bindings += matrixBinding
        attributes += matrixAttributes
        if (variant.instanceAlpha) {
            // One vec4 (rgba) per instance, not a lone float -- alpha rides in .w alongside
            // per-particle color, see DrawCall.instanceColors' own doc comment.
            val (colorBinding, colorAttributes) = instanceRateBinding(
                INSTANCE_ALPHA_BINDING,
                VEC4_BYTES,
                firstLocation + MATRIX_ROWS,
                listOf(0 to VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT),
            )
            bindings += colorBinding
            attributes += colorAttributes
        }
    }
    return arrayOf(
        VkPipelineVertexInputStateCreateInfo(
            pVertexBindingDescriptions = bindings.toTypedArray(),
            pVertexAttributeDescriptions = attributes.toTypedArray(),
        ),
    )
}

private fun viewportState(swapchainManager: SwapchainManager): Array<VkPipelineViewportStateCreateInfo> = arrayOf(
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

private fun depthStencilState(depthWriteEnabled: Boolean): Array<VkPipelineDepthStencilStateCreateInfo> = arrayOf(
    VkPipelineDepthStencilStateCreateInfo(depthWriteEnable = depthWriteEnabled),
)

private fun rasterizationState(polygonMode: VkPolygonMode): Array<VkPipelineRasterizationStateCreateInfo> = arrayOf(
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

private fun colorBlendState(blendEnabled: Boolean): Array<VkPipelineColorBlendStateCreateInfo> {
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
    return arrayOf(VkPipelineColorBlendStateCreateInfo(pAttachments = arrayOf(blendAttachment)))
}

private fun createPipelineLayout(
    device: Long,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    extraDescriptorSetLayouts: List<DescriptorSetLayoutHandle>,
): Long = Vulkan.vkCreatePipelineLayout(
    device,
    VkPipelineLayoutCreateInfo(
        pSetLayouts = (
            listOf(descriptorSetLayout.handle) + extraDescriptorSetLayouts.map { it.handle }
            ).toTypedArray(),
    ),
)

private fun GpuDataShape.toVkFormat(): VkFormat = when (this) {
    GpuDataShape.Float -> VkFormat.VK_FORMAT_R32_SFLOAT
    GpuDataShape.Vec2 -> VkFormat.VK_FORMAT_R32G32_SFLOAT
    GpuDataShape.Vec3 -> VkFormat.VK_FORMAT_R32G32B32_SFLOAT
    GpuDataShape.Vec4 -> VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT
    GpuDataShape.UInt4 -> VkFormat.VK_FORMAT_R32G32B32A32_UINT
    GpuDataShape.Mat4 -> error("Mat4 is not a valid vertex-attribute format.")
}
