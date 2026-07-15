// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameWindowConfig
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.gameInstaller
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.freeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.orbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.scene
import io.github.ronjunevaldoz.awake.ui.ui

val AwakeGame.helloCubeWindowConfig: GameWindowConfig
    get() = windowConfig

fun helloCubeGame(): AwakeGame {
    val state = HelloCubeRuntimeState()

    return game {
        window {
            title = "Hello Cube"
            size(1600, 900)
            backend.select(platformBackendPreference())
        }
        scene("hello-cube") {
            cameraEntity(
                "camera",
                transform = { position(0f, 0f, 5f) },
                camera = {
                    eye(0f, 0f, 5f)
                    center(0f, 0f, 0f)
                    up(0f, 1f, 0f)
                    perspective(fovYDegrees = 45f, near = 0.1f, far = 100f)
                    primary(true)
                }
            )
            meshEntity(
                name = "cube",
                mesh = "cube",
                material = "default",
                transform = {
                    position(0f, 0f, 0f)
                    rotation(0f, 0f, 0f)
                    scale(1f, 1f, 1f)
                }
            )
            assets {
                mesh("cube") {
                    renderer.createMesh(sampleCubeGeometry)
                }
                material("default") {
                    renderer.createMaterial()
                }
            }
            val orbitSystem = orbitCameraSystem(
                target = "cube",
                camera = "camera",
                initialDistance = 8f,
                autoRotateSpeed = 0.4f
            ) {
                pitch = 0.4f
            }
            val freeFlySystem = freeFlyCameraSystem(camera = "camera")
            update { delta ->
                when (state.mode) {
                    HelloCubeCameraMode.ORBIT -> update(orbitSystem, delta)
                    HelloCubeCameraMode.FREE_FLY -> update(freeFlySystem, delta)
                }
                updateHelloCubeHud(state, delta)
            }
        }
        install(
            gameInstaller {
                val runtime = requireService(SceneGameRuntime::class)
                val debugConfig = HelloCubeDebugConfig(
                    websocketControlsEnabled = true,
                    offscreenProofEnabled = true
                )
                service(HelloCubeDebugConfig::class, debugConfig)
                service(HelloCubeDebugController::class, HelloCubeDebugController(runtime, state))
                ready {
                    if (debugConfig.offscreenProofEnabled) {
                        runtime.verifyHelloCubeOffscreenReadback()
                    }
                }
            }
        )
        ui {
            overlay { viewportWidth, viewportHeight ->
                drawHelloCubeOverlay(
                    scene = requireService(),
                    state = state,
                    viewportWidth = viewportWidth,
                    viewportHeight = viewportHeight
                )
            }
        }
    }
}
