// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRuntime
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

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
class CubeDemo(private val ui: UiContext, private val font: BitmapFont) : Game {
    private val fixedTimestepLoop = FixedTimestepLoop()

    private lateinit var renderer: Renderer
    private lateinit var sceneRuntime: SceneRuntime
    private lateinit var cubeMesh: Mesh
    private lateinit var material: Material

    // Smallest possible proof the custom UI overlay pipeline works end to end: a toggle
    // rendered top-left over the existing cube scene (see docs/MVP_PLAN.md's custom-UI
    // decision log entry).
    private var debugOverlayOn = false

    private var cameraMode = CameraMode.ORBIT
    private lateinit var orbitCameraSystem: OrbitCameraSystem
    private lateinit var freeFlyCameraSystem: FreeFlyCameraSystem
    private var showFrustum = false

    /** The scene-authored view (`eye`/`center` etc. from `sample.scene.json`), captured
     * before [OrbitCameraSystem]/[FreeFlyCameraSystem] start mutating the live `Camera`
     * component in place -- the frustum toggle visualizes this fixed "home" view while
     * Orbit/Free-fly drives the actual render camera, the same role
     * `demo.SceneRuntimeHost.followCameraSnapshot()` played in the now-retired `awake-demo`,
     * minus the moving-player part (this sample has no player). */
    private lateinit var homeCameraSnapshot: CoreCamera

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
        val cubeTransform: Transform = world.get(cubeEntity)
            ?: error("'cube' node has no Transform.")
        val cameraComponent: Camera = world.get(cameraEntity)
            ?: error("'camera' node has no Camera component.")

        val liveCamera = cameraComponent.camera
        homeCameraSnapshot = CoreCamera(
            eye = liveCamera.eye.copy(),
            center = liveCamera.center.copy(),
            up = liveCamera.up.copy(),
            fovYRadians = liveCamera.fovYRadians,
            near = liveCamera.near,
            far = liveCamera.far
        )

        orbitCameraSystem = OrbitCameraSystem(
            target = cubeTransform,
            camera = cameraComponent,
            // This sample has nothing else to animate (a single static cube, no player/AI),
            // so auto-rotate by default -- drag still takes over/overrides yaw normally,
            // see OrbitCameraSystem's own doc comment for why this defaults to off there.
            autoRotateSpeed = AUTO_ROTATE_SPEED
        )
        freeFlyCameraSystem = FreeFlyCameraSystem(camera = cameraComponent)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
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
            render = {
                drawCatalogUi(viewportWidth / viewportHeight)
                sceneRuntime.render(delta)
            }
        )
    }

    private fun drawCatalogUi(aspectRatio: Float) {
        val debugLabel = if (debugOverlayOn) "DEBUG: ON" else "DEBUG: OFF"
        debugOverlayOn = ui.toggle("debug-toggle", 20f, 20f, 120f, 40f, debugOverlayOn, debugLabel, font)

        val modeNames = CameraMode.entries.map { it.name }
        ui.dropdown("camera-mode", 20f, 70f, 160f, 32f, modeNames, cameraMode.ordinal, font)?.let { picked ->
            cameraMode = CameraMode.entries[picked]
        }

        showFrustum = ui.toggle("show-frustum", 200f, 70f, 100f, 32f, showFrustum, "FRUSTUM", font)
        if (showFrustum) {
            val corners = Frustum.corners(homeCameraSnapshot, aspectRatio)
            val lines = Frustum.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], FRUSTUM_COLOR) }
            renderer.drawDebugLines(lines)
        }
    }

    /** Releases the mesh/material this demo created in [ready] -- required now that
     * [DemoCatalog] can switch away from this demo and back, not just construct it once for
     * the whole app's lifetime. */
    override fun dispose() {
        cubeMesh.destroy()
        material.destroy()
    }

    private enum class CameraMode { ORBIT, FREE_FLY }

    companion object {
        private const val SCENE_PATH = "scenes/sample.scene.json"

        // Radians/second -- a full 2*PI orbit takes about 21 seconds at this speed.
        private const val AUTO_ROTATE_SPEED = 0.3f
        private val FRUSTUM_COLOR = floatArrayOf(1f, 0.9f, 0.2f, 1f)
    }
}
