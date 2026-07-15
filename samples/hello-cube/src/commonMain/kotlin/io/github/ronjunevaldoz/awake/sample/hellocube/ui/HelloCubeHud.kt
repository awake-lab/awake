// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.overlayShell
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.shellPane
import io.github.ronjunevaldoz.awake.ui.textLines
import io.github.ronjunevaldoz.awake.ui.designsystem.DefaultUiTheme
import io.github.ronjunevaldoz.awake.sample.hellocube.presentation.helloCubeOverlayModel
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val model = scene.helloCubeOverlayModel(state)
    overlayShell(viewportWidth, viewportHeight) {
        topRight(
            width = 280f,
            height = 112f,
            margin = UiInsets(start = 0f.dp, top = 24f.dp, end = 40f.dp, bottom = 0f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "hello-cube-status",
                theme = DefaultUiTheme,
                textScale = HELLO_CUBE_TEXT_SCALE
            ) {
                sectionTitle("Scene")
                text("SCENE: ${model.sceneName}")
                text("MODE: ${model.modeLabel}")
                text("CAMERA: ${model.cameraLabel}")
            }
        }

        bottomLeft(
            width = 320f,
            height = 120f,
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 12f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "hello-cube-debug",
                theme = DefaultUiTheme,
                textScale = HELLO_CUBE_TEXT_SCALE
            ) {
                sectionTitle("Debug")
                textLines(model.debugLines)
            }
        }
    }
}

private const val HELLO_CUBE_TEXT_SCALE = 2f
