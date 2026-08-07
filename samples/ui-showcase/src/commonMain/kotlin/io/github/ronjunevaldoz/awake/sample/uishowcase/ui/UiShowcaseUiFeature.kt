// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.ui
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

internal fun uiShowcaseUiModule(state: UiShowcaseRuntimeState): GameModule = gameModule {
    ui(uiShowcaseUiSpec(state))
}
