// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.debug

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.sample.server.DebugService
import io.github.ronjunevaldoz.awake.sample.hellocube.presentation.helloCubeDebugLines
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeCameraMode
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal data class HelloCubeDebugConfig(
    val websocketControlsEnabled: Boolean,
    val offscreenProofEnabled: Boolean
)

internal class HelloCubeDebugController(
    private val runtime: SceneGameRuntime,
    private val state: HelloCubeRuntimeState
) {
    fun switchDemo(index: Int) {
        state.mode = when (index) {
            1 -> HelloCubeCameraMode.FREE_FLY
            else -> HelloCubeCameraMode.ORBIT
        }
    }

    fun setCameraEye(eye: Vec3) {
        runtime.requireCamera("camera").camera.eye = eye
    }

    fun setCameraCenter(center: Vec3) {
        runtime.requireCamera("camera").camera.center = center
    }

    fun setMinimap(enabled: Boolean) {
        state.minimapEnabled = enabled
    }

    fun snapshot(): DebugSnapshot = runtime.helloCubeSnapshot(state)
}

internal val AwakeGame.helloCubeDebugConfig: HelloCubeDebugConfig
    get() = requireService()

internal val AwakeGame.helloCubeDebugController: HelloCubeDebugController
    get() = requireService()

internal fun HelloCubeDebugController.asDebugService(): DebugService<DebugCommand, DebugSnapshot> =
    object : DebugService<DebugCommand, DebugSnapshot> {
        override fun handle(command: DebugCommand) {
            when (command) {
                is DebugCommand.SwitchDemo -> switchDemo(command.index)
                is DebugCommand.SetCameraEye -> setCameraEye(Vec3(command.x, command.y, command.z))
                is DebugCommand.SetCameraCenter -> setCameraCenter(Vec3(command.x, command.y, command.z))
                is DebugCommand.SetMinimap -> setMinimap(command.enabled)
                DebugCommand.GetState -> Unit
            }
        }

        override fun snapshot(): DebugSnapshot = this@asDebugService.snapshot()
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

private fun Vec3.toDebugVec3() = DebugVec3(x, y, z)
