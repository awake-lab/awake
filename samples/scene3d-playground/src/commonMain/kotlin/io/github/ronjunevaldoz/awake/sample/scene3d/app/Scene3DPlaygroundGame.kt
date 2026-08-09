// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.game.GameSpec
import io.github.ronjunevaldoz.awake.engine.gameauthoring.WindowDsl
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameDefinition
import io.github.ronjunevaldoz.awake.engine.gameauthoring.select
import io.github.ronjunevaldoz.awake.sample.scene3d.scene3DPlaygroundModule

private val scene3DPlaygroundDefinition = gameDefinition(createState = {}) {
    window {
        configureScene3DPlaygroundWindow()
    }
    module(scene3DPlaygroundModule())
}

fun scene3DPlayground() = scene3DPlaygroundDefinition.createGame()

fun scene3DPlaygroundSpec(): GameSpec = scene3DPlaygroundDefinition.createGameSpec()

private fun WindowDsl.configureScene3DPlaygroundWindow() {
    title = "Awake 3D Playground"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
