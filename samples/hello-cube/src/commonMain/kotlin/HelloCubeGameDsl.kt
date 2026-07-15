// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.WindowBackendDsl
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.ui.GameUiDsl
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import kotlin.math.round

internal enum class HelloCubeCameraMode {
    ORBIT,
    FREE_FLY
}

internal class HelloCubeRuntimeState(
    var mode: HelloCubeCameraMode = HelloCubeCameraMode.ORBIT
) {
    var fps = 0f
    var frameTimeMs = 0f
    var fpsAccumulator = 0f
    var fpsFrameCount = 0
    var minimapEnabled = false
}

internal fun WindowDsl.helloCubeWindow() {
    title = "Hello Cube"
    size(1600, 900)
    backend.select(platformBackendPreference())
}

internal fun SceneGameDsl.helloCubeScene() {
    name("hello-cube")
    entity("camera") {
        transform {
            position(0f, 0f, 5f)
        }
        camera {
            eye(0f, 0f, 5f)
            center(0f, 0f, 0f)
            up(0f, 1f, 0f)
            perspective(fovYDegrees = 45f, near = 0.1f, far = 100f)
            primary(true)
        }
    }
    entity("cube") {
        transform {
            position(0f, 0f, 0f)
            rotation(0f, 0f, 0f)
            scale(1f, 1f, 1f)
        }
        meshRenderer(mesh = "cube", material = "default")
    }
}

internal fun SceneGameDsl.helloCubeAssets() {
    assets {
        mesh("cube") {
            renderer.createMesh(sampleCubeGeometry)
        }
        material("default") {
            renderer.createMaterial()
        }
    }
}

internal fun SceneGameDsl.helloCubeCameraControls(state: HelloCubeRuntimeState) {
    lateinit var orbitSystem: SceneSystemHandle<OrbitCameraSystem>
    lateinit var freeFlySystem: SceneSystemHandle<FreeFlyCameraSystem>

    orbitSystem = system("orbit") {
        OrbitCameraSystem(
            target = requireTransform("cube"),
            camera = requireCamera("camera"),
            initialDistance = 8f,
            autoRotateSpeed = 0.4f
        ).also { system ->
            system.pitch = 0.4f
        }
    }
    freeFlySystem = system("freeFly") {
        FreeFlyCameraSystem(requireCamera("camera"))
    }
    update { delta ->
        when (state.mode) {
            HelloCubeCameraMode.ORBIT -> update(orbitSystem, delta)
            HelloCubeCameraMode.FREE_FLY -> update(freeFlySystem, delta)
        }
        updateHelloCubeHud(state, delta)
    }
}

internal class HelloCubeDebugDsl {
    private var websocketControlsEnabled: Boolean = false
    private var offscreenProofEnabled: Boolean = false

    fun websocketControls(enabled: Boolean = true) {
        websocketControlsEnabled = enabled
    }

    fun offscreenProof(enabled: Boolean = true) {
        offscreenProofEnabled = enabled
    }

    internal fun build(): HelloCubeDebugConfig = HelloCubeDebugConfig(
        websocketControlsEnabled = websocketControlsEnabled,
        offscreenProofEnabled = offscreenProofEnabled
    )
}

internal fun helloCubeDebug(
    state: HelloCubeRuntimeState,
    block: HelloCubeDebugDsl.() -> Unit = {}
): GameInstaller {
    val config = HelloCubeDebugDsl().apply(block).build()
    return object : GameInstaller {
        override fun install(into: GameDsl) {
            val runtime = into.requireService(SceneGameRuntime::class)
            into.service(HelloCubeDebugConfig::class, config)
            into.service(HelloCubeDebugController::class, HelloCubeDebugController(runtime, state))
            into.ready {
                if (config.offscreenProofEnabled) {
                    runtime.verifyHelloCubeOffscreenReadback()
                }
            }
        }
    }
}

internal suspend fun SceneGameRuntime.verifyHelloCubeOffscreenReadback() {
    val camera = Camera(
        eye = Vec3(2.5f, 2f, 4f),
        center = Vec3(0f, 0f, 0f),
        fovYRadians = 1f,
        near = 0.1f,
        far = 10f,
        flipYForClipSpace = renderer.flipYForClipSpace
    )
    val pixels = readback(camera, width = 128, height = 128)
    val centerOffset = ((pixels.height / 2) * pixels.width + pixels.width / 2) * 4
    println(
        "OFFSCREEN READBACK: ${pixels.width}x${pixels.height} (${collectDrawCalls().size} draw calls) center pixel RGBA = " +
            "${pixels.data[centerOffset].toInt() and 0xFF}," +
            "${pixels.data[centerOffset + 1].toInt() and 0xFF}," +
            "${pixels.data[centerOffset + 2].toInt() and 0xFF}," +
            "${pixels.data[centerOffset + 3].toInt() and 0xFF}"
    )
    saveDebugPng(pixels.data, pixels.width, pixels.height, "offscreen-debug.png")
}

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    column(
        x = viewportWidth - 320f,
        y = 24f,
        width = 280f,
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        text("SCENE: ${scene.sceneName}")
        text("MODE: ${state.mode.name}")
        text("CAMERA: ${scene.requireCamera("camera").camera.eye.debugLabel()}")
    }

    val lines = scene.helloCubeDebugLines(state)
    column(
        x = 20f,
        y = viewportHeight - lines.size * HELLO_CUBE_HUD_LINE_HEIGHT - 12f,
        width = 320f,
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        lines.forEach { line ->
            text(line)
        }
    }
}

internal fun GameUiDsl.helloCubeOverlay(state: HelloCubeRuntimeState) {
    overlay { viewportWidth, viewportHeight ->
        drawHelloCubeOverlay(
            scene = requireService<SceneGameRuntime>(),
            state = state,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight
        )
    }
}

internal fun SceneGameRuntime.updateHelloCubeHud(
    state: HelloCubeRuntimeState,
    delta: Float
) {
    state.frameTimeMs = delta * 1000f
    state.fpsAccumulator += delta
    state.fpsFrameCount++
    if (state.fpsAccumulator >= 1f) {
        state.fps = state.fpsFrameCount / state.fpsAccumulator
        state.fpsAccumulator = 0f
        state.fpsFrameCount = 0
        println("DEBUG HUD [HELLO CUBE]")
        helloCubeDebugLines(state).forEach { println("DEBUG HUD:   $it") }
    }
}

internal fun SceneGameRuntime.helloCubeSnapshot(state: HelloCubeRuntimeState): DebugSnapshot {
    val camera = requireCamera("camera").camera
    return DebugSnapshot(
        demoName = "HELLO CUBE",
        debugLines = helloCubeDebugLines(state),
        cameraEye = camera.eye.toDebugVec3(),
        cameraCenter = camera.center.toDebugVec3(),
        minimapEnabled = state.minimapEnabled
    )
}

private fun SceneGameRuntime.helloCubeDebugLines(state: HelloCubeRuntimeState): List<String> {
    return listOf(
        "FPS: ${state.fps.toInt()}  FRAME: ${state.frameTimeMs.toInt()}MS",
        "MODE: ${state.mode.name}",
        "CAMERA: ${requireCamera("camera").camera.eye.debugLabel()}"
    )
}

private fun Vec3.toDebugVec3() = DebugVec3(x, y, z)

private fun Vec3.debugLabel(): String {
    return "${round2(x)}, ${round2(y)}, ${round2(z)}"
}

private fun round2(value: Float): Float = round(value * 100f) / 100f

private const val HELLO_CUBE_TEXT_SCALE = 2f
private const val HELLO_CUBE_HUD_LINE_HEIGHT = 22f

internal expect fun platformBackendPreference(): GameWindowBackend

internal fun WindowBackendDsl.select(backend: GameWindowBackend) {
    when (backend) {
        GameWindowBackend.DEFAULT -> default()
        GameWindowBackend.VULKAN -> vulkan()
        GameWindowBackend.WEBGPU -> webGpu()
        GameWindowBackend.OPENGL -> openGl()
    }
}
