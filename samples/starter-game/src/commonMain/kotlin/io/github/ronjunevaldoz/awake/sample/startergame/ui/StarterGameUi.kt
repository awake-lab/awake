// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.startergame.presentation.starterOverlayModel
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_SHOWCASE
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.UiInsets
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
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
    val showcaseActive = router.activeSceneId == STARTER_SCENE_SHOWCASE
    overlayShell(viewportWidth, viewportHeight) {
        topLeft(
            width = 280f,
            height = 260f,
            margin = UiInsets(start = 20f.dp, top = 20f.dp, end = 0f.dp, bottom = 0f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "starter-nav",
                theme = AwakeShadcnTheme
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

        if (showcaseActive) {
            topRight(
                width = 560f,
                height = 620f,
                margin = UiInsets(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)
            ) { slot ->
                drawStarterShadcnShowcase(slot, state)
            }
        } else {
            topRight(
                width = 320f,
                height = 228f,
                margin = UiInsets(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)
            ) { slot ->
                shellPane(
                    slot = slot,
                    id = "starter-inspector",
                    theme = AwakeShadcnTheme
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
        }

        bottomLeft(
            width = 360f,
            height = 96f,
            margin = UiInsets(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 20f.dp)
        ) { slot ->
            shellPane(
                slot = slot,
                id = "starter-footer",
                theme = AwakeShadcnTheme
            ) {
                text("Desktop: WASD / arrows / mouse")
                text("WebSocket debug can switch scenes remotely")
                text(if (showcaseActive) "Showcase scene previews the Awake shadcn component layer" else "Reference scaffold with a dedicated showcase scene")
            }
        }
    }
}
