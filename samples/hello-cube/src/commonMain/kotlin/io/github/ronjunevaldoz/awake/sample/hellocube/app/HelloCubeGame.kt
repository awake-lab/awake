// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.GameWindowConfig
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.scene.runtime.ecsGameSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugInstaller
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.helloCubeSceneSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.sample.hellocube.ui.helloCubeUiSpec

val AwakeGame.helloCubeWindowConfig: GameWindowConfig
    get() = windowConfig

fun helloCubeGame(): AwakeGame = helloCubeGameSpec().createGame()

fun helloCubeGameSpec(): GameSpec {
    val state = HelloCubeRuntimeState()

    return ecsGameSpec {
        window {
            title = "Hello Cube"
            size(1600, 900)
            backend.select(platformBackendPreference())
        }
        scene(helloCubeSceneSpec(state))
        ui(helloCubeUiSpec(state))
        install(helloCubeDebugInstaller(state))
    }
}
