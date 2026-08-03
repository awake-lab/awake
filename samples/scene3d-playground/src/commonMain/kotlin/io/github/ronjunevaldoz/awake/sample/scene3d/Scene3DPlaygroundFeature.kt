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
/** Lightest gray, same hue as [RotatingCubeDemo]'s own grid color (0.55, 0.55, 0.6) --
 * [RotatingCubeDemo]'s camera can orbit/pitch above its ground grid into empty space with
 * nothing drawn there, which otherwise reads as stark solid black (a real repro, not a
 * hypothetical). Only set while that demo is active (see below); every other page/demo leaves
 * [io.github.ronjunevaldoz.awake.render.renderer.Renderer.clearColor] at its
 * [DEFAULT_CLEAR_COLOR] default. */
private val SKY_CLEAR_COLOR = floatArrayOf(0.92f, 0.92f, 0.94f, 1f)
private val DEFAULT_CLEAR_COLOR = floatArrayOf(0f, 0f, 0f, 1f)

internal fun scene3DPlaygroundModule(): GameModule {
    val state = Scene3DPlaygroundState()
    return gameModule {
        ui {
            overlay {
                // Frame stats are always visible here (see Scene3DPlaygroundUi.kt's own badge,
                // pinned to the viewport pane's corner via frameStats()) -- NOT via
                // perfStatsEnabled/the built-in HUD, which anchors to the whole window (frame{}'s
                // root box) and would land over the sidebar/controls panes instead of the
                // viewport in this 3-column shell, plus drags in UiMeasureTrialStats/text-cache
                // debug numbers this playground's simple fps/frame-time badge doesn't want.

                // Only [RotatingCubeDemo] currently has real 3D geometry to submit through
                // [io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime.provideDrawCalls] --
                // gated on it being the active demo so its cube/grid don't keep drawing (and
                // wasting a lazy-build call) on every other playground page.
                if (RotatingCubeDemo.isActive(state.activeDemoId)) {
                    renderer.clearColor = SKY_CLEAR_COLOR
                    RotatingCubeDemo.update(renderer, deltaSeconds)
                    provideDrawCalls = { RotatingCubeDemo.cameraAndDrawCalls() }
                } else {
                    renderer.clearColor = DEFAULT_CLEAR_COLOR
                    provideDrawCalls = null
                }
                drawScene3DPlaygroundOverlay(state)
            }
        }
    }
}
