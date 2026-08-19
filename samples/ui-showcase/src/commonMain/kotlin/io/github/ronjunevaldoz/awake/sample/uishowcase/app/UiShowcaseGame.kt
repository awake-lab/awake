// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.gameauthoring.WindowDsl
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameDefinition
import io.github.ronjunevaldoz.awake.engine.gameauthoring.select
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

private val uiShowcaseDefinition = gameDefinition(createState = ::UiShowcaseRuntimeState) {
    window {
        configureUiShowcaseWindow()
    }
    module(::uiShowcaseModule)
}

fun uiShowcase() = uiShowcaseDefinition.createGame()

fun uiShowcaseSpec(): AppSpec = uiShowcaseDefinition.createGameSpec()

private fun WindowDsl.configureUiShowcaseWindow() {
    title = "Awake UI Showcase"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
