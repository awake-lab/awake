// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.WindowDsl
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.appDefinition
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.select
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

private val uiShowcaseDefinition = appDefinition(createState = ::UiShowcaseRuntimeState) {
    window {
        configureUiShowcaseWindow()
    }
    module(::uiShowcaseModule)
}

fun uiShowcase() = uiShowcaseDefinition.createApp()

fun uiShowcaseSpec(): AppSpec = uiShowcaseDefinition.createAppSpec()

private fun WindowDsl.configureUiShowcaseWindow() {
    title = "Awake UI Showcase"
    size(1600, 900)
    backend.select(platformBackendPreference())
}
