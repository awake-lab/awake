// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.systems

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.PlayerControlSystem

fun SceneGameDsl.orbitCameraSystem(
    name: String = "orbit",
    target: String,
    camera: String,
    initialDistance: Float = 5f,
    initialPitch: Float = 0.4f,
    autoRotateSpeed: Float = 0f,
    configure: OrbitCameraSystem.() -> Unit = {}
): SceneSystemHandle<OrbitCameraSystem> {
    onReady {
        val cameraEntity = requireEntity(camera)
        world.add(cameraEntity, io.github.ronjunevaldoz.awake.scene.components.OrbitControl().apply {
            this.target = requireTransform(target)
            this.distance = initialDistance
            this.pitch = initialPitch
        })
    }
    return system(name) {
        OrbitCameraSystem(
            autoRotateSpeed = autoRotateSpeed
        ).also(configure)
    }
}

fun SceneGameDsl.freeFlyCameraSystem(
    name: String = "freeFly",
    camera: String,
    configure: FreeFlyCameraSystem.() -> Unit = {}
): SceneSystemHandle<FreeFlyCameraSystem> {
    onReady {
        val cameraEntity = requireEntity(camera)
        world.add(cameraEntity, io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl())
    }
    return system(name) {
        FreeFlyCameraSystem().also(configure)
    }
}

fun SceneGameDsl.playerControlSystem(
    name: String = "playerControl",
    rotateSpeed: Float = 0.01f,
    zoomSpeed: Float = 4f,
    pinchZoomSpeed: Float = 0.5f
): SceneSystemHandle<PlayerControlSystem> {
    return system(name) {
        PlayerControlSystem(
            rotateSpeed = rotateSpeed,
            zoomSpeed = zoomSpeed,
            pinchZoomSpeed = pinchZoomSpeed,
            inputProvider = { requireService(io.github.ronjunevaldoz.awake.core.input.Input::class).currentSnapshot },
            uiResultProvider = { uiContext.finishFrame().ownership }
        )
    }
}
