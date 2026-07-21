// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme

internal fun helloCubeUiSpec(state: HelloCubeRuntimeState): GameUiSpec {
    return gameUi {
        theme(AwakeShadcnTheme)
        overlay {
            drawHelloCubeOverlay(
                scene = requireService<SceneGameRuntime>(),
                state = state
            )
        }
    }
}
