// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.sample.hellocube.presentation.helloCubeOverlayModel
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.align
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.metaText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.padding
import io.github.ronjunevaldoz.awake.ui.toDimension

internal fun GameUiRuntime.drawHelloCubeOverlay(
    scene: SceneGameRuntime,
    state: HelloCubeRuntimeState
) {
    val model = scene.helloCubeOverlayModel(state)
    canvas { constraints ->
        val panelWidth = if (constraints.isCompact) Dimension.FillMax else 300f.toDimension()
        val debugWidth = if (constraints.isCompact) Dimension.FillMax else 340f.toDimension()
        surface(
            id = "hello-cube-scene",
            width = panelWidth,
            height = Dimension.WrapContent,
            modifier = UiModifier()
                .align(if (constraints.isCompact) UiAlignment.TopStart else UiAlignment.TopEnd)
                .padding(
                    start = if (constraints.isCompact) 16f.dp else 0f.dp,
                    top = if (constraints.isCompact) 16f.dp else 24f.dp,
                    end = if (constraints.isCompact) 16f.dp else 40f.dp,
                    bottom = 0f.dp
                )
        ) {
            awakeShadcnBadge("HELLO CUBE", variant = AwakeShadcnBadgeVariant.Primary)
            sectionTitle("Scene")
            metaText("SCENE: ${model.sceneName}")
            metaText("MODE: ${model.modeLabel}")
            metaText("CAMERA: ${model.cameraLabel}")
        }

        surface(
            id = "hello-cube-debug",
            width = debugWidth,
            height = Dimension.WrapContent,
            modifier = UiModifier()
                .align(UiAlignment.BottomStart)
                .padding(
                    start = if (constraints.isCompact) 16f.dp else 20f.dp,
                    top = 0f.dp,
                    end = if (constraints.isCompact) 16f.dp else 0f.dp,
                    bottom = 12f.dp
                )
        ) {
            awakeShadcnBadge("DEBUG", variant = AwakeShadcnBadgeVariant.Outline)
            sectionTitle("Runtime")
            supportingLines(model.debugLines)
        }
    }
}

