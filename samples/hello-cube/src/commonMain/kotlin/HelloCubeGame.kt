// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameWindowConfig
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle
import io.github.ronjunevaldoz.awake.scene.runtime.ecs
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem

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
    val debugConfig = HelloCubeDebugConfig(
        websocketControlsEnabled = true,
        offscreenProofEnabled = true
    )
    val state = HelloCubeRuntimeState()
    lateinit var orbitSystem: SceneSystemHandle<OrbitCameraSystem>
    lateinit var freeFlySystem: SceneSystemHandle<FreeFlyCameraSystem>

    return game {
        window {
            title = "Hello Cube"
            size(1600, 900)
            backend.select(platformBackendPreference())
        }
        ecs {
            scene("hello-cube") {
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
            assets {
                mesh("cube") {
                    renderer.createMesh(sampleCubeGeometry)
                }
                material("default") {
                    renderer.createMaterial()
                }
            }
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
            overlay { viewportWidth, viewportHeight ->
                drawHelloCubeOverlay(state, viewportWidth, viewportHeight)
            }
            onReady {
                if (debugConfig.offscreenProofEnabled) {
                    verifyHelloCubeOffscreenReadback()
                }
            }
            service { debugConfig }
            service { HelloCubeDebugController(this, state) }
        }
    }
}
