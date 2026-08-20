// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.appModule
import io.github.ronjunevaldoz.awake.engine.platformauthoring.ui.ui
import io.github.ronjunevaldoz.awake.sample.uishowcase.state.UiShowcaseRuntimeState

internal fun uiShowcaseUiModule(state: UiShowcaseRuntimeState): AppModule = appModule {
    ui(uiShowcaseUiSpec(state))
}
