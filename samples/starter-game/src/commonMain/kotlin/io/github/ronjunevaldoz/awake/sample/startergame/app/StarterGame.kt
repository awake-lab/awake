// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.gameDefinition
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState

private val starterGameDefinition = gameDefinition(createState = ::StarterGameRuntimeState) {
    window {
        configureStarterGameWindow()
    }
    module(::starterGameModule)
}

fun starterGame() = starterGameDefinition.createGame()

fun starterGameSpec(): GameSpec = starterGameDefinition.createGameSpec()

private fun WindowDsl.configureStarterGameWindow() {
    title = "Awake Starter Game"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
