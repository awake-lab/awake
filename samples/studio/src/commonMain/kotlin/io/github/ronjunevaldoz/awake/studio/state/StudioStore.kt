// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.state

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class StudioStore {
    private val _state = MutableStateFlow(StudioContract.State())
    val state: StateFlow<StudioContract.State> = _state.asStateFlow()

    private val effects = Channel<StudioContract.Effect>(Channel.BUFFERED)

    fun dispatch(intent: StudioContract.Intent) {
        when (intent) {
            is StudioContract.Intent.SelectExample -> {
                _state.update { it.copy(examples = it.examples.copy(activeExampleId = intent.id)) }
                effects.trySend(StudioContract.Effect.LoadExample(intent.id))
            }

            is StudioContract.Intent.SelectEntity -> {
                _state.update { it.copy(inspector = it.inspector.copy(selectedEntityId = intent.id)) }
            }

            is StudioContract.Intent.SelectTool -> {
                _state.update { it.copy(toolRail = it.toolRail.copy(activeTool = intent.tool)) }
            }

            is StudioContract.Intent.SetCameraMode -> {
                _state.update { it.copy(camera = it.camera.copy(mode = intent.mode)) }
            }

            is StudioContract.Intent.OrbitBy -> {
                _state.update {
                    val camera = it.camera
                    val pitch = (camera.pitch + intent.deltaPitch)
                        .coerceIn(-StudioContract.PITCH_LIMIT_RADIANS, StudioContract.PITCH_LIMIT_RADIANS)
                    it.copy(camera = camera.copy(yaw = camera.yaw + intent.deltaYaw, pitch = pitch))
                }
            }

            is StudioContract.Intent.SetProjection -> {
                _state.update { it.copy(camera = it.camera.copy(projection = intent.projection)) }
            }
        }
    }

    fun drainEffects(): List<StudioContract.Effect> = buildList {
        while (true) {
            val effect = effects.tryReceive().getOrNull() ?: break
            add(effect)
        }
    }
}
