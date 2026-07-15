// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.gameDefinition
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

private val helloCubeDefinition = gameDefinition(createState = ::HelloCubeRuntimeState) {
    window {
        configureHelloCubeWindow()
    }
    module(::helloCubeGameModule)
}

fun helloCubeGame() = helloCubeDefinition.createGame()

fun helloCubeGameSpec(): GameSpec = helloCubeDefinition.createGameSpec()

internal fun WindowDsl.configureHelloCubeWindow() {
    title = "Hello Cube"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
