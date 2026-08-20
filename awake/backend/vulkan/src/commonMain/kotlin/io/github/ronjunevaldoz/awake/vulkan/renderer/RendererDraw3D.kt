// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.command.PreparedDraw
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.passes.ui.writeLineVertex
import io.github.ronjunevaldoz.awake.render.passes.uniforms.fogUniformFloats
import io.github.ronjunevaldoz.awake.render.passes.uniforms.pbrMaterialFloats
import io.github.ronjunevaldoz.awake.render.passes.uniforms.pbrTexturedMaterialFloats
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.RenderViewport
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_FAR
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_NEAR
import io.github.ronjunevaldoz.awake.render.renderer.SHADOW_ORTHO_HALF_SIZE
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.renderer.UniformFields
import io.github.ronjunevaldoz.awake.render.renderer.directionalShadowBox
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.enums.VkCommandBufferLevel
import io.github.ronjunevaldoz.awake.vulkan.enums.VkResult
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkCommandBufferUsageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.enums.flags.VkPipelineStageFlagBits
import io.github.ronjunevaldoz.awake.vulkan.material.Material
import io.github.ronjunevaldoz.awake.vulkan.mesh.AlphaInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.mesh.FrameInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.mesh.InstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.mesh.Mesh
import io.github.ronjunevaldoz.awake.vulkan.mesh.SkinnedInstanceBuffer
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferAllocateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkCommandBufferBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkFenceCreateInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkPresentInfoKHR
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkSubmitInfo
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPassSlot
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.VulkanMaterialBinding
import io.github.ronjunevaldoz.awake.vulkan.utils.VkResultException
import kotlin.math.ceil
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial

/** The 3D frame path -- [Renderer.draw]'s whole-frame orchestration (wait/acquire -> update
 * uniforms -> record -> submit -> present), the shared per-draw-call recording loop, the
 * swapchain command-buffer recording (3D pass + UI overlay pass), the offscreen one-time-
 * command runner both [Renderer.renderToTexture]/[Renderer.readPixels] use, and debug-line
 * staging. See [Renderer]'s class doc comment for why this lives here as `internal` extension
 * functions rather than as members. */

/** Renders one frame: waits for this frame-in-flight slot, acquires a swapchain image,
 * prepares each [DrawCall]'s MVP matrix (model combined with [camera]'s view/projection)
 * into a concrete material uniform slot, records and submits a command buffer that draws
 * every call in order, then presents. Mutable per-frame resources (material uniforms,
 * debug-line uniforms/buffers, and UI dynamic meshes/descriptors) are indexed by the same
 * frame slot, so this path only waits that slot's fence rather than stalling the entire
 * device after every submit.
 *
 * Named `performDraw`, not `draw` -- [Renderer]'s `override fun draw(...)` (the actual
 * `RenderRenderer` interface method) is a one-line delegate to this extension function; an
 * extension function can't share its name with a member function it's called from without
 * the member call winning resolution and recursing into itself. Frame acquisition itself
 * is [acquireSwapchainImage] ([RendererSwapchainAcquire.kt]), split into its own file to
 * keep both detekt's method-length limit and this file's function-count limit. */
internal fun Renderer.performDraw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) {
    val currentFrame = swapchainManager.currentFrame
    val imageIndex = acquireSwapchainImage(currentFrame) ?: return

    val aspect = resolvedSceneViewport()?.aspect
        ?: (swapchainManager.extent.width.toFloat() / swapchainManager.extent.height.toFloat())
    val viewProjection = camera.viewProjectionMatrix(aspect, clipSpace)
    val lightViewProjection = if (shadowMap != null) lightViewProjection(light) else null
    val materialUsage = mutableMapOf<RenderMaterial, Int>()
    val preparedDrawCalls =
        prepareDrawCalls(
            currentFrame,
            viewProjection,
            drawCalls,
            light,
            lightViewProjection,
            materialUsage,
            cameraPosition = camera.eye,
        )

    recordShadowPass(preparedDrawCalls)

    Vulkan.vkResetCommandBuffer(commandBuffers[currentFrame], 0)
    recordCommandBuffer(
        commandBuffers[currentFrame],
        currentFrame,
        imageIndex,
        preparedDrawCalls,
        viewProjection,
        camera.eye,
        light,
    )

    val waitSemaphores = arrayOf(swapchainManager.imageAvailableSemaphores[currentFrame])
    val waitStages =
        intArrayOf(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value)
    val signalSemaphores = arrayOf(swapchainManager.renderFinishedSemaphores[imageIndex])

    val submitInfo = VkSubmitInfo(
        pWaitSemaphores = waitSemaphores,
        pWaitDstStageMask = waitStages,
        pCommandBuffers = arrayOf(commandBuffers[currentFrame]),
        pSignalSemaphores = signalSemaphores,
    )

    Vulkan.vkQueueSubmit(
        graphicsQueue,
        arrayOf(submitInfo),
        swapchainManager.inFlightFences[currentFrame],
    )

    val presentInfo = VkPresentInfoKHR(
        pWaitSemaphores = signalSemaphores,
        pSwapchains = arrayOf(swapchainManager.swapChain),
        pImageIndices = intArrayOf(imageIndex),
        pResults = VkResult.values(),
    )

    try {
        Vulkan.vkQueuePresentKHR(presentQueue, presentInfo)
    } catch (e: VkResultException) {
        when (e.result) {
            VkResult.VK_SUBOPTIMAL_KHR, VkResult.VK_ERROR_OUT_OF_DATE_KHR -> recreateSwapChain()
            else -> throw e
        }
    }

    swapchainManager.currentFrame = (currentFrame + 1) % commandBuffers.size
}

/** [Renderer.sceneViewport] trimmed to this frame's swapchain extent -- see
 * [RenderViewport.clampedTo] for why a caller's rect can outlive the surface it was measured
 * against. */
internal fun Renderer.resolvedSceneViewport(): RenderViewport? = sceneViewport?.clampedTo(
    swapchainManager.extent.width.toFloat(),
    swapchainManager.extent.height.toFloat(),
)

internal fun RenderViewport.toVkViewport(): VkViewport =
    VkViewport(x = x, y = y, width = width, height = height)

internal fun RenderViewport.toVkScissor(): VkRect2D = VkRect2D(
    offset = VkOffset2D(x.toInt(), y.toInt()),
    // Rounded up so a fractional edge does not scissor away the viewport's last column/row.
    extent = VkExtent2D(ceil(width).toInt(), ceil(height).toInt()),
)

/** Binds+draws each [drawCalls] entry through the shared per-draw recording loop, against
 * whatever pipeline the caller already bound -- [Renderer.renderToTexture]'s offscreen path,
 * which groups by pipeline and binds each group itself. The swapchain frame reaches the same
 * loop through `SharedOpaqueRenderFeature.recordCommands` instead. */
internal fun Renderer.recordDrawCalls(commandBuffer: Long, drawCalls: List<PreparedDrawCall>) {
    commandRecorder.commandBuffer = commandBuffer
    sharedOpaqueFeature.recordDraws(commandRecorder, drawCalls)
}

/** Records the frame's two passes: the 3D scene pass, then either the UI overlay pass or the
 * bare present-transition pass. What is drawn *inside* each pass is the registered
 * [io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderFeature]s' job (dispatched through
 * [Renderer.recordSharedPassFeatures]); choosing and beginning/ending the passes stays here. */
internal fun Renderer.recordCommandBuffer(
    commandBuffer: Long,
    frameIndex: Int,
    acquiredImageIndex: Int,
    drawCalls: List<PreparedDrawCall>,
    viewProjection: Mat4,
    cameraEye: Vec3,
    light: SceneLight,
) {
    val beginInfo = VkCommandBufferBeginInfo(
        flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value,
    )
    Vulkan.vkBeginCommandBuffer(commandBuffer, beginInfo)

    val renderPassInfo = VkRenderPassBeginInfo(
        renderPass = renderPipeline.renderPass,
        framebuffer = framebuffers[acquiredImageIndex],
        renderArea = VkRect2D(
            extent = swapchainManager.extent,
        ),
        pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue),
    )
    Vulkan.vkCmdBeginRenderPass(
        commandBuffer,
        renderPassInfo,
        VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE,
    )

    // Grouped by resolved pipeline: primary group first, then debug lines, then every other
    // format's group. Draws within each group are clustered by mesh to maximize vertex and index
    // buffer binding reuse across consecutive calls in SharedOpaqueRenderFeature.
    val groupedDrawCalls = drawCalls
        .groupBy { it.pipeline }
        .mapValues { (_, draws) ->
            draws.sortedBy { it.drawCall.mesh.hashCode() }
        }
    val primaryPipeline = pipelineFor(renderPipeline.vertexFormat) ?: renderPipeline
    val context = RendererFrameContext(
        renderer = this,
        commandBuffer = commandBuffer,
        frameIndex = frameIndex,
        groupedDrawCalls = groupedDrawCalls,
        primaryPipeline = primaryPipeline,
        viewProjection = viewProjection,
        cameraEye = cameraEye,
        light = light,
    )
    // Full-surface pair: what the UI pass below restores, and what the 3D pass uses when no
    // scene viewport is set.
    val viewport = VkViewport(
        width = swapchainManager.extent.width.toFloat(),
        height = swapchainManager.extent.height.toFloat(),
    )
    val scissor = VkRect2D(
        extent = swapchainManager.extent,
    )
    // Set once for the whole 3D pass -- debug lines and the non-primary format groups recorded
    // further down share it, since they are scene content too.
    val sceneRect = resolvedSceneViewport()
    Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(sceneRect?.toVkViewport() ?: viewport))
    Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(sceneRect?.toVkScissor() ?: scissor))

    // Sky (first, depth test/write off), then opaque geometry + debug lines -- registration
    // order in the feature list is paint order.
    recordSharedPassFeatures(RenderPassSlot.Scene, context)

    Vulkan.vkCmdEndRenderPass(commandBuffer)

    // Second pass, same command buffer, on top of the 3D output: the 3D pass's finalLayout
    // leaves the image in COLOR_ATTACHMENT_OPTIMAL for this pass to pick up. Only recorded
    // once drawUi() has built a UI pipeline; mirrors WebGPU's equivalent guard.
    val uiPipeline = uiRenderPipeline
    if (uiPipeline != null) {
        val uiRenderPassInfo = VkRenderPassBeginInfo(
            renderPass = uiPipeline.renderPass,
            framebuffer = uiFramebuffers[acquiredImageIndex],
            renderArea = VkRect2D(extent = swapchainManager.extent),
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            uiRenderPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE,
        )
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))

        recordSharedPassFeatures(RenderPassSlot.Ui, context)

        Vulkan.vkCmdEndRenderPass(commandBuffer)
    } else {
        val presentTransitionPassInfo = VkRenderPassBeginInfo(
            renderPass = presentTransitionRenderPass,
            framebuffer = presentTransitionFramebuffers[acquiredImageIndex],
            renderArea = VkRect2D(extent = swapchainManager.extent),
        )
        Vulkan.vkCmdBeginRenderPass(
            commandBuffer,
            presentTransitionPassInfo,
            VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE,
        )
        Vulkan.vkCmdEndRenderPass(commandBuffer)
    }

    Vulkan.vkEndCommandBuffer(commandBuffer)
}

/** Waits until the current frame-in-flight slot is no longer referenced by the GPU before
 * CPU code rewrites host-visible resources assigned to that slot. This is intentionally much
 * narrower than `vkDeviceWaitIdle`: other submitted frame slots may continue running. */
internal fun Renderer.waitForCurrentFrameResourceSlot() {
    val currentFrame = swapchainManager.currentFrame
    val fence = swapchainManager.inFlightFences.getOrNull(currentFrame) ?: return
    if (fence == 0L) return
    Vulkan.vkWaitForFences(device, longArrayOf(fence), true, Long.MAX_VALUE)
}

/**
 * Also this backend's [PreparedDraw]: the port's members are computed off the fields already
 * here, so the shared opaque feature iterates these directly instead of a second per-draw object
 * being built for it every frame. Every getter resolves to an object created at
 * resource-creation time (a material's uniform slot, a mesh's buffer binding), so reading one
 * allocates nothing.
 */
internal data class PreparedDrawCall(
    val drawCall: DrawCall,
    override val pipeline: RenderPipeline,
    val material: Material,
    val frameIndex: Int,
    val uniformSlotIndex: Int,
    /** The descriptor set `prepareDrawCalls` just wrote this draw's uniforms into. */
    override val materialBinding: VulkanMaterialBinding,
    /** Non-null only for a [DrawCall.instanceModels] draw resolved to an instanced pipeline --
     * [recordDrawCalls] then binds it and issues one `drawInstanced` instead of `draw`. */
    val instanceBuffer: InstanceBuffer? = null,
    val instanceCount: Int = 0,
    /** Non-null only for an ANIMATED instanced draw ([DrawCall.instanceJointPalettes]), where it
     * accompanies [instanceBuffer] -- per-instance model matrices still come through that. */
    val jointPaletteBuffer: SkinnedInstanceBuffer? = null,
    /** Non-null only for a billboard-particle instanced draw ([DrawCall.instanceColors]),
     * bound at binding 2 alongside [instanceBuffer]'s binding 1. */
    val alphaInstanceBuffer: AlphaInstanceBuffer? = null,
    /** Non-null only for a billboard-particle instanced draw ([DrawCall.instanceFrames]), bound
     * at binding 3 alongside [alphaInstanceBuffer]'s binding 2. */
    val frameInstanceBuffer: FrameInstanceBuffer? = null,
) : PreparedDraw {
    /** Safe for the same reason `prepareDrawCalls`' own `drawCall.material as Material` is: a
     * `Renderer` only ever draws meshes it created itself. */
    private val vulkanMesh: Mesh get() = drawCall.mesh as Mesh

    override val vertexBuffer get() = vulkanMesh.vertexBinding
    override val indexBuffer get() = vulkanMesh.indexBinding
    override val elementCount get() = vulkanMesh.indexCount

    /** [instanceCount] is 0 for a non-instanced draw; the port's count is the real one Vulkan
     * issues, which is 1 there. */
    override val instances get() = if (instanceBuffer == null) 1 else instanceCount
    override val instanceVertexBuffer get() = instanceBuffer?.binding(frameIndex)
    override val jointPaletteBinding get() = jointPaletteBuffer?.binding(frameIndex)
    override val instanceColorBuffer get() = alphaInstanceBuffer?.binding(frameIndex)
    override val instanceFrameBuffer get() = frameInstanceBuffer?.binding(frameIndex)
}

/** Resolves each [drawCalls] entry against [Renderer.pipelinesByFormat] by its
 * [DrawCall.mesh]'s own [io.github.ronjunevaldoz.awake.render.mesh.Mesh.format] and writes
 * its uniform buffer -- one shared pass for every vertex format a [Renderer] can draw,
 * replacing what used to be a separate `prepare*DrawCalls` function (and a separate
 * `Prepared*DrawCall` type) per format. A [drawCall] whose format has no entry in
 * [Renderer.pipelinesByFormat] is SKIPPED, not drawn through [Renderer.renderPipeline] as a
 * fallback -- rendering one format's vertex data through a different format's pipeline would
 * silently misinterpret the vertex buffer (wrong attribute count/offsets), which is worse
 * than not drawing it.
 *
 * [light] reaches [drawCall]s resolved to [Renderer.renderPipeline] itself (the primary/lit
 * format) and to the `PositionNormalColorUv` (textured/PBR) format; every other resolved
 * pipeline gets [DrawCall.extraUniformFloats] instead (e.g. a skinned mesh's joint palette),
 * exactly matching what each format's own shader expects (`triangle.wgsl`/`textured.wgsl`
 * read light, `skinned.wgsl` doesn't).
 *
 * [lightViewProjection] is `null` for every [Renderer] not built with shadow support (see
 * [Renderer.shadowMap]'s doc comment) -- in that case the written uniform buffer is byte-for-
 * byte identical to before shadows existed (mvp + 8 light floats). Non-null only appends 16
 * more floats (`drawCall.model * lightViewProjection`, same "Kotlin order gives the
 * conventional product" convention as `mvp` itself) for [Renderer.renderPipeline]-resolved
 * calls, matching `lit_shadow.wgsl`'s `Uniforms.lightMvp`. */
internal fun Renderer.prepareDrawCalls(
    frameIndex: Int,
    viewProjection: Mat4,
    drawCalls: List<DrawCall>,
    light: SceneLight,
    lightViewProjection: Mat4? = null,
    materialUsage: MutableMap<RenderMaterial, Int> = mutableMapOf(),
    cameraPosition: Vec3 = Vec3.ZERO,
): List<PreparedDrawCall> {
    val prepared = ArrayList<PreparedDrawCall>(drawCalls.size)
    // `vec4f` (not `vec3f`) for both fields on the WGSL side specifically to sidestep vec3's
    // implicit 16-byte alignment padding in a uniform-buffer struct -- see triangle.wgsl's own
    // Uniforms struct doc comment. The unused 4th float of each is simply never read there.
    val lightFloats = floatArrayOf(
        light.direction.x,
        light.direction.y,
        light.direction.z,
        shadowTexelDepthScale(),
        light.color.x,
        light.color.y,
        light.color.z,
        0f,
    )
    var drawIndex = 0
    var instancedIndex = 0
    while (drawIndex < drawCalls.size) {
        val drawCall = drawCalls[drawIndex]
        val instanceModels = drawCall.instanceModels
        if (instanceModels != null) {
            // A separate path entirely: one uniform write and one GPU call for every transform,
            // instead of this loop's per-draw-call MVP. Null (no instanced pipeline for this
            // format, or nothing to draw) skips the call, same as an unresolved format below.
            // Particles carry their own camera-right/camera-up basis (particle.wgsl's
            // Uniforms) instead of the scene light -- every other instanced format keeps the
            // viewProjection+light block (instanced.wgsl/skinned_instanced.wgsl's Uniforms).
            val isParticle = drawCall.mesh.format == VertexFormat.PositionUv
            val instancedUniformFloats = if (isParticle) {
                viewProjection.data + drawCall.extraUniformFloats
            } else {
                viewProjection.data + lightFloats
            }
            val instanced = prepareInstancedDrawCall(
                drawCall,
                frameIndex,
                instancedIndex,
                instancedUniformFloats,
                materialUsage,
            )
            if (instanced != null) {
                prepared += instanced
                instancedIndex += 1
            } else if (debugMode) {
                val animated = drawCall.instanceJointPalettes != null
                println(
                    "Awake (Vulkan): instanced DrawCall skipped -- no ${if (animated) "skinned-" else ""}" +
                        "instanced pipeline registered for mesh format ${drawCall.mesh.format}, " +
                        "or instanceModels was empty.",
                )
            }
            drawIndex += 1
            continue
        }
        // pipelineFor, not a direct pipelinesByFormat lookup: swaps in this format's
        // VK_POLYGON_MODE_LINE variant when Renderer.wireframe is on and one was built for
        // it, or its VK_CULL_MODE_BACK_BIT variant when drawCall.cullMode asks for it (see
        // that function's doc comment) -- an unregistered format still resolves to null and is
        // skipped below, same as before either parameter existed.
        val pipeline = pipelineFor(drawCall.mesh.format, drawCall.cullMode)
        if (pipeline != null) {
            val material = drawCall.material as Material
            val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
            // Kotlin's `A * B` computes the conventional `B * A` (see Mat4.times/
            // Camera.viewProjectionMatrix's docs), so `model * viewProjection` (Kotlin
            // order) gives the conventional `projection * view * model`.
            val mvp = drawCall.model * viewProjection
            // Compared by FORMAT, not pipeline identity: wireframe's pipelineFor can resolve the
            // primary lit format to a different pipeline object than renderPipeline itself.
            val extraFloats =
                if (drawCall.mesh.format == renderPipeline.vertexFormat) {
                    if (lightViewProjection != null) {
                        // Order matches lit_shadow.wgsl's Uniforms field order exactly.
                        lightFloats +
                            (drawCall.model * lightViewProjection).data +
                            drawCall.model.data +
                            floatArrayOf(
                                cameraPosition.x,
                                cameraPosition.y,
                                cameraPosition.z,
                                0f,
                            ) +
                            pbrMaterialFloats(drawCall) +
                            fogFloats()
                    } else {
                        lightFloats
                    }
                } else if (drawCall.mesh.format == VertexFormat.PositionNormalColorUv) {
                    // textured.wgsl's Uniforms: light, then model + cameraPosition (its PBR
                    // specular needs a world-space position and view vector), then the glTF
                    // material factors. Keyed by format rather than by pipeline identity for
                    // the same reason the branch above is.
                    lightFloats +
                        drawCall.model.data +
                        floatArrayOf(cameraPosition.x, cameraPosition.y, cameraPosition.z, 0f) +
                        pbrTexturedMaterialFloats(drawCall) +
                        fogFloats()
                } else {
                    drawCall.extraUniformFloats
                }
            val binding =
                material.updateUniformBuffer(frameIndex, uniformSlotIndex, mvp.data + extraFloats)
            prepared += PreparedDrawCall(
                drawCall,
                pipeline,
                material,
                frameIndex,
                uniformSlotIndex,
                binding,
            )
        } else if (debugMode) {
            println(
                "Awake (Vulkan): DrawCall skipped -- no pipeline registered for mesh format " +
                    "${drawCall.mesh.format}.",
            )
        }
        drawIndex += 1
    }
    return prepared
}

/** One [DrawCall] carrying [instanceModels] resolved against
 * [Renderer.instancedPipelinesByFormat], or `null` when this format has no instanced pipeline
 * (or there is nothing to draw) -- see that field's own doc comment for why an unresolved
 * format is skipped rather than force-drawn.
 *
 * The uniform write is what genuinely differs from the non-instanced path: `instanced.wgsl`'s
 * `Uniforms` holds `viewProjection` + light, NOT a per-draw `mvp`, since one call covers many
 * model matrices and none of them can be folded in on the CPU. It's still written into
 * [DrawCall.material]'s own frame/draw slot (not a pipeline-owned buffer): the block is the same
 * 24 floats the non-shadow lit path already writes there, so no second uniform/descriptor scheme
 * is needed for it. [uniformFloats] is already the caller's fully-assembled block (particle vs.
 * light-based content decided by the caller, which already branches on [DrawCall.mesh]'s
 * format) -- this function just writes it, it doesn't need to know which case it is.
 *
 * A call that also carries [DrawCall.instanceJointPalettes] resolves against
 * [Renderer.skinnedInstancedPipelinesByFormat] instead and also
 * fills a [SkinnedInstanceBuffer] with this call's per-instance joint palettes -- everything
 * else (uniform block, model-matrix instance buffer, draw call) is identical, which is why
 * this stayed one function rather than a near-duplicate second one. */
private fun Renderer.prepareInstancedDrawCall(
    drawCall: DrawCall,
    frameIndex: Int,
    instancedIndex: Int,
    uniformFloats: FloatArray,
    materialUsage: MutableMap<RenderMaterial, Int>,
): PreparedDrawCall? {
    val animated = drawCall.instanceJointPalettes != null
    val isParticle = drawCall.mesh.format == VertexFormat.PositionUv
    val pipeline = when {
        animated -> skinnedInstancedPipelinesByFormat[drawCall.mesh.format]
        else -> instancedPipelinesByFormat[drawCall.mesh.format]
    }
    val instanceModels = drawCall.instanceModels.orEmpty()
    if (pipeline == null || instanceModels.isEmpty()) return null
    val material = drawCall.material as Material
    val uniformSlotIndex = materialUsage.nextSlot(drawCall.material)
    val instanceBuffer = instanceBufferForRun(instancedIndex)
    instanceBuffer.update(frameIndex, instanceModels)
    val jointPaletteBuffer = if (animated) {
        skinnedInstanceBufferForRun(instancedIndex).also {
            it.update(frameIndex, drawCall.instanceJointPalettes.orEmpty())
        }
    } else {
        null
    }
    val alphaInstanceBuffer = if (isParticle) {
        alphaInstanceBufferForRun(instancedIndex).also {
            it.update(frameIndex, drawCall.instanceColors.orEmpty())
        }
    } else {
        null
    }
    val frameInstanceBuffer = if (isParticle) {
        frameInstanceBufferForRun(instancedIndex).also {
            it.update(frameIndex, drawCall.instanceFrames.orEmpty())
        }
    } else {
        null
    }
    val binding = material.updateUniformBuffer(frameIndex, uniformSlotIndex, uniformFloats)
    return PreparedDrawCall(
        drawCall = drawCall,
        pipeline = pipeline,
        material = material,
        frameIndex = frameIndex,
        uniformSlotIndex = uniformSlotIndex,
        materialBinding = binding,
        instanceBuffer = instanceBuffer,
        instanceCount = instanceModels.size,
        jointPaletteBuffer = jointPaletteBuffer,
        alphaInstanceBuffer = alphaInstanceBuffer,
        frameInstanceBuffer = frameInstanceBuffer,
    )
}

/** The NDC depth one shadow-map texel of lateral travel spans on a 45-degree surface --
 * `lit_shadow.wgsl` scales its depth bias by this so its constants can be written in texels.
 * Computed here because this is where the light frustum's depth range and the map size both
 * live; a copy of them in the shader is how a bias silently ends up meaning ~40x more
 * world-space offset than it reads, which detaches the shadow from its caster. 0 with no shadow
 * map, where nothing reads it. */
private fun Renderer.shadowTexelDepthScale(): Float {
    val map = shadowMap ?: return 0f
    return (2f * SHADOW_ORTHO_HALF_SIZE / map.size) / (SHADOW_FAR - SHADOW_NEAR)
}

private fun Renderer.fogFloats(): FloatArray = fogUniformFloats(fogColor, fogDensity)

private fun MutableMap<RenderMaterial, Int>.nextSlot(material: RenderMaterial): Int {
    val slot = this[material] ?: 0
    this[material] = slot + 1
    return slot
}

/** The directional light's own view-projection, built the same "view * projection" (Kotlin
 * operator order) way [Camera.viewProjectionMatrix] builds a real camera's -- an orthographic
 * projection instead of a perspective one (correct for a directional/parallel-rays light, and
 * simpler than fitting a perspective frustum to one). The box is a FIXED size centered on the
 * world origin, not derived from actual scene bounds or the active camera's frustum.
 * ponytail: fixed shadow-box centered at the origin, not scene/camera-fit; upgrade path is a
 * per-frame bounding-box (or camera-frustum) fit once a demo's content moves far from origin. */
internal fun Renderer.lightViewProjection(light: SceneLight): Mat4 =
    directionalShadowBox(light.direction, clipSpace).viewProjection

/** [Renderer.renderToTexture]/[Renderer.readPixels]'s own one-time-command runner -- see
 * [Renderer.offscreenCommandBuffer]'s doc comment for why this doesn't use
 * `transferContext.runOneTimeCommands`. Allocates its command buffer/fence once, on
 * first use, then resets and reuses both every call after that. */
internal fun Renderer.runOffscreenCommands(block: (Long) -> Unit) {
    if (offscreenCommandBuffer == 0L) {
        offscreenCommandBuffer = Vulkan.vkAllocateCommandBuffers(
            device,
            VkCommandBufferAllocateInfo(
                commandPool = transferContext.commandPool.handle,
                level = VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY,
                commandBufferCount = 1,
            ),
        )
        offscreenFence = Vulkan.vkCreateFence(device, VkFenceCreateInfo())
    }
    Vulkan.vkResetCommandBuffer(offscreenCommandBuffer, 0)
    Vulkan.vkBeginCommandBuffer(
        offscreenCommandBuffer,
        VkCommandBufferBeginInfo(flags = VkCommandBufferUsageFlagBits.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT.value),
    )
    block(offscreenCommandBuffer)
    Vulkan.vkEndCommandBuffer(offscreenCommandBuffer)

    Vulkan.vkResetFences(device, longArrayOf(offscreenFence))
    Vulkan.vkQueueSubmit(
        graphicsQueue,
        arrayOf(VkSubmitInfo(pCommandBuffers = arrayOf(offscreenCommandBuffer))),
        offscreenFence,
    )
    Vulkan.vkWaitForFences(device, longArrayOf(offscreenFence), true, Long.MAX_VALUE)
}

/** Stages this frame's world-space debug lines (e.g. a frustum wireframe) -- rewrites
 * [Renderer.lineMesh]'s buffer but issues no GPU commands itself, same "stage now, consume on
 * next draw" pattern as `drawUi`. Call before [performDraw] each frame.
 *
 * Named `performDrawDebugLines`, not `drawDebugLines` -- see [performDraw]'s doc comment for
 * why. */
internal fun Renderer.performDrawDebugLines(lines: List<LineSegment>) {
    waitForCurrentFrameResourceSlot()
    require(lines.size <= Renderer.MAX_DEBUG_LINES) {
        "Debug line count (${lines.size}) exceeds Renderer's LineMesh capacity (${Renderer.MAX_DEBUG_LINES})."
    }
    val vertices = FloatArray(lines.size * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX)
    var lineIndex = 0
    while (lineIndex < lines.size) {
        val line = lines[lineIndex]
        val vertexBase = lineIndex * LineMesh.VERTICES_PER_LINE * LineMesh.FLOATS_PER_VERTEX
        writeLineVertex(vertices, vertexBase, line.start.x, line.start.y, line.start.z, line.color)
        writeLineVertex(
            vertices,
            vertexBase + LineMesh.FLOATS_PER_VERTEX,
            line.end.x,
            line.end.y,
            line.end.z,
            line.color,
        )
        lineIndex += 1
    }
    lineMesh.update(swapchainManager.currentFrame, vertices)
}
