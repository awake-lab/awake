// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.sample.uishowcase.scene.uiShowcaseSceneModule
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.uiShowcaseUiModule

internal fun uiShowcaseModule(state: UiShowcaseRuntimeState): GameModule {
    return gameModule {
        module(uiShowcaseSceneModule())
        module(uiShowcaseUiModule(state))
    }
}
