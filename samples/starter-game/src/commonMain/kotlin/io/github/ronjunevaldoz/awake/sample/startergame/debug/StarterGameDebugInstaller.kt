// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.debug

import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.gameInstaller
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime

internal fun starterGameDebugInstaller(
    state: StarterGameRuntimeState,
    websocketControlsEnabled: Boolean = true
): GameInstaller {
    return gameInstaller {
        val router = requireService(SceneRouterRuntime::class)
        service(
            StarterGameDebugConfig::class,
            StarterGameDebugConfig(
                websocketControlsEnabled = websocketControlsEnabled
            )
        )
        service(
            StarterGameDebugController::class,
            StarterGameDebugController(router, state)
        )
    }
}
