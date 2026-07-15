// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.createGame
import io.github.ronjunevaldoz.awake.engine.application.createGameSpec
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

fun helloCubeGame() = HelloCubeRuntimeState().let { state ->
    helloCubeGameModule(state).createGame {
        title = "Hello Cube"
        size(1600, 900)
        backend.select(platformBackendPreference())
    }
}

fun helloCubeGameSpec(): GameSpec = HelloCubeRuntimeState().let { state ->
    helloCubeGameModule(state).createGameSpec {
        title = "Hello Cube"
        size(1600, 900)
        backend.select(platformBackendPreference())
    }
}
