// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.ui
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

internal fun uiShowcaseUiModule(state: UiShowcaseRuntimeState): AppModule = gameModule {
    ui(uiShowcaseUiSpec(state))
}
