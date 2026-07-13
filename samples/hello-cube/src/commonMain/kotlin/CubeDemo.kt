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
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.text
import io.github.ronjunevaldoz.awake.ui.textureQuad
import io.github.ronjunevaldoz.awake.ui.toggle
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
 */
class CubeDemo(private val ui: UiContext, private val font: BitmapFont) :
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

    // Smallest possible proof the custom UI overlay pipeline works end to end: a toggle
    // rendered top-left over the existing cube scene (see docs/MVP_PLAN.md's custom-UI
    // decision log entry).
    private var debugOverlayOn = false

    private var cameraMode = CameraMode.ORBIT
    private lateinit var orbitCameraSystem: OrbitCameraSystem
    private lateinit var freeFlyCameraSystem: FreeFlyCameraSystem
    private var showFrustum = false

    // Reference/floor grid on the XZ plane at y=0 -- this demo has no ground mesh (a single
    // static cube, no floor), but a subtle spatial reference grid is still useful for judging
    // orbit distance/orientation. Reuses Renderer.drawDebugLines, same as showFrustum -- see
    // Grid's own doc comment.
    private var showGrid = false

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
    private var showMinimap = false
    private lateinit var minimapTarget: RenderTarget
    private lateinit var minimapMaterial: Material

    /** The frustum/minimap camera's view (`eye`/`center` etc., initially seeded from
     * `sample.scene.json`), captured/mutated separately from the live render camera --
     * [OrbitCameraSystem]/[FreeFlyCameraSystem] drive the actual render camera, while this
     * one is now independently orbitable via [frustumYaw]/[frustumPitch]/[frustumDistance]
     * (see the FRUSTUM AZIMUTH/ELEVATION/ZOOM sliders in [drawCatalogUi]), the same role
     * `demo.SceneRuntimeHost.followCameraSnapshot()` played in the now-retired `awake-demo`,
     * minus the moving-player part (this sample has no player). */
    private lateinit var homeCameraSnapshot: CoreCamera
    private lateinit var cubeTransform: Transform

    // Frustum camera's own orbit state -- same yaw/pitch/distance-around-a-target idea as
    // OrbitCameraSystem, but driven only by sliders (no drag/WASD/pinch), so it's plain
    // fields recomputed into homeCameraSnapshot.eye/center each frame rather than a full
    // OrbitCameraSystem instance (which would also need a live scene.components.Camera to
    // write into, not a bare CoreCamera).
    private var frustumYaw = 0f
    private var frustumPitch = 0f
    private var frustumDistance = 0f

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
        // slider has been touched.
        val offset = homeCameraSnapshot.eye - cubeTransform.position
        frustumDistance = offset.length3()
        if (frustumDistance > 0f) {
            frustumPitch = asin((offset.y / frustumDistance).coerceIn(-1f, 1f))
            frustumYaw = atan2(offset.x, offset.z)
        }

        orbitCameraSystem = OrbitCameraSystem(
            target = cubeTransform,
            camera = cameraComponent,
            // This sample has nothing else to animate (a single static cube, no player/AI),
            // so auto-rotate by default -- drag still takes over/overrides yaw normally,
            // see OrbitCameraSystem's own doc comment for why this defaults to off there.
            autoRotateSpeed = AUTO_ROTATE_SPEED
        )
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
                when (cameraMode) {
                    CameraMode.ORBIT -> orbitCameraSystem.update(world, step)
                    CameraMode.FREE_FLY -> freeFlyCameraSystem.update(world, step)
                }
            },
            render = { sceneRuntime.render(delta) }
        )
    }

    override fun drawPanel(panel: ColumnScope) {
        // Grouped under section labels (plain text, not a bordered panel() -- this UI library
        // has no content auto-sizing/measure pass, so a fixed-height bordered box can't wrap
        // these groups' variable content, e.g. the frustum sliders only appearing while
        // FRUSTUM is on). Confirmed via a real screenshot that one undifferentiated column
        // read as catalog-picker and per-demo config all mixed together.
        panel.text("CAMERA", color = SECTION_LABEL_COLOR)
        val modeNames = CameraMode.entries.map { it.name }
        panel.dropdown("camera-mode", modeNames, cameraMode.ordinal, 0f, 32f)?.let { picked ->
            val newMode = CameraMode.entries[picked]
            if (newMode == CameraMode.FREE_FLY && cameraMode == CameraMode.ORBIT) {
                // Hand off orbit's current look orientation so switching modes doesn't snap
                // free-fly to its yaw=0/pitch=0 default (looking down -Z) -- orbit's yaw/pitch
                // describe the target-to-eye offset direction; free-fly's describe the
                // eye-to-look-target forward direction, the exact opposite vector, which
                // works out to negating both angles (eye position itself is already shared
                // via the same live Camera component, so only orientation needs handing off).
                freeFlyCameraSystem.setOrientation(-orbitCameraSystem.yaw, -orbitCameraSystem.pitch)
            }
            cameraMode = newMode
        }
        // Orbit-only: FREE_FLY drives the same live Camera component with its own
        // WASD/mouse-look controls (FreeFlyCameraSystem), so these sliders would fight it --
        // only ORBIT's yaw/pitch/distance are meaningful slider targets.
        if (cameraMode == CameraMode.ORBIT) {
            orbitCameraSystem.yaw = panel.slider("orbit-azimuth", -PI.toFloat(), PI.toFloat(), orbitCameraSystem.yaw, 0f, 28f, "AZIMUTH")
            orbitCameraSystem.pitch = panel.slider("orbit-elevation", OrbitCameraSystem.MIN_PITCH, OrbitCameraSystem.MAX_PITCH, orbitCameraSystem.pitch, 0f, 28f, "ELEVATION")
            orbitCameraSystem.distance = panel.slider("orbit-zoom", OrbitCameraSystem.MIN_DISTANCE, MAX_ZOOM_DISTANCE, orbitCameraSystem.distance, 0f, 28f, "ZOOM")
        }
        // Frustum camera sliders -- only shown while FRUSTUM is on (toggled in the DEBUG
        // OVERLAYS section below; reads last frame's value here, one frame of display lag on
        // the very toggle click, same as this immediate-mode architecture's usual "you own
        // your state, call order affects what THIS frame reads" contract elsewhere).
        if (showFrustum) {
            frustumYaw = panel.slider("frustum-azimuth", -PI.toFloat(), PI.toFloat(), frustumYaw, 0f, 28f, "F.AZIMUTH")
            frustumPitch = panel.slider("frustum-elevation", OrbitCameraSystem.MIN_PITCH, OrbitCameraSystem.MAX_PITCH, frustumPitch, 0f, 28f, "F.ELEVATION")
            frustumDistance = panel.slider("frustum-zoom", OrbitCameraSystem.MIN_DISTANCE, MAX_ZOOM_DISTANCE, frustumDistance, 0f, 28f, "F.ZOOM")
        }

        panel.text("DEBUG OVERLAYS", color = SECTION_LABEL_COLOR)
        val debugLabel = if (debugOverlayOn) "DEBUG: ON" else "DEBUG: OFF"
        debugOverlayOn = panel.toggle("debug-toggle", debugOverlayOn, 0f, 40f, debugLabel)
        showFrustum = panel.toggle("show-frustum", showFrustum, 0f, 32f, "FRUSTUM")
        showGrid = panel.toggle("show-grid", showGrid, 0f, 32f, "GRID")
        showMinimap = panel.toggle("show-minimap", showMinimap, 0f, 32f, "MINIMAP")

        // Recomputes homeCameraSnapshot.eye/center every frame regardless of showFrustum (cheap
        // trig) since the minimap (below) always renders from this camera and must stay in
        // sync even if the FRUSTUM wireframe itself is toggled off.
        val frustumCosPitch = cos(frustumPitch)
        val target = cubeTransform.position
        homeCameraSnapshot.eye.x = target.x + frustumDistance * frustumCosPitch * sin(frustumYaw)
        homeCameraSnapshot.eye.y = target.y + frustumDistance * sin(frustumPitch)
        homeCameraSnapshot.eye.z = target.z + frustumDistance * frustumCosPitch * cos(frustumYaw)
        homeCameraSnapshot.center.x = target.x
        homeCameraSnapshot.center.y = target.y
        homeCameraSnapshot.center.z = target.z

        // drawDebugLines stages (replaces, doesn't accumulate) the lines for the next draw()
        // call -- see its own doc comment -- so both toggles' lines must be combined into one
        // call rather than each calling drawDebugLines separately (a second call would
        // overwrite the first's lines rather than adding to them).
        val debugLines = buildList {
            if (showFrustum) {
                val corners = Frustum.corners(homeCameraSnapshot, aspectRatio)
                addAll(Frustum.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], FRUSTUM_COLOR) })
            }
            if (showGrid) {
                addAll(Grid.lines(size = GRID_SIZE, divisions = GRID_DIVISIONS).map { (a, b) -> LineSegment(a, b, GRID_COLOR) })
            }
        }
        renderer.drawDebugLines(debugLines)

        if (showMinimap) {
            // Same camera the FRUSTUM wireframe visualizes -- the minimap now shows exactly
            // what the frustum camera sees, not an unrelated fixed overhead view.
            renderer.renderToTexture(minimapTarget, homeCameraSnapshot, sampleDrawCalls())
            val size = MINIMAP_SIZE.toFloat()
            // Top-left, not top-right -- the whole right edge is now the settings panel
            // column above, so the minimap preview has the top-left corner free instead.
            ui.absolute(20f, 20f).textureQuad(size, size, minimapMaterial)
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
            "MODE: ${cameraMode.name}",
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

    override fun isMinimapEnabled(): Boolean = showMinimap
    override fun setMinimapEnabled(enabled: Boolean) {
        showMinimap = enabled
    }

    private enum class CameraMode { ORBIT, FREE_FLY }

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

        // Upper bound for the orbit-zoom slider -- well past OrbitCameraSystem's own
        // DEFAULT_DISTANCE (8f), enough room to zoom out and see the whole cube+frustum.
        private const val MAX_ZOOM_DISTANCE = 20f
    }
}
