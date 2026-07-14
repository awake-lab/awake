// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Grid
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRuntime
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.ui.ColumnScope
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.border
import io.github.ronjunevaldoz.awake.ui.clip
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.panel
import io.github.ronjunevaldoz.awake.ui.propertyCheckbox
import io.github.ronjunevaldoz.awake.ui.propertyRow
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.textureQuad
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class CameraMode { ORBIT, FREE_FLY }

/** [CubeDemo]'s camera/debug-overlay state, owned by [DemoCatalog] instead of [CubeDemo]
 * itself -- [DemoCatalog.switchTo] constructs a brand-new [CubeDemo] every time the user
 * switches back to CUBE, so state kept as a plain [CubeDemo] field would silently reset each
 * switch. These settings are conceptually global to the demo, not tied to one instance's
 * lifetime, so [DemoCatalog] constructs exactly one of these and hands the same instance to
 * every [CubeDemo] it creates. */
class CubeDemoConfig {
    var cameraMode = CameraMode.ORBIT
    var debugOverlayOn = false
    var showFrustum = false
    var showGrid = false
    var showMinimap = false

    var orbitYaw = 0f
    var orbitPitch = 0f
    var orbitDistance = 8f

    var frustumYaw = 0f
    var frustumPitch = 0f
    var frustumDistance = 0f
    // Guards the scene-derived initial frustum yaw/pitch/distance derivation in
    // CubeDemo.ready() so it only runs once (this demo's first-ever construction), not on
    // every switch-back, which would otherwise clobber whatever the user already dialed in.
    var frustumSeeded = false
}

/**
 * The minimal "hello, cube" demo this module was originally built to showcase -- a plain
 * [Game] implementation (see docs/MVP_PLAN.md's decision log, "GenericGameApplication a
 * standalone render bootstrap") -- constructible/testable independent of any backend. Now one
 * entry in [DemoCatalog] rather than injected directly at each platform entry point (see that
 * class's doc comment for why).
 *
 * A single static cube, no player/NavMesh -- just the camera/frustum catalog tool (below),
 * scoped down to this demo's one entity: no catalog-target dropdown (nothing to switch
 * between) and no `FOLLOW` camera mode (nothing to follow).
 *
 * Takes [ui]/[font] from [DemoCatalog] rather than owning them -- [Renderer.drawUi] replaces
 * this frame's whole staged UI on every call, it doesn't accumulate across callers, so only
 * one place (`DemoCatalog.render`) may call `ui.beginFrame`/`renderer.drawUi` per frame. This
 * demo just appends its own widgets to the same [ui] instance [DemoCatalog]'s own
 * demo-picker dropdown already staged this frame.
 *
 * [config] is owned by [DemoCatalog], not this class -- [DemoCatalog.switchTo] constructs a
 * brand-new [CubeDemo] every time the user switches back to CUBE, so any state kept as a
 * plain instance field here would silently reset (camera mode, debug overlay toggles, orbit
 * angle) every switch. Camera/debug-overlay settings are conceptually global to this demo,
 * not tied to one particular [CubeDemo] instance's lifetime, so they live in [config] instead.
 */
class CubeDemo(private val ui: UiContext, private val font: BitmapFont, private val config: CubeDemoConfig) :
    Game, DebugReadout, OffscreenPreviewSource, DebugCameraTarget, DebugMinimapTarget, PanelUser {
    private val fixedTimestepLoop = FixedTimestepLoop()

    // Set every render() call, read by drawPanel() -- PanelUser.drawPanel(panel: ColumnScope)
    // has no viewportWidth/aspectRatio params of its own (DemoCatalog calls it separately
    // from render()), so this demo remembers its own last-known frame geometry.
    private var aspectRatio = 1f

    private lateinit var renderer: Renderer
    private lateinit var sceneRuntime: SceneRuntime
    private lateinit var cubeMesh: Mesh
    private lateinit var material: Material
    private lateinit var cameraComponent: Camera

    private lateinit var orbitCameraSystem: OrbitCameraSystem
    private lateinit var freeFlyCameraSystem: FreeFlyCameraSystem

    // Minimap: proof that RenderTarget compositing (Renderer.renderToTexture +
    // UiContext.textureQuad) actually renders on screen, not just a clear color -- an
    // overhead camera of the same cube, drawn as a small quad in the top-right corner.
    //
    // KNOWN ISSUE, previously confirmed by real desktop testing: toggling this on calls
    // renderToTexture every frame, and this reportedly crashed the Vulkan backend within
    // roughly 30-60 seconds with `VK_SUBOPTIMAL_KHR` out of vkAcquireNextImageKHR in the
    // MAIN swapchain draw (not the offscreen pass itself) -- confirmed NOT caused by the
    // command-buffer-per-call leak an earlier session fixed (Renderer.runOffscreenCommands).
    //
    // Re-investigated (see docs/MVP_PLAN.md's D24 entry) by driving this toggle live via
    // DebugControlServer's `setMinimap` WebSocket command instead of a GUI click, with
    // Vulkan validation layers enabled for the first time on this repro. That run did NOT
    // reproduce the VK_SUBOPTIMAL_KHR message verbatim, but DID find a real, independent,
    // reproducible bug: `GenericGameApplication.dispose()` was destroying the backend
    // (VkDevice included) BEFORE calling `game.dispose()` -- so `CubeDemo.dispose()`'s
    // `cubeMesh.destroy()`/etc. (which call vkDestroyBuffer/vkDestroyImage against a
    // now-dead device) were undefined behavior, confirmed to reproducibly SIGSEGV inside
    // libvulkan's own vkDestroyBuffer. Fixed (dispose order reversed -- see that class's
    // doc comment). After the fix, driving this toggle on via the WebSocket channel and
    // leaving it on ran stable for 130+ seconds (no crash, no VK_SUBOPTIMAL_KHR) under
    // this investigation's conditions, well past the originally-reported 30-60s window.
    //
    // Left OFF by default anyway: the validation-layer run's window closed unexpectedly
    // early each time (tens of seconds in, cause not conclusively identified -- possibly an
    // artifact of validation-layer log-spam overhead stalling the render thread, since a
    // later run WITHOUT validation layers did not exhibit early closing at all), so the
    // exact original trigger for the reported VK_SUBOPTIMAL_KHR message is not confirmed
    // fixed, only ruled out as NOT being (solely) this dispose-order bug. Flip to `true`
    // to keep exercising this path; re-open the investigation if VK_SUBOPTIMAL_KHR
    // resurfaces now that the dispose-order crash it may have been masquerading as is gone.
    // Minimap now renders from homeCameraSnapshot (the frustum camera), not a separate fixed
    // overhead camera -- previously it showed an unrelated top-down view, which didn't match
    // what the FRUSTUM wireframe toggle actually visualizes.
    private lateinit var minimapTarget: RenderTarget
    private lateinit var minimapMaterial: Material

    /** The frustum/minimap camera's view (`eye`/`center` etc., initially seeded from
     * `sample.scene.json`), captured/mutated separately from the live render camera --
     * [OrbitCameraSystem]/[FreeFlyCameraSystem] drive the actual render camera, while this
     * one is now independently orbitable via [config]'s frustumYaw/frustumPitch/
     * frustumDistance (see the FRUSTUM AZIMUTH/ELEVATION/ZOOM sliders in [drawPanel]), the
     * same role `demo.SceneRuntimeHost.followCameraSnapshot()` played in the now-retired
     * `awake-demo`, minus the moving-player part (this sample has no player). */
    private lateinit var homeCameraSnapshot: CoreCamera
    private lateinit var cubeTransform: Transform

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        cubeMesh = renderer.createMesh(sampleCubeGeometry)
        material = renderer.createMaterial()
        sceneRuntime = SceneRuntime(renderer)
        sceneRuntime.load(SCENE_PATH) { request ->
            val mesh = cubeMesh.takeIf { request.meshRenderer.mesh == "cube" }
                ?: error("Unsupported scene mesh '${request.meshRenderer.mesh}'.")
            MeshRenderer(mesh, material)
        }

        val world = sceneRuntime.world
        val scene = sceneRuntime.scene
        val cubeEntity = scene.roots.firstOrNull { it.name == "cube" }?.entity
            ?: error("sample.scene.json is missing a root node named 'cube'.")
        val cameraEntity = scene.roots.firstOrNull { it.name == "camera" }?.entity
            ?: error("sample.scene.json is missing a root node named 'camera'.")
        cubeTransform = world.get(cubeEntity)
            ?: error("'cube' node has no Transform.")
        cameraComponent = world.get(cameraEntity)
            ?: error("'camera' node has no Camera component.")

        val liveCamera = cameraComponent.camera
        homeCameraSnapshot = CoreCamera(
            eye = liveCamera.eye.copy(),
            center = liveCamera.center.copy(),
            up = liveCamera.up.copy(),
            fovYRadians = liveCamera.fovYRadians,
            near = liveCamera.near,
            far = liveCamera.far,
            flipYForClipSpace = renderer.flipYForClipSpace
        )

        // Derive the frustum camera's initial yaw/pitch/distance from its scene-authored
        // eye/center (inverse of OrbitCameraSystem.update()'s own offset formula) so the
        // first frame's frustum/minimap view matches the scene JSON exactly, before any
        // slider has been touched -- only on this demo's very first construction (config
        // persists across switches, so a later switch-back must NOT clobber whatever the
        // user already dialed in with a fresh scene-derived value).
        if (!config.frustumSeeded) {
            val offset = homeCameraSnapshot.eye - cubeTransform.position
            config.frustumDistance = offset.length3()
            if (config.frustumDistance > 0f) {
                config.frustumPitch = asin((offset.y / config.frustumDistance).coerceIn(-1f, 1f))
                config.frustumYaw = atan2(offset.x, offset.z)
            }
            config.frustumSeeded = true
        }

        orbitCameraSystem = OrbitCameraSystem(
            target = cubeTransform,
            camera = cameraComponent,
            initialDistance = config.orbitDistance,
            // This sample has nothing else to animate (a single static cube, no player/AI),
            // so auto-rotate by default -- drag still takes over/overrides yaw normally,
            // see OrbitCameraSystem's own doc comment for why this defaults to off there.
            autoRotateSpeed = AUTO_ROTATE_SPEED
        )
        orbitCameraSystem.yaw = config.orbitYaw
        orbitCameraSystem.pitch = config.orbitPitch
        freeFlyCameraSystem = FreeFlyCameraSystem(camera = cameraComponent)

        minimapTarget = renderer.createRenderTarget(MINIMAP_SIZE, MINIMAP_SIZE)
        minimapMaterial = renderer.createMaterial(renderTarget = minimapTarget)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        aspectRatio = viewportWidth / viewportHeight
        // CubeDemo is the one place that still wants a fixed-step camera update --
        // FixedTimestepLoop is now an implementation detail CubeDemo opts into itself, not
        // something GenericGameApplication imposes on every Game.
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = { step ->
                val world = sceneRuntime.world
                when (config.cameraMode) {
                    CameraMode.ORBIT -> orbitCameraSystem.update(world, step)
                    CameraMode.FREE_FLY -> freeFlyCameraSystem.update(world, step)
                }
            },
            render = { sceneRuntime.render(delta) }
        )
    }

    override fun drawPanel(panel: ColumnScope) {
        // Real bordered/rounded background box -- previously drawPanel() wrote rows straight
        // onto the plain column DemoCatalog hands it, so there was never any visible box behind
        // the inspector content at all (confirmed missing via a real screenshot + the panel()
        // widget's own snapshot test, which proved panel() itself renders children fine -- the
        // gap was this call site never using it). Fixed height (PANEL_CONTENT_HEIGHT), sized
        // for the worst case (every optional row shown at once) -- this library has no content
        // auto-sizing/measure pass (see UiModifier.kt's Dimension doc comment), so a bordered
        // box can't shrink-to-fit; unused space at the bottom when fewer rows are shown is the
        // accepted tradeoff over not having a box at all.
        panel.panel(
            "inspector",
            Dimension.FillMax,
            Dimension.Fixed(PANEL_CONTENT_HEIGHT.px),
            radius = UiShape.md,
            borderWidth = 2f.dp
        ) { slot ->
            // panel()'s own nested column starts flush at the box's top-left corner -- build a
            // second, inset column instead so content doesn't touch the border. TEXT_SCALE
            // makes the 8px bitmap font legible at this window size (see UiScope.textScale's
            // doc comment) -- every glyph-measuring call below (propertyRow/propertyCheckbox/
            // dropdown/slider labels) must go through a scope carrying the same scale, or rows
            // sized for big text would clip small text and vice versa.
            val content = context.column(slot.x + PANEL_PADDING, slot.y + PANEL_PADDING, slot.width - PANEL_PADDING * 2, font, theme, textScale = TEXT_SCALE)

            // Studio-inspector layout: label|control property rows instead of a centered label
            // baked into a big recolored rect. propertyRow() draws the label in a fixed-width
            // left column and hands back the remaining right-hand slot for exactly one control.
            content.text("CAMERA", color = SECTION_LABEL_COLOR)
            val modeNames = CameraMode.entries.map { it.name }
            val modeSlot = content.propertyRow("MODE", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
            context.absolute(modeSlot.x, modeSlot.y, font, theme, textScale = TEXT_SCALE)
                .dropdown("camera-mode", modeNames, config.cameraMode.ordinal, modeSlot.width, modeSlot.height, modifier = WIDGET_MODIFIER)
                ?.let { picked ->
                    val newMode = CameraMode.entries[picked]
                    if (newMode == CameraMode.FREE_FLY && config.cameraMode == CameraMode.ORBIT) {
                        // Hand off orbit's current look orientation so switching modes doesn't snap
                        // free-fly to its yaw=0/pitch=0 default (looking down -Z) -- orbit's yaw/pitch
                        // describe the target-to-eye offset direction; free-fly's describe the
                        // eye-to-look-target forward direction, the exact opposite vector, which
                        // works out to negating both angles (eye position itself is already shared
                        // via the same live Camera component, so only orientation needs handing off).
                        freeFlyCameraSystem.setOrientation(-orbitCameraSystem.yaw, -orbitCameraSystem.pitch)
                    }
                    config.cameraMode = newMode
                }
            // Orbit-only: FREE_FLY drives the same live Camera component with its own
            // WASD/mouse-look controls (FreeFlyCameraSystem), so these sliders would fight it --
            // only ORBIT's yaw/pitch/distance are meaningful slider targets.
            if (config.cameraMode == CameraMode.ORBIT) {
                val azimuthSlot = content.propertyRow("AZIMUTH", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                orbitCameraSystem.yaw = context.absolute(azimuthSlot.x, azimuthSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("orbit-azimuth", -PI.toFloat(), PI.toFloat(), orbitCameraSystem.yaw, azimuthSlot.width, azimuthSlot.height, modifier = WIDGET_MODIFIER)
                val elevationSlot = content.propertyRow("ELEVATION", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                orbitCameraSystem.pitch = context.absolute(elevationSlot.x, elevationSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("orbit-elevation", OrbitCameraSystem.MIN_PITCH, OrbitCameraSystem.MAX_PITCH, orbitCameraSystem.pitch, elevationSlot.width, elevationSlot.height, modifier = WIDGET_MODIFIER)
                val zoomSlot = content.propertyRow("ZOOM", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                orbitCameraSystem.distance = context.absolute(zoomSlot.x, zoomSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("orbit-zoom", OrbitCameraSystem.MIN_DISTANCE, MAX_ZOOM_DISTANCE, orbitCameraSystem.distance, zoomSlot.width, zoomSlot.height, modifier = WIDGET_MODIFIER)
            }
            // Written back every frame (not just when a slider moves it) -- auto-rotate/drag
            // inside OrbitCameraSystem.update() also mutate yaw/pitch/distance, and config is
            // what survives DemoCatalog.switchTo() constructing a brand-new CubeDemo later.
            config.orbitYaw = orbitCameraSystem.yaw
            config.orbitPitch = orbitCameraSystem.pitch
            config.orbitDistance = orbitCameraSystem.distance

            // propertyCheckbox, not the plain settings-list checkbox() -- this section shares
            // the same label|value column rhythm as CAMERA above (one grouped inspector, not a
            // separately-styled list), box right-aligned to the row's own right edge.
            content.text("DEBUG OVERLAYS", color = SECTION_LABEL_COLOR)
            config.debugOverlayOn = content.propertyCheckbox("debug-toggle", config.debugOverlayOn, "DEBUG", ROW_HEIGHT, modifier = WIDGET_MODIFIER, boxSize = CHECKBOX_SIZE)
            config.showFrustum = content.propertyCheckbox("show-frustum", config.showFrustum, "FRUSTUM", ROW_HEIGHT, modifier = WIDGET_MODIFIER, boxSize = CHECKBOX_SIZE)
            // Nested directly under FRUSTUM's own row (no separate floating panel elsewhere on
            // screen) -- "F." prefix reads as a sub-item of the checkbox immediately above.
            if (config.showFrustum) {
                val fAzimuthSlot = content.propertyRow("F.AZIMUTH", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                config.frustumYaw = context.absolute(fAzimuthSlot.x, fAzimuthSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("frustum-azimuth", -PI.toFloat(), PI.toFloat(), config.frustumYaw, fAzimuthSlot.width, fAzimuthSlot.height, modifier = WIDGET_MODIFIER)
                val fElevationSlot = content.propertyRow("F.ELEVATION", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                config.frustumPitch = context.absolute(fElevationSlot.x, fElevationSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("frustum-elevation", OrbitCameraSystem.MIN_PITCH, OrbitCameraSystem.MAX_PITCH, config.frustumPitch, fElevationSlot.width, fElevationSlot.height, modifier = WIDGET_MODIFIER)
                val fZoomSlot = content.propertyRow("F.ZOOM", ROW_HEIGHT, labelWidth = LABEL_WIDTH)
                config.frustumDistance = context.absolute(fZoomSlot.x, fZoomSlot.y, font, theme, textScale = TEXT_SCALE)
                    .slider("frustum-zoom", OrbitCameraSystem.MIN_DISTANCE, MAX_ZOOM_DISTANCE, config.frustumDistance, fZoomSlot.width, fZoomSlot.height, modifier = WIDGET_MODIFIER)
            }
            config.showGrid = content.propertyCheckbox("show-grid", config.showGrid, "GRID", ROW_HEIGHT, modifier = WIDGET_MODIFIER, boxSize = CHECKBOX_SIZE)
            config.showMinimap = content.propertyCheckbox("show-minimap", config.showMinimap, "MINIMAP", ROW_HEIGHT, modifier = WIDGET_MODIFIER, boxSize = CHECKBOX_SIZE)
        }

        // Recomputes homeCameraSnapshot.eye/center every frame regardless of showFrustum (cheap
        // trig) since the minimap (below) always renders from this camera and must stay in
        // sync even if the FRUSTUM wireframe itself is toggled off.
        val frustumCosPitch = cos(config.frustumPitch)
        val target = cubeTransform.position
        homeCameraSnapshot.eye.x = target.x + config.frustumDistance * frustumCosPitch * sin(config.frustumYaw)
        homeCameraSnapshot.eye.y = target.y + config.frustumDistance * sin(config.frustumPitch)
        homeCameraSnapshot.eye.z = target.z + config.frustumDistance * frustumCosPitch * cos(config.frustumYaw)
        homeCameraSnapshot.center.x = target.x
        homeCameraSnapshot.center.y = target.y
        homeCameraSnapshot.center.z = target.z

        // drawDebugLines stages (replaces, doesn't accumulate) the lines for the next draw()
        // call -- see its own doc comment -- so both toggles' lines must be combined into one
        // call rather than each calling drawDebugLines separately (a second call would
        // overwrite the first's lines rather than adding to them).
        val debugLines = buildList {
            if (config.showFrustum) {
                val corners = Frustum.corners(homeCameraSnapshot, aspectRatio)
                addAll(Frustum.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], FRUSTUM_COLOR) })
            }
            if (config.showGrid) {
                addAll(Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS).map { (a, b) -> LineSegment(a, b, GRID_COLOR) })
            }
        }
        renderer.drawDebugLines(debugLines)

        if (config.showMinimap) {
            // Same camera the FRUSTUM wireframe visualizes -- the minimap now shows exactly
            // what the frustum camera sees, not an unrelated fixed overhead view.
            renderer.renderToTexture(minimapTarget, homeCameraSnapshot, sampleDrawCalls())
            val size = MINIMAP_SIZE.toFloat()
            // Top-left, below the demo picker -- the old separate frustum panel that used to
            // occupy this spot is gone now (folded into the inspector column on the right), so
            // this stays free. Bottom-left is already the debug HUD text readout.
            ui.absolute(20f, 60f).textureQuad(size, size, minimapMaterial)
        }
    }

    /** Releases the mesh/material this demo created in [ready] -- required now that
     * [DemoCatalog] can switch away from this demo and back, not just construct it once for
     * the whole app's lifetime. */
    override fun dispose() {
        cubeMesh.destroy()
        material.destroy()
        minimapMaterial.destroy()
        minimapTarget.destroy()
    }

    override fun sampleDrawCalls(): List<DrawCall> = listOf(DrawCall(cubeMesh, material))

    override fun debugLines(): List<String> {
        val eye = cameraComponent.camera.eye
        return listOf(
            "MODE: ${config.cameraMode.name}",
            "X:${eye.x.format1()} Y:${eye.y.format1()} Z:${eye.z.format1()}"
        )
    }

    private fun Float.format1(): String = (kotlin.math.round(this * 10f) / 10f).toString()

    override fun getCameraEye(): Vec3 = cameraComponent.camera.eye
    override fun setCameraEye(eye: Vec3) {
        cameraComponent.camera.eye = eye
    }
    override fun getCameraCenter(): Vec3 = cameraComponent.camera.center
    override fun setCameraCenter(center: Vec3) {
        cameraComponent.camera.center = center
    }

    override fun isMinimapEnabled(): Boolean = config.showMinimap
    override fun setMinimapEnabled(enabled: Boolean) {
        config.showMinimap = enabled
    }

    companion object {
        private const val SCENE_PATH = "scenes/sample.scene.json"

        // Radians/second -- a full 2*PI orbit takes about 21 seconds at this speed.
        private const val AUTO_ROTATE_SPEED = 0.3f
        private val FRUSTUM_COLOR = floatArrayOf(1f, 0.9f, 0.2f, 1f)
        // Dim gray, deliberately subtle -- shouldn't fight the cube's colorful face gradient
        // or the frustum's yellow wireframe.
        private val GRID_COLOR = floatArrayOf(0.4f, 0.4f, 0.4f, 1f)
        private const val GRID_SIZE = 10f
        private const val GRID_DIVISIONS = 10
        // Dim, not the theme's full-brightness label color -- a section header should read as
        // structure, not another interactive row.
        private val SECTION_LABEL_COLOR = floatArrayOf(0.6f, 0.6f, 0.65f, 1f)
        private const val MINIMAP_SIZE = 160

        // Shared shape/border look for this panel's interactive widgets -- previously none of
        // them used UiModifier at all despite the library supporting it (see UiModifier.kt).
        // 2dp (not 1dp) border -- at TEXT_SCALE's bigger row/box sizes a 1px line read as
        // nearly invisible against the panel's own background (both draw from
        // theme.tokens.background/border, which are close in value by design -- a real
        // screenshot showed the checkbox boxes barely readable at the old 1dp).
        private val WIDGET_MODIFIER = UiModifier().clip(UiShape.sm).border(2f.dp)

        // The 8px BitmapFont glyph cell read as illegibly tiny at this panel's on-screen size
        // (confirmed via a real screenshot) -- a 2x whole-number scale keeps the bitmap atlas
        // crisp, where 1.75x introduced uneven texel expansion and visible text noise. Every
        // row-sizing constant below is sized against this scaled glyph, not the raw 8px cell.
        private const val TEXT_SCALE = 2f

        // Studio-inspector row height -- shared by every propertyRow()/propertyCheckbox() call
        // in drawPanel() so the whole column reads as one consistent grid, not per-widget
        // literal heights scattered across the function. Sized for the taller of this row's
        // two occupants: CHECKBOX_SIZE (24) or a TEXT_SCALE glyph (16), plus vertical padding.
        private const val ROW_HEIGHT = 34f

        // Fixed left-hand label column for propertyRow()/propertyCheckbox() -- sized for the
        // longest label this panel actually draws ("F.ELEVATION", 11 chars) at TEXT_SCALE's
        // glyph width (8 * 2 = 16px/char -> 176px), rounded up for breathing room. This
        // library has no text-measure-then-shrink-column pass (see Dimension's doc comment),
        // so a too-narrow fixed column would silently clip/overlap instead of wrapping.
        private val LABEL_WIDTH = 184f.dp
        private val CHECKBOX_SIZE = 24f.dp

        // Worst-case content height: 2 section labels + up to 11 rows (MODE + 3 orbit sliders
        // + DEBUG + FRUSTUM + 3 frustum sub-sliders + GRID + MINIMAP), each row height + gap,
        // plus top/bottom padding -- see drawPanel()'s own doc comment for why this can't just
        // shrink to whatever's actually shown this frame.
        private const val PANEL_CONTENT_HEIGHT = 600f
        private const val PANEL_PADDING = 14f

        // Upper bound for the orbit-zoom slider -- well past OrbitCameraSystem's own
        // DEFAULT_DISTANCE (8f), enough room to zoom out and see the whole cube+frustum.
        private const val MAX_ZOOM_DISTANCE = 20f
    }
}
