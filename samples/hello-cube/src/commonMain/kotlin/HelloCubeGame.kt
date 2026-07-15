// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameWindowConfig
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.ecs
import io.github.ronjunevaldoz.awake.ui.ui

data class HelloCubeDebugConfig(
    val websocketControlsEnabled: Boolean,
    val offscreenProofEnabled: Boolean
)

class HelloCubeDebugController internal constructor(
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

val AwakeGame.helloCubeWindowConfig: GameWindowConfig
    get() = windowConfig

val AwakeGame.helloCubeDebugConfig: HelloCubeDebugConfig
    get() = requireService()

val AwakeGame.helloCubeDebugController: HelloCubeDebugController
    get() = requireService()

fun helloCubeGame(): AwakeGame {
    val state = HelloCubeRuntimeState()

    return game {
        window {
            helloCubeWindow()
        }
        ecs {
            helloCubeScene()
            helloCubeAssets()
            helloCubeCameraControls(state)
        }
        install(
            helloCubeDebug(state) {
                websocketControls()
                offscreenProof()
            }
        )
        ui {
            helloCubeOverlay(state)
        }
    }
}
