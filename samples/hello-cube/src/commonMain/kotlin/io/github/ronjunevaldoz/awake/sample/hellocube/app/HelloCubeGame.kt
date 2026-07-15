// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.scene.runtime.ecsGame
import io.github.ronjunevaldoz.awake.scene.runtime.ecsGameSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugInstaller
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.helloCubeSceneSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.sample.hellocube.ui.helloCubeUiSpec

fun helloCubeGame() = HelloCubeRuntimeState().let { state ->
    ecsGame {
        window {
            configureHelloCubeWindow()
        }
        scene(helloCubeSceneSpec(state))
        ui(helloCubeUiSpec(state))
        install(helloCubeDebugInstaller(state))
    }
}

fun helloCubeGameSpec(): GameSpec = HelloCubeRuntimeState().let { state ->
    ecsGameSpec {
        window {
            configureHelloCubeWindow()
        }
        scene(helloCubeSceneSpec(state))
        ui(helloCubeUiSpec(state))
        install(helloCubeDebugInstaller(state))
    }
}

internal fun WindowDsl.configureHelloCubeWindow() {
    title = "Hello Cube"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
