// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0

import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.render.renderer.LineSegment
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

/**
 * wasmJs counterpart to `SampleApplication` (`appMain`, desktop/Android/iOS only -- not
 * visible from this wasmJs-only source set, so this class keeps its own copy of the same
 * geometry/catalog-tool wiring rather than sharing one). Same single-cube geometry, no
 * texture, built on [WebGpuGameApplication] (`awake-backend-webgpu`) instead. See
 * `SampleApplication`'s doc comment for why there's no catalog-target dropdown/`FOLLOW`
 * mode here.
 */
class WebGpuSampleApplication : WebGpuGameApplication(
    vertexShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    fragmentShaderResourcePath = "assets/shader/webgpu/triangle.wgsl",
    vertexStride = VERTEX_STRIDE,
    meshes = mapOf("cube" to MeshGeometry(cubeVertices, cubeIndices)),
    scenePath = "scenes/sample.scene.json"
) {
    // Same verification proof as SampleApplication (appMain) -- see that class's comment.
    private var debugOverlayOn = false

    private var cameraMode = CameraMode.ORBIT
    private lateinit var orbitCameraSystem: OrbitCameraSystem
    private lateinit var freeFlyCameraSystem: FreeFlyCameraSystem
    private var showFrustum = false

    /** See `SampleApplication.homeCameraSnapshot`'s doc comment -- identical role. */
    private lateinit var homeCameraSnapshot: CoreCamera

    override fun onSceneReady() {
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
            autoRotateSpeed = AUTO_ROTATE_SPEED
        )
        freeFlyCameraSystem = FreeFlyCameraSystem(camera = cameraComponent)
    }

    override fun onFixedUpdate(delta: Float) {
        super.onFixedUpdate(delta)
        when (cameraMode) {
            CameraMode.ORBIT -> orbitCameraSystem.update(world, delta)
            CameraMode.FREE_FLY -> freeFlyCameraSystem.update(world, delta)
        }
    }

    override fun onDrawUi(ui: UiContext) {
        debugOverlayOn = ui.toggle("debug-toggle", 20f, 20f, 120f, 40f, debugOverlayOn)
        val label = if (debugOverlayOn) "DEBUG: ON" else "DEBUG: OFF"
        ui.text(150f, 32f, label, floatArrayOf(1f, 1f, 1f, 1f), font)

        val modeNames = CameraMode.entries.map { it.name }
        ui.dropdown("camera-mode", 20f, 70f, 160f, 32f, modeNames, cameraMode.ordinal)?.let { picked ->
            cameraMode = CameraMode.entries[picked]
        }

        showFrustum = ui.toggle("show-frustum", 200f, 70f, 32f, 32f, showFrustum)
        if (showFrustum) {
            val corners = Frustum.corners(homeCameraSnapshot, aspectRatio)
            val lines = Frustum.EDGES.map { (a, b) -> LineSegment(corners[a], corners[b], FRUSTUM_COLOR) }
            drawDebugLines(lines)
        }
    }

    private enum class CameraMode { ORBIT, FREE_FLY }

    companion object {
        // Radians/second -- a full 2*PI orbit takes about 21 seconds at this speed.
        private const val AUTO_ROTATE_SPEED = 0.3f
        private val FRUSTUM_COLOR = floatArrayOf(1f, 0.9f, 0.2f, 1f)
        const val VERTEX_STRIDE = 8 * Float.SIZE_BYTES

        val cubeVertices = floatArrayOf(
            -0.5f, -0.5f, -0.5f, 0f, 0f, 0f, 0f, 0f, // v0
            0.5f, -0.5f, -0.5f, 1f, 0f, 0f, 1f, 0f, // v1
            0.5f, 0.5f, -0.5f, 1f, 1f, 0f, 1f, 1f, // v2
            -0.5f, 0.5f, -0.5f, 0f, 1f, 0f, 0f, 1f, // v3
            -0.5f, -0.5f, 0.5f, 0f, 0f, 1f, 0f, 0f, // v4
            0.5f, -0.5f, 0.5f, 1f, 0f, 1f, 1f, 0f, // v5
            0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, 1f, // v6
            -0.5f, 0.5f, 0.5f, 0f, 1f, 1f, 0f, 1f, // v7
        )
        val cubeIndices = intArrayOf(
            0, 1, 2, 2, 3, 0, // back
            4, 5, 6, 6, 7, 4, // front
            0, 3, 7, 7, 4, 0, // left
            1, 5, 6, 6, 2, 1, // right
            0, 4, 5, 5, 1, 0, // bottom
            3, 2, 6, 6, 7, 3, // top
        )
    }
}
