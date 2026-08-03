// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.engine.application.ui
import io.github.ronjunevaldoz.awake.sample.scene3d.demos.RotatingCubeDemo

/** The whole app -- this module's `app/Main.kt`/`app/main.kt` platform entry points install
 * this directly (see [io.github.ronjunevaldoz.awake.engine.application.gameDefinition]'s
 * `module(...)` call). */
internal fun scene3DPlaygroundModule(): GameModule {
    val state = Scene3DPlaygroundState()
    return gameModule {
        ui {
            overlay {
                // Only [RotatingCubeDemo] currently has real 3D geometry to submit through
                // [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls] --
                // gated on it being the active demo so its cube/grid don't keep drawing (and
                // wasting a lazy-build call) on every other playground page.
                if (RotatingCubeDemo.isActive(state.activeDemoId)) {
                    RotatingCubeDemo.update(renderer, deltaSeconds)
                    provideDrawCalls = { RotatingCubeDemo.cameraAndDrawCalls() }
                } else {
                    provideDrawCalls = null
                }
                drawScene3DPlaygroundOverlay(state)
            }
        }
    }
}
