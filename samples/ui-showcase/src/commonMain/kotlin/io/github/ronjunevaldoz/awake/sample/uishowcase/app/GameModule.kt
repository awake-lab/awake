// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState
import io.github.ronjunevaldoz.awake.sample.uishowcase.ui.uiShowcaseUiModule

internal fun uiShowcaseModule(state: UiShowcaseRuntimeState): GameModule = gameModule {
    module(uiShowcaseUiModule(state))
    // 3D content (the old background rotating-cube scene) moved out of this sample --
    // ui-showcase is the 2D UI component catalog only now. See samples/scene3d-playground/,
    // a standalone sibling sample app, for where that content lives now.
}
