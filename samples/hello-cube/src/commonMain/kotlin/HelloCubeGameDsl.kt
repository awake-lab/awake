// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.FrameStats
import io.github.ronjunevaldoz.awake.engine.application.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.UiAnchor
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.anchored
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.textBlockHeight
import kotlin.math.round

internal enum class HelloCubeCameraMode {
    ORBIT,
    FREE_FLY
}

internal class HelloCubeRuntimeState(
    var mode: HelloCubeCameraMode = HelloCubeCameraMode.ORBIT,
    val frameStats: FrameStats = FrameStats()
) {
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

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val overlayBounds = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    column(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.TopRight,
            width = 280f,
            height = font.textBlockHeight(lineCount = 3, textScale = HELLO_CUBE_TEXT_SCALE),
            margin = UiInsets(start = 0f.dp, top = 24f.dp, end = 40f.dp, bottom = 0f.dp)
        ),
        textScale = HELLO_CUBE_TEXT_SCALE,
    ) {
        text("SCENE: ${scene.sceneName}")
        text("MODE: ${state.mode.name}")
        text("CAMERA: ${scene.requireCamera("camera").camera.eye.debugLabel()}")
    }

    val lines = scene.helloCubeDebugLines(state)
    column(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.BottomLeft,
            width = 320f,
            height = font.textBlockHeight(lineCount = lines.size, textScale = HELLO_CUBE_TEXT_SCALE),
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 12f.dp)
        ),
        textScale = HELLO_CUBE_TEXT_SCALE,
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
    if (state.frameStats.update(delta)) {
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
        "FPS: ${state.frameStats.fps.toInt()}  FRAME: ${state.frameStats.frameTimeMs.toInt()}MS",
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

internal expect fun platformBackendPreference(): GameWindowBackend
