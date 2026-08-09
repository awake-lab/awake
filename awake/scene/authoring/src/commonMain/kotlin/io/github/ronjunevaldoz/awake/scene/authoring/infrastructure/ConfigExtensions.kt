// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.infrastructure

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.scene.authoring.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraInputSystem
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraSystem
import io.github.ronjunevaldoz.awake.scene.controls.systems.MatrixRelativeMovementSystem
import io.github.ronjunevaldoz.awake.scene.controls.systems.PlayerInputSystem
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle

fun SceneGameDsl.cameraSystem(
    name: String = "camera",
): SceneSystemHandle<CameraSystem> = frameSystem(name) {
    CameraSystem(
        inputProvider = { requireService(Input::class).currentSnapshot },
        uiResultProvider = { uiContext.finishFrame().ownership },
    )
}

fun SceneGameDsl.cameraInputSystem(
    name: String = "cameraInput",
): SceneSystemHandle<CameraInputSystem> = frameSystem(name) {
    CameraInputSystem(
        inputProvider = { requireService(Input::class).currentSnapshot },
        uiResultProvider = { uiContext.finishFrame().ownership },
    )
}

fun SceneGameDsl.playerInputSystem(
    name: String = "playerInput",
): SceneSystemHandle<PlayerInputSystem> = frameSystem(name) {
    PlayerInputSystem(
        inputProvider = { requireService(Input::class).currentSnapshot },
        uiResultProvider = { uiContext.finishFrame().ownership },
    )
}

fun SceneGameDsl.matrixRelativeMovementSystem(
    name: String = "movement",
    speed: Float = 5f,
): SceneSystemHandle<MatrixRelativeMovementSystem> = frameSystem(name) {
    MatrixRelativeMovementSystem(speed = speed)
}
