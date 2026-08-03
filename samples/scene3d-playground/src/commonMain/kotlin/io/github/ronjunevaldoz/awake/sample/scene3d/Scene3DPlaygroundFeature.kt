// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.ui

/** The whole app -- this module's `app/Main.kt`/`app/main.kt` platform entry points install
 * this directly (see [io.github.ronjunevaldoz.awake.engine.application.gameDefinition]'s
 * `module(...)` call). */
internal fun scene3DPlaygroundModule(): GameModule {
    val state = Scene3DPlaygroundState()
    return gameModule {
        ui {
            overlay {
                drawScene3DPlaygroundOverlay(state)
            }
        }
    }
}
