// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.sample.startergame.debug.starterGameDebugInstaller
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_EDITOR
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_OVERVIEW
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_PLAYGROUND
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_SHOWCASE
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterEditorSceneSpec
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterOverviewSceneSpec
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterPlaygroundSceneSpec
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterShowcaseSceneSpec
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.flow
import io.github.ronjunevaldoz.awake.ui.ui
import io.github.ronjunevaldoz.awake.sample.startergame.ui.starterGameUiSpec

internal fun starterGameModule(
    state: StarterGameRuntimeState,
    websocketControlsEnabled: Boolean = true
): GameModule {
    return gameModule {
        flow {
            start(STARTER_SCENE_OVERVIEW)
            scene(
                id = STARTER_SCENE_OVERVIEW,
                label = "Overview",
                spec = starterOverviewSceneSpec()
            )
            scene(
                id = STARTER_SCENE_EDITOR,
                label = "Editor",
                spec = starterEditorSceneSpec()
            )
            scene(
                id = STARTER_SCENE_PLAYGROUND,
                label = "Playground",
                spec = starterPlaygroundSceneSpec()
            )
            scene(
                id = STARTER_SCENE_SHOWCASE,
                label = "Showcase",
                spec = starterShowcaseSceneSpec()
            )
        }
        ui(starterGameUiSpec(state))
        install(
            starterGameDebugInstaller(
                state = state,
                websocketControlsEnabled = websocketControlsEnabled
            )
        )
    }
}
