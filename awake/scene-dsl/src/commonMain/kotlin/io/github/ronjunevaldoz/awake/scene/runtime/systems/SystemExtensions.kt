// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.systems

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle
import io.github.ronjunevaldoz.awake.scene.systems.CameraInputSystem
import io.github.ronjunevaldoz.awake.scene.systems.CameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.MatrixRelativeMovementSystem
import io.github.ronjunevaldoz.awake.scene.systems.PlayerInputSystem

fun SceneGameDsl.cameraSystem(
    name: String = "camera"
): SceneSystemHandle<CameraSystem> {
    return frameSystem(name) {
        CameraSystem(
            inputProvider = { requireService(Input::class).currentSnapshot },
            uiResultProvider = { uiContext.finishFrame().ownership }
        )
    }
}

fun SceneGameDsl.cameraInputSystem(
    name: String = "cameraInput"
): SceneSystemHandle<CameraInputSystem> {
    return frameSystem(name) {
        CameraInputSystem(
            inputProvider = { requireService(Input::class).currentSnapshot },
            uiResultProvider = { uiContext.finishFrame().ownership }
        )
    }
}

fun SceneGameDsl.playerInputSystem(
    name: String = "playerInput"
): SceneSystemHandle<PlayerInputSystem> {
    return frameSystem(name) {
        PlayerInputSystem(
            inputProvider = { requireService(Input::class).currentSnapshot },
            uiResultProvider = { uiContext.finishFrame().ownership }
        )
    }
}

fun SceneGameDsl.matrixRelativeMovementSystem(
    name: String = "movement",
    speed: Float = 5f
): SceneSystemHandle<MatrixRelativeMovementSystem> {
    return frameSystem(name) {
        MatrixRelativeMovementSystem(speed = speed)
    }
}
