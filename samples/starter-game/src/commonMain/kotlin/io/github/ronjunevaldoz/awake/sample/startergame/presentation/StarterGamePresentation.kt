// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.presentation

import io.github.ronjunevaldoz.awake.sample.startergame.state.StarterGameRuntimeState
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRouterRuntime

internal data class StarterSceneButtonModel(
    val id: String,
    val label: String,
    val selected: Boolean
)

internal data class StarterOverlayModel(
    val activeSceneLabel: String,
    val sceneButtons: List<StarterSceneButtonModel>,
    val tipsVisible: Boolean,
    val notes: List<String>
)

internal fun SceneRouterRuntime.starterOverlayModel(state: StarterGameRuntimeState): StarterOverlayModel {
    return StarterOverlayModel(
        activeSceneLabel = activeSceneLabel,
        sceneButtons = scenes.map { scene ->
            StarterSceneButtonModel(
                id = scene.id,
                label = scene.label,
                selected = scene.id == activeSceneId
            )
        },
        tipsVisible = state.tipsVisible,
        notes = listOf(
            "Scene switching is owned by common code.",
            "Desktop input + debug control now live in reusable hosts.",
            "Each platform entrypoint only boots the shared sample."
        )
    )
}
