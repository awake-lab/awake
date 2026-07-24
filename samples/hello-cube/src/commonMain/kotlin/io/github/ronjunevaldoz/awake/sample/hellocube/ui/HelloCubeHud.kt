// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui

import io.github.ronjunevaldoz.awake.engine.application.GameUiRuntime
import io.github.ronjunevaldoz.awake.engine.application.canvas
import io.github.ronjunevaldoz.awake.sample.hellocube.presentation.helloCubeOverlayModel
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.metaText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.sectionTitle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingLines
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
            modifier = (Modifier
                .align(if (constraints.isCompact) UiAlignment.TopStart else UiAlignment.TopEnd)
                .padding(
                    start = if (constraints.isCompact) 16f.dp else 0f.dp,
                    top = if (constraints.isCompact) 16f.dp else 24f.dp,
                    end = if (constraints.isCompact) 16f.dp else 40f.dp,
                    bottom = 0f.dp
                )).copy(width = panelWidth, height = Dimension.WrapContent)) {
            shadcnBadge("HELLO CUBE", variant = ShadcnBadgeVariant.Primary)
            sectionTitle("Scene")
            metaText("SCENE: ${model.sceneName}")
            metaText("MODE: ${model.modeLabel}")
            metaText("CAMERA: ${model.cameraLabel}")
        }

        surface(
            id = "hello-cube-debug",
            modifier = (Modifier
                .align(UiAlignment.BottomStart)
                .padding(
                    start = if (constraints.isCompact) 16f.dp else 20f.dp,
                    top = 0f.dp,
                    end = if (constraints.isCompact) 16f.dp else 0f.dp,
                    bottom = 12f.dp
                )).copy(width = debugWidth, height = Dimension.WrapContent)) {
            shadcnBadge("DEBUG", variant = ShadcnBadgeVariant.Outline)
            sectionTitle("Runtime")
            supportingLines(model.debugLines)
        }
    }
}

