// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.game
import io.github.ronjunevaldoz.awake.engine.application.gameSpec
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.scene.runtime.ecs
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugInstaller
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.helloCubeSceneSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.sample.hellocube.ui.helloCubeUiSpec
import io.github.ronjunevaldoz.awake.ui.ui

fun helloCubeGame() = game {
    val state = HelloCubeRuntimeState()
    helloCubeGame(state)
}

fun helloCubeGameSpec(): GameSpec {
    val state = HelloCubeRuntimeState()
    return gameSpec {
        helloCubeGame(state)
    }
}

private fun io.github.ronjunevaldoz.awake.engine.application.GameDsl.helloCubeGame(
    state: HelloCubeRuntimeState
) {
    window {
        title = "Hello Cube"
        size(1600, 900)
        backend.select(platformBackendPreference())
    }
    ecs(helloCubeSceneSpec(state))
    ui(helloCubeUiSpec(state))
    install(helloCubeDebugInstaller(state))
}
