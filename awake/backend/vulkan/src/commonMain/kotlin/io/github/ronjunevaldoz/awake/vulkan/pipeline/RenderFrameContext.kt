// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.vulkan.debug.LineMesh
import io.github.ronjunevaldoz.awake.vulkan.renderer.PreparedDrawCall
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.vulkan.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiRoundedQuadRenderPipeline
import io.github.ronjunevaldoz.awake.vulkan.ui.UiTextureRenderPipeline

/**
 * Everything a [RenderFeature] is allowed to see of the frame being recorded -- deliberately
 * narrow, and deliberately free of any reference to `Renderer` itself, so a feature can be read
 * and tested without knowing that class exists. `Renderer` supplies one of these per frame
 * through the single adapter that bridges the two (`RendererFrameContext`).
 */
internal interface RenderFrameContext {
    val commandBuffer: Long
    val frameIndex: Int

    /** Computed once per frame by whoever builds this context -- every scene feature needs the
     * same grouping, so it is not recomputed per feature. */
    val groupedDrawCalls: Map<RenderPipeline, List<PreparedDrawCall>>
    val primaryPipeline: RenderPipeline

    /** This frame's camera matrices/light, for the features that write their own pipeline's
     * uniform block (debug lines, sky) before drawing with it. */
    val viewProjection: Mat4
    val cameraEye: Vec3
    val light: SceneLight

    val showEnvironment: Boolean
    val horizonColor: FloatArray
    val zenithColor: FloatArray

    /** Staged before the frame by `drawDebugLines`/`drawUi` respectively -- consumed here. */
    val lineMesh: LineMesh
    val uiRuns: List<Renderer.UiRun>

    /** The surface this pass draws into, for the UI clip run's defensive scissor clamp. */
    val surfaceWidth: Int
    val surfaceHeight: Int

    fun recordDrawCalls(drawCalls: List<PreparedDrawCall>)

    /** This frame's UI pipeline set, or `null` when nothing has ever called `drawUi` (the quad
     * pipeline is built lazily there, and rebuilt on resize) -- a method, not a field, because
     * the underlying pipelines are mutable and must be read fresh on every frame. */
    fun uiPipelines(): UiPipelineSet?

    /** Stateful pooled `DynamicMesh` allocator -- see `Renderer.textureMeshForPrimitive`'s own
     * doc comment for the pooling contract this preserves. */
    fun textureMeshForPrimitive(index: Int): DynamicMesh
}

/** The lazily built UI pipelines, snapshotted for one feature call. [quad] is non-null by
 * construction (it is what "UI exists this frame" means); the other three stay nullable, each
 * built only once a frame actually contains that kind of primitive. */
internal class UiPipelineSet(
    val quad: UiRenderPipeline,
    val glyph: UiGlyphRenderPipeline?,
    val texture: UiTextureRenderPipeline?,
    val roundedQuad: UiRoundedQuadRenderPipeline?,
)

/** Which of the two render passes `recordCommandBuffer` begins a feature belongs to. Not a
 * feature *hierarchy*: both slots share the exact same [RenderFeature] interface, this only
 * says which pass a feature's commands are valid inside. The shadow pass is absent on purpose
 * -- it owns its own render pass and is not a [RenderFeature] at all (see `ShadowFeature`). */
internal enum class RenderPassSlot { Scene, Ui }

/** One unit of drawing inside a render pass someone else begins and ends -- the 3D scene pass
 * (opaque geometry + debug lines, sky) and the UI overlay pass. Registration order in
 * `Renderer`'s feature list is paint order within a slot. */
internal interface RenderFeature {
    val pass: RenderPassSlot
    fun recordCommands(context: RenderFrameContext)
    fun destroy()
}
