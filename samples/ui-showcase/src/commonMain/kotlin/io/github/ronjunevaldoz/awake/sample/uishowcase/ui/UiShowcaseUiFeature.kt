// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.game.GameModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.ui
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

internal fun uiShowcaseUiModule(state: UiShowcaseRuntimeState): GameModule = gameModule {
    ui(uiShowcaseUiSpec(state))
}
