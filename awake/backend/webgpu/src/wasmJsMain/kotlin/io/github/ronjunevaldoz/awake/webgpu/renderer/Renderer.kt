// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.colors.Color as AwakeColor
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.times
import io.github.ronjunevaldoz.awake.render.material.Material as RenderMaterial
import io.github.ronjunevaldoz.awake.render.mesh.Mesh as RenderMesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DEFAULT_SCENE_LIGHT
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer as RenderRenderer
import io.github.ronjunevaldoz.awake.render.renderer.SceneLight
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.webgpu.debug.LineMesh
import io.github.ronjunevaldoz.awake.webgpu.fastArrayBufferOf
import io.github.ronjunevaldoz.awake.webgpu.debug.LineRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.device.GraphicsDevice
import io.github.ronjunevaldoz.awake.webgpu.material.Material
import io.github.ronjunevaldoz.awake.webgpu.mesh.Mesh
import io.github.ronjunevaldoz.awake.webgpu.mesh.meshIndexFormat
import io.github.ronjunevaldoz.awake.webgpu.pipeline.RenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.swapchain.SwapchainManager
import io.github.ronjunevaldoz.awake.webgpu.texture.OffscreenRenderTarget
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.webgpu.ui.UiGlyphRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiRoundedQuadRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.ui.UiTextureRenderPipeline
import io.github.ronjunevaldoz.awake.webgpu.WebGpuHandles
import io.ygdrasil.webgpu.BufferDescriptor
import io.ygdrasil.webgpu.Color as GpuColor
import io.ygdrasil.webgpu.Extent3D
import io.ygdrasil.webgpu.GPUBindGroup
import io.ygdrasil.webgpu.GPUBuffer
import io.ygdrasil.webgpu.GPUBufferUsage
import io.ygdrasil.webgpu.GPULoadOp
import io.ygdrasil.webgpu.GPUMapMode
import io.ygdrasil.webgpu.GPURenderPipeline
import io.ygdrasil.webgpu.GPUStoreOp
import io.ygdrasil.webgpu.RenderPassColorAttachment
import io.ygdrasil.webgpu.RenderPassDepthStencilAttachment
import io.ygdrasil.webgpu.RenderPassDescriptor
import io.ygdrasil.webgpu.SamplerDescriptor
import io.ygdrasil.webgpu.TexelCopyBufferInfo
import io.ygdrasil.webgpu.TexelCopyTextureInfo
import io.ygdrasil.webgpu.beginRenderPass

/**
 * Phase 2.5 milestone 2 slice 1 (see docs/MVP_PLAN.md): real wgpu4k implementation of a
 * single triangle/cube draw. No fences/semaphores/frame-in-flight bookkeeping -- the
 * browser's own frame pacing replaces what `SwapchainManager`'s Vulkan sync fields are for.
 *
 * [DrawCall.material] is deliberately **not** touched here -- `Material`'s wasmJs actual is
 * still `TODO()` (out of scope for this slice, see docs/MVP_PLAN.md). Instead this class
 * owns one small uniform buffer + bind group directly per [RenderPipeline] (matching how
 * wgpu4k's own example scenes manage their uniform buffer, with no separate "Material"
 * abstraction), rewritten via `queue.writeBuffer` before each draw call. This only actually
 * works correctly for a single draw call per frame today -- multiple draw calls sharing one
 * uniform buffer within one render pass would clobber each other's MVP matrix, since
 * `queue.writeBuffer` is a queue-scheduled op, not something that interleaves mid-encoder.
 * Real per-draw-call (or per-Material) uniform buffers are Material's job once it's real.
 *
 * This class is deliberately just the class body (fields, constructor, the 3D resource API,
 * and [destroy]) -- the rest of its behavior lives in sibling files as `internal` extension
 * functions on `Renderer`, all in this same package: [RendererUiPipelines.kt] (lazy UI
 * pipeline construction), [RendererDraw3D.kt] (the 3D frame path + debug lines),
 * [RendererDrawUi.kt] (UI primitive staging), and [RendererVertexWriters.kt] (pure
 * vertex-buffer writers). Every field a moved function touches is `internal`, not `private`,
 * to stay accessible from those extension files -- `internal` stays module-scoped
 * (`awake:backend:webgpu` only), so this is not a real encapsulation loss.
 */
class Renderer(
    graphicsDevice: GraphicsDevice,
    swapchainManager: SwapchainManager,
    renderPipeline: RenderPipeline,
    /** The one [io.github.ronjunevaldoz.awake.render.mesh.VertexFormat] [renderPipeline] was
     * built for -- this backend only ever has the one 3D pipeline (see this class's own doc
     * comment), so a [DrawCall] whose mesh uses any other format is skipped rather than drawn
     * through a pipeline that expects a different vertex layout (see [performDraw]'s doc
     * comment). Mirrors Vulkan's `Renderer.pipelinesByFormat`, minus the "more than one
     * pipeline" part -- no format table needed when there's only ever one entry. */
    internal val primaryVertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    internal val lineRenderPipeline: LineRenderPipeline,
    internal val uiShaderCode: ByteArray,
    internal val uiGlyphShaderCode: ByteArray,
    internal val uiTextureShaderCode: ByteArray,
    internal val uiRoundedQuadShaderCode: ByteArray,
    commandPool: Long,
    maxFramesInFlight: Int,
    /** A `GPUPrimitiveTopology.LineList` companion of [renderPipeline] (same shader/vertex
     * layout, drawn via each mesh's own derived [io.github.ronjunevaldoz.awake.webgpu.mesh
     * .Mesh.lineIndexBuffer] instead of its triangle index buffer) -- see [wireframe]'s doc
     * comment for the "why LineList, not a barycentric shader" rationale. `null` (default)
     * for every game that doesn't opt into `WebGpuGameApplication`'s `wireframeSupport`. */
    internal val wireframeRenderPipeline: RenderPipeline? = null
) : RenderRenderer {
    // WebGPU's NDC has +Y up -- confirmed by this module's own ui_quad.wgsl comment
    // ("pixel-space is Y-down, NDC is Y-up") -- so unlike Vulkan (+Y down NDC) no flip is
    // needed here. Depth is 0..1 on both, unlike OpenGL's -1..1.
    override val clipSpace: ClipSpace = ClipSpace.WebGpu

    override var clearColor: FloatArray = floatArrayOf(0f, 0f, 0f, 1f)

    /** See [wireframeRenderPipeline]'s doc comment. `false` by default -- and a no-op even
     * when set `true` if [wireframeRenderPipeline] was never built (this backend's
     * `wireframeSupport` opt-out), same "flag with nothing to switch to just stays filled"
     * shape as Vulkan's `Renderer.pipelineFor`. WebGPU has no `VK_POLYGON_MODE_LINE`
     * equivalent -- topology is fixed per pipeline, not a per-draw-call rasterizer setting --
     * so this backend swaps the whole bound pipeline (`renderPipeline` <-> [wireframeRenderPipeline])
     * and each mesh's index buffer (triangle indices <-> its own derived line-index buffer,
     * see `mesh.Mesh`'s doc comment) instead of a barycentric-coordinate fragment shader, which
     * would need every mesh re-authored with a duplicated, non-indexed vertex buffer just to
     * carry a per-vertex barycentric attribute. */
    override var wireframe: Boolean = false

    /** [clearColor] converted to this backend's clear-value type -- see the Vulkan `Renderer`'s
     * own `clearColorValue` for why this is a fresh-read `get()`, not a cached field. */
    internal val clearColorValue: GpuColor
        get() = GpuColor(clearColor[0].toDouble(), clearColor[1].toDouble(), clearColor[2].toDouble(), clearColor[3].toDouble())

    internal val graphicsDevice = graphicsDevice
    internal val swapchainManager = swapchainManager
    internal val renderPipeline = renderPipeline

    internal var uniformBuffer: GPUBuffer? = null
    internal var uniformBindGroup: GPUBindGroup? = null

    /** [wireframeRenderPipeline]'s own uniform buffer/bind group -- kept separate from
     * [uniformBuffer]/[uniformBindGroup], not shared: WebGPU's "auto" pipeline layout derives
     * a fresh, pipeline-specific `GPUBindGroupLayout` per `createRenderPipeline` call, even
     * when two pipelines share identical WGSL source, and a bind group is only valid against
     * the exact layout it was created from -- see [ensureWireframeUniformResources]'s doc
     * comment ([RendererUiPipelines.kt]). */
    internal var wireframeUniformBuffer: GPUBuffer? = null
    internal var wireframeUniformBindGroup: GPUBindGroup? = null

    // Lazily built on the first drawUi() call of any kind (uiRenderPipeline) and on the
    // first call that passes a non-null font (uiGlyphRenderPipeline) -- see
    // ensureUiQuadPipeline()/ensureGlyphPipeline()'s doc comments. A game that never calls
    // drawUi never builds either pipeline at all.
    internal var uiRenderPipeline: UiRenderPipeline? = null
    internal var uiGlyphRenderPipeline: UiGlyphRenderPipeline? = null
    internal var currentUiFont: UiFont? = null

    // Lazily built on the first drawUi() call that has any Texture primitives -- see
    // ensureTextureQuadPipeline()'s doc comment.
    internal var uiTextureRenderPipeline: UiTextureRenderPipeline? = null

    // Lazily built on the first drawUi() call that has any RoundedQuad primitives outside an
    // active convex-path clip -- see ensureRoundedQuadPipeline()'s doc comment. Mirrors
    // Vulkan's Renderer.uiRoundedQuadRenderPipeline (same lazy-pay-only-if-used pattern).
    internal var uiRoundedQuadRenderPipeline: UiRoundedQuadRenderPipeline? = null

    // One DynamicMesh per contiguous same-type run in a frame's primitive list (see
    // drawUi()'s run-coalescing) rather than one mesh per type -- mirrors Vulkan's
    // Renderer.kt (same file-level bug-fix rationale: a fixed "all quads, then all glyphs,
    // then all textures" pass order can only ever draw every glyph after every quad,
    // regardless of the source list's paint order). Grown on demand, reused every frame.
    internal val uiQuadMeshPool = mutableListOf<DynamicMesh>()
    internal val uiGlyphMeshPool = mutableListOf<DynamicMesh>()
    internal val uiRoundedQuadMeshPool = mutableListOf<DynamicMesh>()
    internal val textureQuadMesh = DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)

    /** One coalesced same-type run of a frame's UI primitives, in original paint order --
     * see `performDrawUi`'s doc comment (in [RendererDrawUi.kt]) for why runs (not "all
     * quads, then all glyphs") are needed. */
    internal sealed class UiRun {
        class QuadRun(val mesh: DynamicMesh) : UiRun()
        class RoundedQuadRun(val mesh: DynamicMesh) : UiRun()
        class GlyphRun(val mesh: DynamicMesh) : UiRun()
        class TextureRun(val primitives: List<TexturedPrimitiveRun>) : UiRun()

        /** Not a real draw call -- [rect] is already fully resolved (see [UiContext]'s clip
         * stack), so consuming this just means "set the scissor to this rect" at the point
         * in the command sequence where it was originally emitted, same as any other run. */
        class ClipRun(val rect: UiBounds) : UiRun()
    }

    internal data class TexturedPrimitiveRun(
        val material: Any,
        val vertices: FloatArray,
        val indices: IntArray
    )

    /** This frame's runs, in paint order -- staged by `performDrawUi`, consumed by
     * `performDraw` (both in sibling files, see this class's doc comment). */
    internal var uiRuns: List<UiRun> = emptyList()

    internal fun quadMeshForRun(index: Int): DynamicMesh {
        while (uiQuadMeshPool.size <= index) uiQuadMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS)
        return uiQuadMeshPool[index]
    }

    internal fun glyphMeshForRun(index: Int): DynamicMesh {
        while (uiGlyphMeshPool.size <= index) {
            uiGlyphMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
        }
        return uiGlyphMeshPool[index]
    }

    internal fun roundedQuadMeshForRun(index: Int): DynamicMesh {
        while (uiRoundedQuadMeshPool.size <= index) {
            uiRoundedQuadMeshPool += DynamicMesh(graphicsDevice, MAX_UI_QUADS, DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX)
        }
        return uiRoundedQuadMeshPool[index]
    }

    // Rewritten by performDrawDebugLines() (staged before draw(), same pattern as uiMesh
    // above).
    internal val lineMesh = LineMesh(graphicsDevice, MAX_DEBUG_LINES)

    /** Uploads [geometry] as a GPU mesh, on demand -- see [RenderRenderer.createMesh]'s doc
     * comment. `runOneTimeCommands` is unused by this backend's `Mesh` (see its own doc
     * comment), so an empty lambda is passed. */
    override fun createMesh(geometry: MeshGeometry): RenderMesh =
        Mesh(graphicsDevice, {}, geometry.vertices, geometry.indices, geometry.format)

    /** Builds a [Material] -- this backend's `Material` is still a compile-only stub (see
     * its own doc comment), so this just constructs it, matching this backend's existing
     * (pre-refactor) behavior of never calling `createResources`. [texture]/[renderTarget]
     * are accepted for interface parity with the Vulkan backend but otherwise unused here. */
    override fun createMaterial(texture: TextureAsset?, renderTarget: RenderTarget?, uniformFloatCount: Int): RenderMaterial {
        require(texture == null || renderTarget == null) { "Pass at most one of texture/renderTarget." }
        val material = Material(graphicsDevice, uniformFloatCount)
        if (renderTarget != null) {
            val offscreen = renderTarget as OffscreenRenderTarget
            val sampler = graphicsDevice.wgpuContext.device.createSampler(SamplerDescriptor())
            material.createResourcesFromRenderTarget(offscreen.colorView, sampler)
        }
        return material
    }

    // RenderTargets created on demand by createRenderTarget() -- same ownership pattern as
    // Vulkan's Renderer.createdRenderTargets.
    private val createdRenderTargets = mutableListOf<OffscreenRenderTarget>()

    /** Creates an offscreen [width]x[height] color render destination -- see
     * [RenderRenderer.createRenderTarget]'s doc comment. Tracked in [createdRenderTargets]
     * for teardown in [destroy]. */
    override fun createRenderTarget(width: Int, height: Int): RenderTarget =
        OffscreenRenderTarget(graphicsDevice, width, height).also { createdRenderTargets += it }

    /** Renders [drawCalls] against [camera] into [target] -- see
     * [RenderRenderer.renderToTexture]'s doc comment. This is `performDraw`'s 3D-pass body,
     * minus the `getCurrentTexture()` call, targeting [target]'s own
     * [OffscreenRenderTarget.colorView] instead -- no explicit layout-transition step needed
     * (unlike Vulkan): WebGPU manages texture state transitions implicitly per-encoder. */
    override fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>) {
        val offscreen = target as OffscreenRenderTarget
        val device = graphicsDevice.wgpuContext.device
        val pipeline = WebGpuHandles.resolve<GPURenderPipeline>(renderPipeline.graphicsPipeline[0])
        ensureUniformResources(pipeline)

        val aspect = offscreen.width.toFloat() / offscreen.height.toFloat()
        val viewProjection = camera.viewProjectionMatrix(aspect, clipSpace)

        val encoder = device.createCommandEncoder()
        encoder.beginRenderPass(
            RenderPassDescriptor(
                colorAttachments = listOf(
                    RenderPassColorAttachment(
                        view = offscreen.colorView,
                        loadOp = GPULoadOp.Clear,
                        clearValue = clearColorValue,
                        storeOp = GPUStoreOp.Store
                    )
                ),
                depthStencilAttachment = RenderPassDepthStencilAttachment(
                    view = offscreen.depthView,
                    depthClearValue = 1.0f,
                    depthLoadOp = GPULoadOp.Clear,
                    depthStoreOp = GPUStoreOp.Store
                )
            )
        ) {
            // vec4f (not vec3f) for both -- see triangle.wgsl's own Uniforms struct doc comment.
            val lightFloats = floatArrayOf(
                DEFAULT_SCENE_LIGHT.direction.x, DEFAULT_SCENE_LIGHT.direction.y, DEFAULT_SCENE_LIGHT.direction.z, 0f,
                DEFAULT_SCENE_LIGHT.color.x, DEFAULT_SCENE_LIGHT.color.y, DEFAULT_SCENE_LIGHT.color.z, 0f
            )
            setPipeline(pipeline)
            var drawIndex = 0
            while (drawIndex < drawCalls.size) {
                val drawCall = drawCalls[drawIndex]
                // Same skip-on-format-mismatch guard as performDraw() -- see that function's
                // own comment.
                if (drawCall.mesh.format != primaryVertexFormat) {
                    drawIndex += 1
                    continue
                }
                val mvp = drawCall.model * viewProjection
                device.queue.writeBuffer(uniformBuffer!!, 0uL, fastArrayBufferOf(mvp.data + lightFloats))
                setBindGroup(0u, uniformBindGroup!!)
                val mesh = drawCall.mesh as Mesh
                setVertexBuffer(0u, WebGpuHandles.resolve(mesh.vertexBuffer.handle))
                setIndexBuffer(WebGpuHandles.resolve(mesh.indexBuffer.handle), meshIndexFormat)
                drawIndexed(mesh.indexCount.toUInt())
                drawIndex += 1
            }
            end()
        }
        device.queue.submit(listOf(encoder.finish()))
    }

    /** Reads [target]'s color attachment back to the CPU -- see
     * [RenderRenderer.readPixels]'s doc comment. Genuinely `suspend`s here (unlike Vulkan's
     * synchronous fence-wait implementation): `GPUBuffer.mapAsync` is asynchronous, no
     * synchronous CPU-visible readback exists in this API. WebGPU requires `bytesPerRow` in
     * a `copyTextureToBuffer` to be a multiple of 256 -- [target]'s width is padded up to
     * satisfy that, then the padding is stripped back out before returning a tightly-packed
     * [TextureAsset] (the same layout Vulkan's implementation already returns). */
    override suspend fun readPixels(target: RenderTarget): TextureAsset {
        val offscreen = target as OffscreenRenderTarget
        val device = graphicsDevice.wgpuContext.device
        val unpaddedBytesPerRow = offscreen.width * 4
        val bytesPerRow = ((unpaddedBytesPerRow + 255) / 256) * 256
        val bufferSize = (bytesPerRow * offscreen.height).toULong()
        val readbackBuffer = device.createBuffer(
            BufferDescriptor(size = bufferSize, usage = GPUBufferUsage.CopyDst or GPUBufferUsage.MapRead)
        )
        val encoder = device.createCommandEncoder()
        encoder.copyTextureToBuffer(
            source = TexelCopyTextureInfo(texture = offscreen.colorTexture),
            destination = TexelCopyBufferInfo(buffer = readbackBuffer, bytesPerRow = bytesPerRow.toUInt()),
            copySize = Extent3D(width = offscreen.width.toUInt(), height = offscreen.height.toUInt())
        )
        device.queue.submit(listOf(encoder.finish()))

        readbackBuffer.mapAsync(GPUMapMode.Read).getOrThrow()
        val mapped = readbackBuffer.getMappedRange()
        val paddedBytes = mapped.toByteArray()
        val packed = ByteArray(unpaddedBytesPerRow * offscreen.height)
        var row = 0
        while (row < offscreen.height) {
            paddedBytes.copyInto(
                destination = packed,
                destinationOffset = row * unpaddedBytesPerRow,
                startIndex = row * bytesPerRow,
                endIndex = row * bytesPerRow + unpaddedBytesPerRow
            )
            row += 1
        }
        readbackBuffer.unmap()
        readbackBuffer.close()
        return TextureAsset(packed, offscreen.width, offscreen.height)
    }

    /** Stages this frame's UI overlay content -- delegates to [performDrawUi]
     * ([RendererDrawUi.kt]). Named differently from the extension function it calls: an
     * extension function can't share its name with a member function it's called from
     * without the member call winning resolution and recursing into itself. */
    override fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) = performDrawUi(primitives, font)

    /** Stages this frame's world-space debug lines -- delegates to [performDrawDebugLines]
     * ([RendererDraw3D.kt]). See [drawUi]'s doc comment for why this can't just be the
     * extracted body under the same name. */
    override fun drawDebugLines(lines: List<LineSegment>) = performDrawDebugLines(lines)

    /** Renders one frame -- delegates to [performDraw] ([RendererDraw3D.kt]). See [drawUi]'s
     * doc comment for why this can't just be the extracted body under the same name. */
    override fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight) = performDraw(camera, drawCalls, light)

    override fun destroy() {
        uniformBuffer?.close()
        uniformBuffer = null
        uniformBindGroup = null
        wireframeUniformBuffer?.close()
        wireframeUniformBuffer = null
        wireframeUniformBindGroup = null
        uiRenderPipeline?.destroy()
        uiGlyphRenderPipeline?.destroy()
        uiTextureRenderPipeline?.destroy()
        uiRoundedQuadRenderPipeline?.destroy()
        createdRenderTargets.forEach { it.destroy() }
        uiQuadMeshPool.forEach { it.destroy() }
        uiGlyphMeshPool.forEach { it.destroy() }
        uiRoundedQuadMeshPool.forEach { it.destroy() }
        textureQuadMesh.destroy()
        lineMesh.destroy()
    }

    companion object {
        internal const val MAX_UI_QUADS = 256
        internal const val MAX_DEBUG_LINES = 64
        internal val WHITE_RGBA = AwakeColor.White
    }
}
