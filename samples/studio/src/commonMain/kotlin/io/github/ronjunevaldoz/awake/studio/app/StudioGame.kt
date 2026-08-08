// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.application.GameSpec
import io.github.ronjunevaldoz.awake.engine.application.WindowDsl
import io.github.ronjunevaldoz.awake.engine.application.gameDefinition
import io.github.ronjunevaldoz.awake.engine.application.select
import io.github.ronjunevaldoz.awake.studio.studioModule

private val studioDefinition = gameDefinition(createState = {}) {
    window {
        configureStudioWindow()
    }
    module(studioModule())
}

fun studioGame() = studioDefinition.createGame()

fun studioGameSpec(): GameSpec = studioDefinition.createGameSpec()

private fun WindowDsl.configureStudioWindow() {
    title = "Awake Studio"
    @Suppress("MagicNumber") // Default window size, used exactly once.
    size(1600, 900)
    backend.select(platformBackendPreference())
}
