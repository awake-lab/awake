// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the one real cross-backend entry
 * point `RenderSystem` calls. `awake-vulkan`'s `expect class Renderer` implements this
 * (`expect class Renderer(...) : io.github.ronjunevaldoz.awake.render.renderer.Renderer`) --
 * see [io.github.ronjunevaldoz.awake.render.mesh.Mesh]'s doc comment for why this doesn't
 * change `VulkanApplication.kt`'s construction pattern.
 */
interface Renderer {
    /** Whether this backend's clip space needs [Camera.viewProjectionMatrix]'s Y-flip
     * correction -- see that method's own doc comment. Vulkan's NDC has +Y down (needs the
     * flip); WebGPU's NDC has +Y up, same as the OpenGL convention [Camera] assumes natively
     * (confirmed by this repo's own `ui_quad.wgsl`: "pixel-space is Y-down, NDC is Y-up"), so
     * it needs no flip. This is a backend implementation detail, not scene-authored content --
     * callers building a [Camera] (manually, or via `SceneRuntime`/`SceneLoader` from scene
     * JSON) should read this rather than hardcoding `true` for every backend. */
    val flipYForClipSpace: Boolean

    /** RGBA (each `0f..1f`) color the 3D render pass clears to before every frame's
     * [draw] call -- defaults to opaque black on every backend, so any app that never sets
     * this sees exactly what it always did. A game with real 3D content whose camera can see
     * past its scene geometry (e.g. a sky above a ground plane) sets this once it becomes
     * relevant, mirroring the "optional per-game override, set from an `overlay`/`onReady`
     * block" pattern [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls]
     * already established -- a plain `var`, not a per-frame parameter of [draw] itself, since
     * one solid background color rarely needs to change every single frame. */
    var clearColor: FloatArray

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
     * trivial 1x1 white pixel. [uniformFloatCount] is the material's per-object uniform
     * buffer's size in floats -- `16` (a bare MVP matrix) for every material before GPU
     * skinning existed; a skinned material passes `16 + 16 * jointCount` (MVP + joint
     * palette) instead, since the skinned vertex shader reads the palette from the same
     * uniform binding. */
    fun createMaterial(
        texture: TextureAsset? = null,
        renderTarget: RenderTarget? = null,
        uniformFloatCount: Int = 16
    ): Material

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
    fun drawUi(primitives: List<UiDrawPrimitive>, font: UiFont? = null)

    /** Draws world-space debug lines (e.g. a frustum wireframe) -- unlike [drawUi], these
     * are transformed by [draw]'s own view-projection matrix and drawn *inside* the main 3D
     * render pass (not a separate pass), so they get real depth-testing against scene
     * geometry. Stages the lines for the next [draw] call, same "stage now, consume on next
     * draw" pattern [drawUi] already uses -- call before [draw] each frame. */
    fun drawDebugLines(lines: List<LineSegment>)

    /** Draws one GPU-skinned mesh this frame, through a dedicated skinned pipeline -- a
     * skinned mesh's vertex layout ([io.github.ronjunevaldoz.awake.render.mesh.VertexFormat.PositionNormalColorSkin])
     * doesn't match [createMesh]'s default 3D pipeline's fixed layout, so it can't go through
     * the ordinary [draw]/[DrawCall] path the way a static mesh does -- a renderer has exactly
     * one fixed-vertex-format main 3D pipeline (see `RendererDraw3D.kt`'s doc comment on the
     * Vulkan backend). [jointPalette] is `16 * jointCount` floats -- see
     * `SkinnedAnimationPlayer.jointPalette`; [model]'s combined with this frame's camera and
     * [jointPalette] into [material]'s uniform buffer, same "stage now, consume on next [draw]"
     * pattern [drawDebugLines] already uses. Default no-op: a backend with no skinned-pipeline
     * support yet (e.g. WebGPU's still-stubbed 3D material path, see that backend's own
     * `Material.kt`) simply doesn't render the call, rather than every implementer needing an
     * empty override. */
    fun drawSkinnedMesh(mesh: Mesh, material: Material, model: Mat4, jointPalette: FloatArray) {}

    /** Draws one textured mesh this frame, through a dedicated textured pipeline -- same
     * reason [drawSkinnedMesh] exists: a mesh with a real `baseColorTexture`
     * ([io.github.ronjunevaldoz.awake.render.mesh.VertexFormat.PositionNormalColorUv]) doesn't
     * match [createMesh]'s default 3D pipeline's fixed layout either, so it goes through this
     * separate staged-draw path instead of [draw]/[DrawCall]. [material] must have been built
     * with a real [texture][createMaterial]'s `texture` parameter, not the default placeholder
     * -- see `textured.wgsl`. Same "stage now, consume on next [draw]" pattern as
     * [drawDebugLines]/[drawSkinnedMesh]; same default no-op for a backend with no textured-
     * pipeline support yet. */
    fun drawTexturedMesh(mesh: Mesh, material: Material, model: Mat4) {}

    fun destroy()
}
