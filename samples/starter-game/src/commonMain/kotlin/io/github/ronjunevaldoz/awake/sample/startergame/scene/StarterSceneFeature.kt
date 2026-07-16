// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.scene

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.scene.runtime.flow

internal fun starterSceneModule(): GameModule {
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
        }
    }
}
