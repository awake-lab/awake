// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.UiAnchor
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.anchored
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.textBlockHeight

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val overlayBounds = UiSlot(0f, 0f, viewportWidth, viewportHeight)
    val model = scene.helloCubeOverlayModel(state)
    column(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.TopRight,
            width = 280f,
            height = font.textBlockHeight(lineCount = 3, textScale = HELLO_CUBE_TEXT_SCALE),
            margin = UiInsets(start = 0f.dp, top = 24f.dp, end = 40f.dp, bottom = 0f.dp)
        ),
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        text("SCENE: ${model.sceneName}")
        text("MODE: ${model.modeLabel}")
        text("CAMERA: ${model.cameraLabel}")
    }

    column(
        slot = overlayBounds.anchored(
            anchor = UiAnchor.BottomLeft,
            width = 320f,
            height = font.textBlockHeight(lineCount = model.debugLines.size, textScale = HELLO_CUBE_TEXT_SCALE),
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 12f.dp)
        ),
        textScale = HELLO_CUBE_TEXT_SCALE
    ) {
        model.debugLines.forEach { line ->
            text(line)
        }
    }
}

private const val HELLO_CUBE_TEXT_SCALE = 2f
