// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.debug

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState

internal fun starterDebugModule(
    state: StarterGameRuntimeState,
    websocketControlsEnabled: Boolean = true
): GameModule {
    return gameModule {
        install(
            starterGameDebugInstaller(
                state = state,
                websocketControlsEnabled = websocketControlsEnabled
            )
        )
    }
}
