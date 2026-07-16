// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.engine.application.requireService
import io.github.ronjunevaldoz.awake.sample.startergame.presentation.starterOverlayModel
import io.github.ronjunevaldoz.awake.sample.startergame.scene.STARTER_SCENE_SHOWCASE
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.GameUiRuntime
import io.github.ronjunevaldoz.awake.ui.GameUiSpec
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.gameUi
import io.github.ronjunevaldoz.awake.ui.metaText
import io.github.ronjunevaldoz.awake.ui.overlayBox
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.sectionTitle
import io.github.ronjunevaldoz.awake.ui.supportingLines
import io.github.ronjunevaldoz.awake.ui.toDimension

internal fun starterGameUiSpec(state: StarterGameRuntimeState): GameUiSpec {
    return gameUi {
        theme(AwakeShadcnTheme)
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
    overlayBox(viewportWidth, viewportHeight) { constraints ->
        val compact = constraints.isCompact
        val navWidth = if (compact) Dimension.FillMax else 280f.toDimension()
        val sideWidth = if (compact) Dimension.FillMax else if (showcaseActive) 560f.toDimension() else 320f.toDimension()
        val footerWidth = if (compact) Dimension.FillMax else 360f.toDimension()

        if (compact) {
            column(
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(16f.dp)
            ) {
                panel(id = "starter-nav-compact", width = navWidth, height = Dimension.WrapContent) { slot ->
                    drawStarterNavigation(model, router)
                }
                panel(id = "starter-side-compact", width = sideWidth, height = Dimension.WrapContent) { slot ->
                    drawStarterSidePanel(showcaseActive = showcaseActive, model = model, state = state)
                }
            }
            panel(
                id = "starter-footer-compact",
                width = footerWidth,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.BottomStart)
                    .padding(16f.dp)
            ) { slot ->
                drawStarterFooter(showcaseActive = showcaseActive)
            }
        } else {
            panel(
                id = "starter-nav",
                width = navWidth,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(start = 20f.dp, top = 20f.dp, end = 0f.dp, bottom = 0f.dp)
            ) { slot ->
                drawStarterNavigation(model, router)
            }

            panel(
                id = if (showcaseActive) "starter-showcase" else "starter-inspector",
                width = sideWidth,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopEnd)
                    .padding(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)
            ) { slot ->
                drawStarterSidePanel(showcaseActive = showcaseActive, model = model, state = state)
            }

            panel(
                id = "starter-footer",
                width = footerWidth,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.BottomStart)
                    .padding(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 20f.dp)
            ) { slot ->
                drawStarterFooter(showcaseActive = showcaseActive)
            }
        }
    }
}

private fun io.github.ronjunevaldoz.awake.ui.UiColumnDslScope.drawStarterNavigation(
    model: io.github.ronjunevaldoz.awake.sample.startergame.presentation.StarterOverlayModel,
    router: SceneRouterRuntime
) {
    awakeShadcnBadge("STARTER", variant = AwakeShadcnBadgeVariant.Primary)
    text("Starter Game")
    metaText(
        label = "Active: ${model.activeSceneLabel}",
        maxLines = 1
    )
    spacer(8f)
    model.sceneButtons.forEach { scene ->
        if (
            awakeShadcnButton(
                id = "scene-${scene.id}",
                label = scene.label,
                width = 0f,
                height = 32f,
                modifier = UiModifier().fillMaxWidth(),
                variant = if (scene.selected) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Secondary
            )
        ) {
            router.switchTo(scene.id)
        }
    }
}

private fun io.github.ronjunevaldoz.awake.ui.UiColumnDslScope.drawStarterSidePanel(
    showcaseActive: Boolean,
    model: io.github.ronjunevaldoz.awake.sample.startergame.presentation.StarterOverlayModel,
    state: StarterGameRuntimeState
) {
    if (showcaseActive) {
        drawStarterShadcnShowcaseContent(state)
    } else {
        awakeShadcnBadge("INSPECTOR", variant = AwakeShadcnBadgeVariant.Outline)
        sectionTitle("Scaffold")
        metaText("scene flow")
        metaText("shared UI shell")
        metaText("thin platform entrypoints")
        spacer(10f)
        val nextTipsVisible = awakeShadcnPropertyToggle(
            id = "starter-tips",
            checked = model.tipsVisible,
            label = "Show notes",
            height = 24f
        )
        if (nextTipsVisible != model.tipsVisible) {
            state.tipsVisible = nextTipsVisible
        }
        if (state.tipsVisible) {
            spacer(8f)
            supportingLines(model.notes)
        }
    }
}

private fun io.github.ronjunevaldoz.awake.ui.UiColumnDslScope.drawStarterFooter(showcaseActive: Boolean) {
    awakeShadcnBadge(if (showcaseActive) "SHOWCASE" else "REFERENCE", variant = AwakeShadcnBadgeVariant.Secondary)
    supportingLines(
        listOf(
            "Desktop: WASD / arrows / mouse",
            "WebSocket debug can switch scenes remotely",
            if (showcaseActive) {
                "Showcase scene previews the Awake shadcn component layer."
            } else {
                "Reference scaffold with a dedicated showcase scene."
            }
        )
    )
}
