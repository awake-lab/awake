// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.WindowBackendDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.ui
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

internal fun SceneGameRuntime.drawHelloCubeOverlay(
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    uiContext.ui(
        x = viewportWidth - 320f,
        y = 24f,
        width = 280f,
        font = font,
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        text("SCENE: $sceneName")
        text("MODE: ${state.mode.name}")
        text("CAMERA: ${requireCamera("camera").camera.eye.debugLabel()}")
    }

    val lines = helloCubeDebugLines(state)
    uiContext.ui(
        x = 20f,
        y = viewportHeight - lines.size * HELLO_CUBE_HUD_LINE_HEIGHT - 12f,
        width = 320f,
        font = font,
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        lines.forEach { line ->
            text(line)
        }
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
