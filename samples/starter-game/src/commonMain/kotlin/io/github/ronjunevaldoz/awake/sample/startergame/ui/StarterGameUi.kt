// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.GameUiSpec
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.engine.application.gameUi
import io.github.ronjunevaldoz.awake.sample.startergame.presentation.starterOverlayModel
import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.metaText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun starterGameUiSpec(state: StarterGameRuntimeState): GameUiSpec {
    return gameUi {
        theme(ShadcnTheme)
        overlay {
            drawStarterGameOverlay(
                router = requireService<SceneRouterRuntime>(),
                state = state
            )
        }
    }
}

internal fun GameUiRuntime.drawStarterGameOverlay(
    router: SceneRouterRuntime,
    state: StarterGameRuntimeState
) {
    val model = router.starterOverlayModel(state)
    canvas { constraints ->
        val compact = constraints.isCompact
        val navWidth = if (compact) Dimension.FillMax else 280f.toDimension()
        val sideWidth = if (compact) Dimension.FillMax else 320f.toDimension()
        val footerWidth = if (compact) Dimension.FillMax else 360f.toDimension()

        if (compact) {
            column(
                modifier = (Modifier
                    .align(UiAlignment.TopStart)
                    .padding(16f.dp)).copy(widthDimension = Dimension.FillMax, heightDimension = Dimension.WrapContent)) {
                shadcnSurface(
                    id = "starter-nav-compact",
                    variant = ShadcnSurfaceVariant.Sidebar
                , modifier = Modifier.copy(widthDimension = navWidth, heightDimension = Dimension.WrapContent)) { _ ->
                    drawStarterNavigation(model, router)
                }
                shadcnSurface(
                    id = "starter-side-compact", modifier = Modifier.copy(widthDimension = sideWidth, heightDimension = Dimension.WrapContent)) { _ ->
                    drawStarterInspectorPanel(model = model, state = state)
                }
            }
            surface(
                id = "starter-footer-compact",
                style = ShadcnStyles.surface(ShadcnSurfaceVariant.Card),
                modifier = (Modifier
                    .align(UiAlignment.BottomStart)
                    .padding(16f.dp)).copy(widthDimension = footerWidth, heightDimension = Dimension.WrapContent)) { _ ->
                drawStarterFooter()
            }
        } else {
            surface(
                id = "starter-nav",
                style = ShadcnStyles.surface(ShadcnSurfaceVariant.Sidebar),
                modifier = (Modifier
                    .align(UiAlignment.TopStart)
                    .padding(start = 20f.dp, top = 20f.dp, end = 0f.dp, bottom = 0f.dp)).copy(widthDimension = navWidth, heightDimension = Dimension.WrapContent)) { _ ->
                drawStarterNavigation(model, router)
            }

            surface(
                id = "starter-inspector",
                style = ShadcnStyles.surface(ShadcnSurfaceVariant.Card),
                modifier = (Modifier
                    .align(UiAlignment.TopEnd)
                    .padding(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)).copy(widthDimension = sideWidth, heightDimension = Dimension.WrapContent)) { _ ->
                drawStarterInspectorPanel(model = model, state = state)
            }

            surface(
                id = "starter-footer",
                style = ShadcnStyles.surface(ShadcnSurfaceVariant.Card),
                modifier = (Modifier
                    .align(UiAlignment.BottomStart)
                    .padding(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 20f.dp)).copy(widthDimension = footerWidth, heightDimension = Dimension.WrapContent)) { _ ->
                drawStarterFooter()
            }
        }
    }
}

private fun ColumnScope.drawStarterNavigation(
    model: io.github.ronjunevaldoz.awake.sample.startergame.presentation.StarterOverlayModel,
    router: SceneRouterRuntime
) {
    shadcnBadge("STARTER", variant = ShadcnBadgeVariant.Primary)
    text("Starter Game")
    metaText(
        label = "Active: ${model.activeSceneLabel}",
        maxLines = 1
    )
    spacer(Modifier.height(8f.dp))
    model.sceneButtons.forEach { scene ->
        if (
            shadcnButton(
                id = "scene-${scene.id}",
                label = scene.label,
                modifier = Modifier.fillMaxWidth().height(32f.dp),
                variant = if (scene.selected) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Secondary
            )
        ) {
            router.switchTo(scene.id)
        }
    }
}

private fun ColumnScope.drawStarterInspectorPanel(
    model: io.github.ronjunevaldoz.awake.sample.startergame.presentation.StarterOverlayModel,
    state: StarterGameRuntimeState
) {
    shadcnBadge("INSPECTOR", variant = ShadcnBadgeVariant.Outline)
    sectionTitle("Scaffold")
    metaText("scene flow")
    metaText("shared UI shell")
    metaText("thin platform entrypoints")
    spacer(Modifier.height(10f.dp))
    val nextTipsVisible = shadcnPropertyToggle(
        id = "starter-tips",
        checked = model.tipsVisible,
        height = 24f.dp
    ) {
        text("Show notes")
    }
    if (nextTipsVisible != model.tipsVisible) {
        state.tipsVisible = nextTipsVisible
    }
    if (state.tipsVisible) {
        spacer(Modifier.height(8f.dp))
        supportingLines(model.notes)
    }
}

private fun ColumnScope.drawStarterFooter() {
    shadcnBadge("REFERENCE", variant = ShadcnBadgeVariant.Secondary)
    supportingLines(
        listOf(
            "Desktop: WASD / arrows / mouse",
            "WebSocket debug can switch scenes remotely",
            "Starter shell stays focused on scene flow and shared sample scaffolding."
        )
    )
}
