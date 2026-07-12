// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the one real cross-backend entry
 * point `RenderSystem` calls. `awake-vulkan`'s `expect class Renderer` implements this
 * (`expect class Renderer(...) : io.github.ronjunevaldoz.awake.render.renderer.Renderer`) --
 * see [io.github.ronjunevaldoz.awake.render.mesh.Mesh]'s doc comment for why this doesn't
 * change `VulkanApplication.kt`'s construction pattern.
 */
interface Renderer {
    /** Uploads [geometry] as a GPU mesh, on demand -- a game calls this itself for whatever
     * assets it wants, whenever it wants (not something the render bootstrap decides upfront
     * from a constructor-supplied asset list). */
    fun createMesh(geometry: MeshGeometry): Mesh

    /** Builds a [Material] bound to this `Renderer`'s single render pipeline, on demand --
     * see [createMesh]'s doc comment for the same "game decides, not the bootstrap"
     * rationale. [texture] and [renderTarget] are mutually exclusive (passing both throws
     * `IllegalArgumentException`) -- when [renderTarget] is given, the built [Material]
     * samples that target's own color attachment directly (no CPU round-trip), the same
     * sampling machinery [texture] already uses either way. Both null falls back to a
     * trivial 1x1 white pixel. */
    fun createMaterial(texture: TextureAsset? = null, renderTarget: RenderTarget? = null): Material

    /** Creates an offscreen [width]x[height] color+depth render destination, on demand -- see
     * [createMesh]'s doc comment for the same "game decides, not the bootstrap" rationale.
     * This `Renderer` tracks the returned [RenderTarget] for teardown in its own `destroy()`
     * (mirroring how created textures are already tracked), same as [RenderTarget.destroy]
     * itself documents. */
    fun createRenderTarget(width: Int, height: Int): RenderTarget

    fun draw(camera: Camera, drawCalls: List<DrawCall>)

    /** Renders [drawCalls] against [camera] into [target] instead of the swapchain/canvas --
     * a sibling of [draw] (not an overload/parameter of it), since the two have different
     * post-conditions: [draw] ends with a present, this ends with [target]'s color image left
     * in a sampled-readable state for [readPixels] or a compositing [Material] to consume.
     * [target]'s own [RenderTarget.width]/[RenderTarget.height] supply the aspect ratio passed
     * to [Camera.viewProjectionMatrix] -- NOT the live swapchain/canvas size. Does not draw
     * debug lines ([drawDebugLines]) or a UI overlay ([drawUi]) -- an offscreen render is a
     * clean scene-only pass; both are out of scope for now. */
    fun renderToTexture(target: RenderTarget, camera: Camera, drawCalls: List<DrawCall>)

    /** Reads [target]'s color attachment back to the CPU as tightly-packed RGBA8 pixels (the
     * same layout [TextureAsset.data] already assumes) -- for golden-image/screenshot-diff
     * testing. `suspend` because WebGPU's `GPUBuffer.mapAsync` readback is genuinely
     * asynchronous (no synchronous CPU-visible readback exists in that API); Vulkan's
     * implementation satisfies this `suspend fun` synchronously under the hood (a fence
     * wait), which is a valid implementation, not a violation of the contract. Call this only
     * after a [renderToTexture] call into the same [target] -- reading before any render is
     * backend-defined (typically zeroed/undefined pixels, not an error). */
    suspend fun readPixels(target: RenderTarget): TextureAsset

    /** Draws this frame's UI overlay on top of whatever [draw] already wrote -- a separate
     * method (not folded into [draw]) so the 3D `Camera`+[DrawCall] contract stays untouched.
     * Each backend composites this as a second render pass with `loadOp = LOAD`, after the
     * 3D pass, in the same frame. [font] is only needed the first time a caller draws glyph
     * primitives -- both the (colored-quad) UI pipeline and the glyph pipeline are built
     * lazily, on the first call that needs them, not unconditionally at `Renderer`
     * construction time, so a game that never calls this never pays for either pipeline. */
    fun drawUi(primitives: List<UiDrawPrimitive>, font: BitmapFont? = null)

    /** Draws world-space debug lines (e.g. a frustum wireframe) -- unlike [drawUi], these
     * are transformed by [draw]'s own view-projection matrix and drawn *inside* the main 3D
     * render pass (not a separate pass), so they get real depth-testing against scene
     * geometry. Stages the lines for the next [draw] call, same "stage now, consume on next
     * draw" pattern [drawUi] already uses -- call before [draw] each frame. */
    fun drawDebugLines(lines: List<LineSegment>)

    fun destroy()
}
