// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.startergame.presentation.starterOverlayModel
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.DefaultUiTheme
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.overlayShell
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.shellPane
import io.github.ronjunevaldoz.awake.ui.textLines

internal fun starterGameUiSpec(state: StarterGameRuntimeState): GameUiSpec {
    return gameUi {
        overlay { viewportWidth, viewportHeight ->
            drawStarterGameOverlay(
                router = requireService<SceneRouterRuntime>(),
                state = state,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight
            )
        }
    }
}

internal fun GameUiRuntime.drawStarterGameOverlay(
    router: SceneRouterRuntime,
    state: StarterGameRuntimeState,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val model = router.starterOverlayModel(state)
    overlayShell(viewportWidth, viewportHeight) {
        topLeft(
            width = 280f,
            height = 260f,
            margin = UiInsets(start = 20f.dp, top = 20f.dp, end = 0f.dp, bottom = 0f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "starter-nav",
                theme = DefaultUiTheme
            ) {
                text("Starter Game")
                text("Active: ${model.activeSceneLabel}")
                spacer(8f)
                model.sceneButtons.forEach { scene ->
                    if (
                        button(
                            id = "scene-${scene.id}",
                            label = scene.label,
                            width = 224f,
                            height = 32f,
                            variant = if (scene.selected) UiButtonVariant.Filled else UiButtonVariant.Outline
                        )
                    ) {
                        router.switchTo(scene.id)
                    }
                }
            }
        }

        topRight(
            width = 320f,
            height = 228f,
            margin = UiInsets(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "starter-inspector",
                theme = DefaultUiTheme
            ) {
                sectionTitle("Scaffold")
                text("scene flow")
                text("shared UI shell")
                text("thin platform entrypoints")
                spacer(10f)
                val nextTipsVisible = checkbox(
                    id = "starter-tips",
                    checked = model.tipsVisible,
                    label = "Show notes",
                    width = 220f,
                    height = 24f
                )
                if (nextTipsVisible != model.tipsVisible) {
                    state.tipsVisible = nextTipsVisible
                }
                if (state.tipsVisible) {
                    spacer(8f)
                    textLines(model.notes)
                }
            }
        }

        bottomLeft(
            width = 360f,
            height = 96f,
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 20f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "starter-footer",
                theme = DefaultUiTheme
            ) {
                text("Desktop: WASD / arrows / mouse")
                text("WebSocket debug can switch scenes remotely")
                text("This sample is the reference scaffold, not a showcase")
            }
        }
    }
}
