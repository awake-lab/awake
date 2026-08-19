// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderFrameContext
import io.github.ronjunevaldoz.awake.vulkan.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.pipeline.UiPipelineSet

/**
 * The one class bridging [RenderFrameContext] to real [Renderer] internals -- every member is a
 * one-line delegation, and this is the only file allowed to reference both sides together.
 * Cheap to allocate once per frame (holds references only, copies nothing).
 */
internal class RendererFrameContext(
    private val renderer: Renderer,
    override val commandBuffer: Long,
    override val frameIndex: Int,
    override val groupedDrawCalls: Map<RenderPipeline, List<PreparedDrawCall>>,
    override val primaryPipeline: RenderPipeline,
    override val viewProjection: Mat4,
    override val cameraEye: Vec3,
    override val light: SceneLight,
) : RenderFrameContext {
    override val showEnvironment get() = renderer.showEnvironment
    override val horizonColor get() = renderer.horizonColor
    override val zenithColor get() = renderer.zenithColor
    override val lineMesh get() = renderer.lineMesh
    override val uiRuns get() = renderer.uiRuns
    override val surfaceWidth get() = renderer.swapchainManager.extent.width
    override val surfaceHeight get() = renderer.swapchainManager.extent.height

    override fun recordDrawCalls(drawCalls: List<PreparedDrawCall>) =
        renderer.recordDrawCalls(commandBuffer, drawCalls)

    override fun uiPipelines(): UiPipelineSet? {
        val quad = renderer.uiRenderPipeline ?: return null
        return UiPipelineSet(
            quad = quad,
            glyph = renderer.uiGlyphRenderPipeline,
            texture = renderer.uiTextureRenderPipeline,
            roundedQuad = renderer.uiRoundedQuadRenderPipeline,
        )
    }

    override fun textureMeshForPrimitive(index: Int) = renderer.textureMeshForPrimitive(index)
}
