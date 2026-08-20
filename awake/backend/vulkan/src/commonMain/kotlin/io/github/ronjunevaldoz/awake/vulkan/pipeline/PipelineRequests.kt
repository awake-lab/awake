// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCullModeFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.VkPolygonMode
import io.github.ronjunevaldoz.awake.vulkan.handles.DescriptorSetLayoutHandle
import io.github.ronjunevaldoz.awake.vulkan.swapchain.SwapchainManager

/** Identifies ONE [RenderPipeline] a [PipelineRequest] builds, used as the key into
 * [buildRequestedPipelines]'s returned map. [Primary] is a distinct case from [Format] on
 * purpose -- even though the primary pipeline's own [VertexFormat] can numerically equal a key
 * some [Format] entry also uses (nothing stops a caller's `additionalPipelines` map from
 * targeting the same format the primary pipeline already draws), the two are never the same
 * sealed case, so they can never collide as map keys. */
sealed interface PipelineKey {
    data object Primary : PipelineKey
    data class Format(val vertexFormat: VertexFormat) : PipelineKey
    data object Instanced : PipelineKey
    data object SkinnedInstanced : PipelineKey
    data object Particle : PipelineKey
}

/** Describes ONE optional [RenderPipeline] for [buildRequestedPipelines] to build -- fields are
 * a 1:1 flattening of [RenderPipeline]'s own constructor params that vary per-request
 * (everything else -- graphicsDevice/swapchainManager/descriptorSetLayout -- is constant across
 * a whole batch and stays a [buildRequestedPipelines] param instead of being repeated here). */
data class PipelineRequest(
    val key: PipelineKey,
    val vertexShaderResourcePath: String,
    val fragmentShaderResourcePath: String,
    val vertexFormat: VertexFormat,
    val vertexEntryPoint: String,
    val fragmentEntryPoint: String,
    /** Builds a second, `VK_POLYGON_MODE_LINE` pipeline reusing this request's own loaded
     * shaders. Only ever `true` for the primary/[PipelineKey.Format] requests -- instanced/
     * skinned-instanced/particle pipelines have never had a wireframe companion. */
    val buildWireframe: Boolean = false,
    /** Builds a third, `VK_CULL_MODE_BACK_BIT` companion pipeline reusing this request's own
     * loaded shaders -- see `render.renderer.CullMode`'s own doc comment. Only ever `true` for
     * the primary/[PipelineKey.Format] requests, same scope [buildWireframe] already has;
     * instanced/skinned-instanced/particle meshes don't opt into per-mesh culling yet. */
    val buildBackCulled: Boolean = false,
    val variant: PipelineVariant = PipelineVariant.Opaque,
    val extraDescriptorSetLayouts: List<DescriptorSetLayoutHandle> = emptyList(),
)

/** The up-to-3 [RenderPipeline]s [buildRequestedPipelines] builds per [PipelineRequest] --
 * [wireframe]/[backCulled] are each only present when that request's own
 * [PipelineRequest.buildWireframe]/[PipelineRequest.buildBackCulled] was `true`. */
data class RequestedPipelines(
    val fill: RenderPipeline,
    val wireframe: RenderPipeline? = null,
    val backCulled: RenderPipeline? = null,
)

/** Builds one [RenderPipeline] (plus its optional wireframe/back-culled companions) per
 * [requests] entry -- the single loop that replaces the 3 near-identical
 * `shaderSet?.let { load + build }` blocks `VulkanGameApplication.createBackendResources` used
 * to hand-roll for additionalPipelines/instanced/skinnedInstanced/particle. [loadShaders] is
 * injected rather than called directly so this file doesn't need to know about
 * `readResourceBytes`/`ShaderPair`'s IO.
 *
 * [renderPass] is built ONCE by the caller (via [createSceneRenderPass]) and passed to every
 * [RenderPipeline] this builds -- every request shares the same swapchain/depth attachment
 * shape, so one render pass covers all of them instead of one per pipeline. */
suspend fun buildRequestedPipelines(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPass: Long,
    descriptorSetLayout: DescriptorSetLayoutHandle,
    requests: List<PipelineRequest>,
    loadShaders: suspend (vertexPath: String, fragmentPath: String) -> ShaderPair,
): Map<PipelineKey, RequestedPipelines> = buildMap {
    requests.forEach { request ->
        // Wrapped per-request, not once around the whole loop: a resource-not-found or
        // shader-module-creation failure otherwise surfaces with only a bare file path/native
        // error code, giving no hint which of the (up to 7) pipelines being built was the one
        // that actually failed.
        try {
            val shaders = loadShaders(request.vertexShaderResourcePath, request.fragmentShaderResourcePath)
            val fill = RenderPipeline(
                graphicsDevice,
                swapchainManager,
                renderPass,
                descriptorSetLayout,
                shaders,
                request.vertexFormat,
                request.vertexEntryPoint,
                request.fragmentEntryPoint,
                variant = request.variant,
                extraDescriptorSetLayouts = request.extraDescriptorSetLayouts,
            )
            val wireframe = if (request.buildWireframe) {
                RenderPipeline(
                    graphicsDevice,
                    swapchainManager,
                    renderPass,
                    descriptorSetLayout,
                    shaders,
                    request.vertexFormat,
                    request.vertexEntryPoint,
                    request.fragmentEntryPoint,
                    polygonMode = VkPolygonMode.VK_POLYGON_MODE_LINE,
                    variant = request.variant,
                    extraDescriptorSetLayouts = request.extraDescriptorSetLayouts,
                )
            } else {
                null
            }
            val backCulled = if (request.buildBackCulled) {
                RenderPipeline(
                    graphicsDevice,
                    swapchainManager,
                    renderPass,
                    descriptorSetLayout,
                    shaders,
                    request.vertexFormat,
                    request.vertexEntryPoint,
                    request.fragmentEntryPoint,
                    cullMode = VkCullModeFlagBits.VK_CULL_MODE_BACK_BIT,
                    variant = request.variant,
                    extraDescriptorSetLayouts = request.extraDescriptorSetLayouts,
                )
            } else {
                null
            }
            put(request.key, RequestedPipelines(fill, wireframe, backCulled))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to build pipeline '${request.key}': ${e.message}", e)
        }
    }
}
