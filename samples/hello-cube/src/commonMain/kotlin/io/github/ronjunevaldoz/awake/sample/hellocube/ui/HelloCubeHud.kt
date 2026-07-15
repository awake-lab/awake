// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.UiAnchor
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.anchored
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.shellPane
import io.github.ronjunevaldoz.awake.ui.textLines
import io.github.ronjunevaldoz.awake.sample.hellocube.presentation.helloCubeOverlayModel
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val overlayBounds = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    val model = scene.helloCubeOverlayModel(state)
    shellPane(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.TopRight,
            width = 280f,
            height = 112f,
            margin = UiInsets(start = 0f.dp, top = 24f.dp, end = 40f.dp, bottom = 0f.dp)
        ),
        id = "hello-cube-status",
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        sectionTitle("Scene")
        text("SCENE: ${model.sceneName}")
        text("MODE: ${model.modeLabel}")
        text("CAMERA: ${model.cameraLabel}")
    }

    shellPane(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.BottomLeft,
            width = 320f,
            height = 120f,
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 12f.dp)
        ),
        id = "hello-cube-debug",
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        sectionTitle("Debug")
        textLines(model.debugLines)
    }
}

private const val HELLO_CUBE_TEXT_SCALE = 2f
