// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal fun helloCubeUiSpec(state: HelloCubeRuntimeState): GameUiSpec {
    return gameUi {
        overlay { viewportWidth, viewportHeight ->
            drawHelloCubeOverlay(
                scene = requireService<SceneGameRuntime>(),
                state = state,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight
            )
        }
    }
}
