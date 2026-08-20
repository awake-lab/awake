// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.WindowDsl
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.appDefinition
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.select
import io.github.ronjunevaldoz.awake.studio.studioModule

private val studioDefinition = appDefinition(createState = {}) {
    window {
        configureStudioWindow()
    }
    module(studioModule())
}

fun studioApp(): AwakeAppLifecycle = studioDefinition.createApp()

private fun WindowDsl.configureStudioWindow() {
    title = "Awake Studio"
    @Suppress("MagicNumber") // Default window size, used exactly once.
    size(1600, 900)
    backend.select(platformBackendPreference())
}
