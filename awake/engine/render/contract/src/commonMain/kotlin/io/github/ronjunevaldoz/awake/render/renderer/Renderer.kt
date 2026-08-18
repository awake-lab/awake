// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.texture.PbrTextureSet
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.font.UiFont

private const val DEFAULT_LIGHT_DIRECTION_X = 0.4f
private const val DEFAULT_LIGHT_DIRECTION_Y = 0.8f
private const val DEFAULT_LIGHT_DIRECTION_Z = 0.4f

/** The direction/color every lit shader (`triangle.wgsl` and friends) hardcoded as a WGSL
 * `const` before [Renderer.draw]'s `light` parameter existed -- kept as the default so a scene
 * with no `Light` entity renders identically to before this parameter was added. */
val DEFAULT_SCENE_LIGHT = SceneLight(
    direction = Vec3(DEFAULT_LIGHT_DIRECTION_X, DEFAULT_LIGHT_DIRECTION_Y, DEFAULT_LIGHT_DIRECTION_Z),
    color = Vec3(1f, 1f, 1f),
)

/** Defaults for [Renderer.horizonColor]/[Renderer.zenithColor]/[Renderer.fogColor] -- a plain
 * daytime sky (warm, light blue-white at the horizon, deeper blue overhead) and a neutral
 * gray-blue haze. Read-only in practice: a backend overriding these with real storage hands
 * out its own arrays. */
@Suppress("MagicNumber") // Colour components; naming each channel would not clarify anything.
val DEFAULT_HORIZON_COLOR = floatArrayOf(0.72f, 0.80f, 0.88f, 1f)

@Suppress("MagicNumber")
val DEFAULT_ZENITH_COLOR = floatArrayOf(0.20f, 0.38f, 0.68f, 1f)

@Suppress("MagicNumber")
val DEFAULT_FOG_COLOR = floatArrayOf(0.55f, 0.62f, 0.70f, 1f)

/**
 * Module restructuring slice 1 (see docs/MVP_PLAN.md): the one real cross-backend entry
 * point `RenderSystem` calls. `awake-vulkan`'s `expect class Renderer` implements this
 * (`expect class Renderer(...) : io.github.ronjunevaldoz.awake.render.renderer.Renderer`) --
 * see [io.github.ronjunevaldoz.awake.render.mesh.Mesh]'s doc comment for why this doesn't
 * change `VulkanApplication.kt`'s construction pattern.
 */
interface Renderer {
    /** The NDC convention this backend's API expects -- Y direction *and* depth range (see
     * [ClipSpace]). Vulkan's NDC has +Y down and depth `0..1`; WebGPU's has +Y up and depth
     * `0..1` (the +Y up half is confirmed by this repo's own `ui_quad.wgsl`: "pixel-space is
     * Y-down, NDC is Y-up").
     *
     * The renderer owns this because the renderer owns the API. A [Camera] stores only lens
     * parameters; this value is what gets handed to [Camera.viewProjectionMatrix] at the
     * moment a matrix is built, so nothing upstream -- a scene document, a demo, a test --
     * ever gets the chance to bake in the wrong convention. */
    val clipSpace: ClipSpace

    /** RGBA (each `0f..1f`) color the 3D render pass clears to before every frame's
     * [draw] call -- defaults to opaque black on every backend, so any app that never sets
     * this sees exactly what it always did. A game with real 3D content whose camera can see
     * past its scene geometry (e.g. a sky above a ground plane) sets this once it becomes
     * relevant, mirroring the "optional per-game override, set from an `overlay`/`onReady`
     * block" pattern [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls]
     * already established -- a plain `var`, not a per-frame parameter of [draw] itself, since
     * one solid background color rarely needs to change every single frame. */
    var clearColor: FloatArray

    /** When `true`, meshes drawn by the next [draw] call render as edges instead of filled
     * triangles, for whichever formats the active backend has a wireframe-capable pipeline
     * for (a format with none just keeps drawing filled -- see each backend's `Renderer` for
     * which formats it built one for). `false` by default so a game that never toggles this
     * sees exactly what it always did. A plain `var`, same "not a per-frame [draw] parameter"
     * pattern as [clearColor] -- a debug/dev toggle, not something that needs to vary call to
     * call. Vulkan and WebGPU implement this with genuinely different mechanisms
     * (`VK_POLYGON_MODE_LINE` vs. a line-index buffer derived from each mesh's triangle
     * indices, drawn with `LineList` topology) -- WebGPU has no polygon-mode equivalent, see
     * that backend's `Renderer`/`Mesh` for why the derived-index-buffer approach was chosen
     * over a barycentric-coordinate fragment shader. */
    var wireframe: Boolean

    /** Whether the shadow depth pre-pass runs before [draw]'s main pass -- a backend/bootstrap
     * with no shadow support at all (every backend's default construction path today) simply
     * ignores this: it exists so a game can offer a "Shadows" toggle without depending on a
     * concrete backend type. Defaults to `true` so a backend that DOES support shadows shows
     * them the moment it opts in, matching how a new [Renderer] capability elsewhere in this
     * interface (e.g. [drawDebugLines]) is on by default rather than requiring an extra call
     * to enable. */
    var shadowsEnabled: Boolean

    /**
     * When `true`, a backend prints a warning whenever it silently drops a [DrawCall] because
     * [DrawCall.mesh]'s format has no matching pipeline registered (the intentional but
     * previously-silent "unmatched format, skip" behavior every backend already had -- this
     * only makes it observable, it changes no rendering decision). `false` by default with a
     * no-op setter here, same "ignore unless a real backend overrides it with actual storage"
     * pattern [sceneViewport] already uses -- a backend/test double that never opts in keeps
     * behaving exactly as it always did, and every existing `object : Renderer` test fake needs
     * no change to keep compiling.
     */
    var debugMode: Boolean
        get() = false
        set(_) {}

    /**
     * When `true`, the 3D pass draws a procedural sky (horizon-to-zenith gradient plus a sun
     * disc at the frame's `SceneLight.direction` and a moon disc opposite it) behind all scene
     * geometry, instead of leaving [clearColor] visible. `false` by default, so nothing changes
     * appearance until a game opts in.
     *
     * Accessors default to "ignore" rather than being abstract, same as [debugMode]: a backend
     * whose bootstrap never built a skybox pipeline, and every test double, keeps rendering
     * exactly as it always did. Both real backends override this with real storage.
     */
    var showEnvironment: Boolean
        get() = false

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

    /** RGB(A) the sky gradient blends from at the horizon ([showEnvironment] only). */
    var horizonColor: FloatArray
        get() = DEFAULT_HORIZON_COLOR

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

    /** RGB(A) the sky gradient blends to straight overhead ([showEnvironment] only). */
    var zenithColor: FloatArray
        get() = DEFAULT_ZENITH_COLOR

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

    /** RGB(A) distant geometry blends toward on the two PBR-capable lit paths
     * (`textured.wgsl`/`lit_shadow.wgsl`). Only visible once [fogDensity] is non-zero. */
    var fogColor: FloatArray
        get() = DEFAULT_FOG_COLOR

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

    /** Exponential fog density -- `0f` (default) is "no fog", which is what every scene got
     * before this existed. Unlike [showEnvironment] this needs no pipeline support: it is one
     * more uniform field the existing lit shaders read, so it works on any backend. */
    var fogDensity: Float
        get() = 0f

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

    /**
     * The sub-rect of the surface [draw]'s 3D pass renders into, and the aspect ratio its
     * projection is built for -- `null` (the default) means the whole surface, which is what
     * every game that never sets this always got.
     *
     * An editor sets this so the scene stays inside its viewport panel instead of filling the
     * window behind the surrounding chrome. Only the 3D pass is confined: [drawUi] still covers
     * the full surface, since the chrome is what defines the rect in the first place.
     *
     * Accessors default to "ignore" rather than being abstract: a backend that has no way to
     * scissor a pass, and every test double, keeps rendering full-surface unchanged. A backend
     * that supports it overrides this with real storage.
     */
    var sceneViewport: RenderViewport?
        get() = null

        @Suppress("UNUSED_PARAMETER")
        set(value) = Unit

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
     * buffer's size in floats -- `24` (MVP + [SceneLight]'s direction/color, both `vec4f`) by
     * default, since every material a [DrawCall] can reach goes through [draw]'s single lit
     * pass (`prepareDrawCalls` always writes MVP + either light or [DrawCall
     * .extraUniformFloats], unconditionally -- a smaller buffer here would be a real
     * out-of-bounds write, not just wasted space); a skinned material passes `16 + 16 *
     * jointCount` instead (MVP + joint palette via [DrawCall.extraUniformFloats], no light).
     * [pbrTextures] is the rest of a glTF metallic-roughness material's channels alongside
     * [texture] (base color) -- `null` (default) keeps every existing caller building a
     * base-color-only (or untextured) material exactly as it always did; a backend with no
     * PBR sampling support is free to ignore it, same as [texture] itself on a backend with
     * no texture support at all. */
    fun createMaterial(
        texture: TextureAsset? = null,
        renderTarget: RenderTarget? = null,
        uniformFloatCount: Int = 24,
        pbrTextures: PbrTextureSet? = null,
    ): Material

    /** Creates an offscreen [width]x[height] color+depth render destination, on demand -- see
     * [createMesh]'s doc comment for the same "game decides, not the bootstrap" rationale.
     * This `Renderer` tracks the returned [RenderTarget] for teardown in its own `destroy()`
     * (mirroring how created textures are already tracked), same as [RenderTarget.destroy]
     * itself documents. */
    fun createRenderTarget(width: Int, height: Int): RenderTarget

    /** [light] shades every [DrawCall] in this frame's pass -- defaults to
     * [DEFAULT_SCENE_LIGHT] (the same direction/color every lit shader hardcoded before this
     * parameter existed) so a scene with no `Light` entity looks exactly as it always did.
     * `RenderSystem` overrides this with the scene's actual primary `Light` entity when one
     * exists. */
    fun draw(camera: Camera, drawCalls: List<DrawCall>, light: SceneLight = DEFAULT_SCENE_LIGHT)

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

    fun destroy()
}
