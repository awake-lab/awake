// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.scene.runtime.ecs
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.helloCubeDebugInstaller
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.helloCubeSceneSpec
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.sample.hellocube.ui.helloCubeUiSpec
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column

internal fun helloCubeGameModule(
    state: HelloCubeRuntimeState,
    websocketControlsEnabled: Boolean = true,
    offscreenProofEnabled: Boolean = true
): GameModule {
    return gameModule {
        ecs(helloCubeSceneSpec(state))
        column(helloCubeUiSpec(state))
        install(
            helloCubeDebugInstaller(
                state = state,
                websocketControlsEnabled = websocketControlsEnabled,
                offscreenProofEnabled = offscreenProofEnabled
            )
        )
    }
}
