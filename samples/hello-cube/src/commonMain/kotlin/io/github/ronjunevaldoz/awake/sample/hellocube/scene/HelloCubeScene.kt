// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameSpec
import io.github.ronjunevaldoz.awake.scene.runtime.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.freeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.orbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.playerControlSystem
import io.github.ronjunevaldoz.awake.scene.runtime.sceneGame
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.updateHelloCubeHud
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeCameraMode
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal fun helloCubeSceneSpec(state: HelloCubeRuntimeState): SceneGameSpec {
    return sceneGame {
        name("hello-cube")
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
        val playerControl = playerControlSystem()
        val orbitSystem = orbitCameraSystem(
            target = "cube",
            camera = "camera",
            initialDistance = 8f,
            initialPitch = 0.4f,
            autoRotateSpeed = 0.4f
        )
        val freeFlySystem = freeFlyCameraSystem(camera = "camera")
        update { delta ->
            update(playerControl, delta)
            when (state.mode) {
                HelloCubeCameraMode.ORBIT -> update(orbitSystem, delta)
                HelloCubeCameraMode.FREE_FLY -> update(freeFlySystem, delta)
            }
            updateHelloCubeHud(state, delta)
        }
    }
}
