// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.scene

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.scene.runtime.flow

internal fun uiShowcaseSceneModule(): GameModule {
    return gameModule {
        flow {
            start(UI_SHOWCASE_SCENE_CATALOG)
            scene(
                id = UI_SHOWCASE_SCENE_CATALOG,
                label = "Catalog",
                spec = uiShowcaseSceneSpec()
            )
        }
    }
}
