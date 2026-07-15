// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.debug

import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.engine.application.gameInstaller
import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal fun helloCubeDebugInstaller(
    state: HelloCubeRuntimeState,
    websocketControlsEnabled: Boolean = true,
    offscreenProofEnabled: Boolean = true
): GameInstaller {
    return gameInstaller {
        val runtime = requireService(SceneGameRuntime::class)
        val debugConfig = HelloCubeDebugConfig(
            websocketControlsEnabled = websocketControlsEnabled,
            offscreenProofEnabled = offscreenProofEnabled
        )
        service(HelloCubeDebugConfig::class, debugConfig)
        service(HelloCubeDebugController::class, HelloCubeDebugController(runtime, state))
        ready {
            if (debugConfig.offscreenProofEnabled) {
                runtime.verifyHelloCubeOffscreenReadback()
            }
        }
    }
}
