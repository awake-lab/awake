// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import kotlin.math.round

internal data class HelloCubeOverlayModel(
    val sceneName: String,
    val modeLabel: String,
    val cameraLabel: String,
    val debugLines: List<String>
)

internal fun SceneGameRuntime.helloCubeOverlayModel(state: HelloCubeRuntimeState): HelloCubeOverlayModel {
    val cameraLabel = requireCamera("camera").camera.eye.toHelloCubeLabel()
    val debugLines = listOf(
        "FPS: ${state.frameStats.fps.toInt()}  FRAME: ${state.frameStats.frameTimeMs.toInt()}MS",
        "MODE: ${state.mode.name}",
        "CAMERA: $cameraLabel"
    )
    return HelloCubeOverlayModel(
        sceneName = sceneName,
        modeLabel = state.mode.name,
        cameraLabel = cameraLabel,
        debugLines = debugLines
    )
}

internal fun SceneGameRuntime.helloCubeDebugLines(state: HelloCubeRuntimeState): List<String> {
    return helloCubeOverlayModel(state).debugLines
}

internal fun Vec3.toHelloCubeLabel(): String {
    return "${round2(x)}, ${round2(y)}, ${round2(z)}"
}

private fun round2(value: Float): Float = round(value * 100f) / 100f
