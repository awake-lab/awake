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
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.AwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.components.input.awakeShadcnPropertyToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.metaText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.column
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.toDimension
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

internal fun starterGameUiSpec(state: StarterGameRuntimeState): GameUiSpec {
    return gameUi {
        theme(AwakeShadcnTheme)
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
                width = Dimension.FillMax,
                height = Dimension.WrapContent,
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(16f.dp)
            ) {
                awakeShadcnSurface(
                    id = "starter-nav-compact",
                    width = navWidth,
                    height = Dimension.WrapContent,
                    variant = AwakeShadcnSurfaceVariant.Sidebar
                ) { _ ->
                    drawStarterNavigation(model, router)
                }
                awakeShadcnSurface(
                    id = "starter-side-compact",
                    width = sideWidth,
                    height = Dimension.WrapContent
                ) { _ ->
                    drawStarterInspectorPanel(model = model, state = state)
                }
            }
            surface(
                id = "starter-footer-compact",
                width = footerWidth,
                height = Dimension.WrapContent,
                style = AwakeShadcnStyles.surface(AwakeShadcnSurfaceVariant.Card),
                modifier = UiModifier()
                    .align(UiAlignment.BottomStart)
                    .padding(16f.dp)
            ) { _ ->
                drawStarterFooter()
            }
        } else {
            surface(
                id = "starter-nav",
                width = navWidth,
                height = Dimension.WrapContent,
                style = AwakeShadcnStyles.surface(AwakeShadcnSurfaceVariant.Sidebar),
                modifier = UiModifier()
                    .align(UiAlignment.TopStart)
                    .padding(start = 20f.dp, top = 20f.dp, end = 0f.dp, bottom = 0f.dp)
            ) { _ ->
                drawStarterNavigation(model, router)
            }

            surface(
                id = "starter-inspector",
                width = sideWidth,
                height = Dimension.WrapContent,
                style = AwakeShadcnStyles.surface(AwakeShadcnSurfaceVariant.Card),
                modifier = UiModifier()
                    .align(UiAlignment.TopEnd)
                    .padding(start = 0f.dp, top = 20f.dp, end = 20f.dp, bottom = 0f.dp)
            ) { _ ->
                drawStarterInspectorPanel(model = model, state = state)
            }

            surface(
                id = "starter-footer",
                width = footerWidth,
                height = Dimension.WrapContent,
                style = AwakeShadcnStyles.surface(AwakeShadcnSurfaceVariant.Card),
                modifier = UiModifier()
                    .align(UiAlignment.BottomStart)
                    .padding(start = 20f.dp, top = 0f.dp, end = 0f.dp, bottom = 20f.dp)
            ) { _ ->
                drawStarterFooter()
            }
        }
    }
}

private fun ColumnScope.drawStarterNavigation(
    model: io.github.ronjunevaldoz.awake.sample.startergame.presentation.StarterOverlayModel,
    router: SceneRouterRuntime
) {
    awakeShadcnBadge("STARTER", variant = AwakeShadcnBadgeVariant.Primary)
    text("Starter Game")
    metaText(
        label = "Active: ${model.activeSceneLabel}",
        maxLines = 1
    )
    spacer(UiModifier().height(8f.dp))
    model.sceneButtons.forEach { scene ->
        if (
            awakeShadcnButton(
                id = "scene-${scene.id}",
                label = scene.label,
                modifier = UiModifier().fillMaxWidth().height(32f.dp),
                variant = if (scene.selected) AwakeShadcnButtonVariant.Primary else AwakeShadcnButtonVariant.Secondary
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
    awakeShadcnBadge("INSPECTOR", variant = AwakeShadcnBadgeVariant.Outline)
    sectionTitle("Scaffold")
    metaText("scene flow")
    metaText("shared UI shell")
    metaText("thin platform entrypoints")
    spacer(UiModifier().height(10f.dp))
    val nextTipsVisible = awakeShadcnPropertyToggle(
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
        spacer(UiModifier().height(8f.dp))
        supportingLines(model.notes)
    }
}

private fun ColumnScope.drawStarterFooter() {
    awakeShadcnBadge("REFERENCE", variant = AwakeShadcnBadgeVariant.Secondary)
    supportingLines(
        listOf(
            "Desktop: WASD / arrows / mouse",
            "WebSocket debug can switch scenes remotely",
            "Starter shell stays focused on scene flow and shared sample scaffolding."
        )
    )
}
