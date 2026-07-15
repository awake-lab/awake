// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.createGame
import io.github.ronjunevaldoz.awake.engine.application.createGameSpec
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState

fun starterGame() = StarterGameRuntimeState().let { state ->
    starterGameModule(state).createGame {
        configureStarterGameWindow()
    }
}

fun starterGameSpec(): GameSpec = StarterGameRuntimeState().let { state ->
    starterGameModule(state).createGameSpec {
        configureStarterGameWindow()
    }
}

private fun WindowDsl.configureStarterGameWindow() {
    title = "Awake Starter Game"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
