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

            is StudioContract.Intent.SetCameraMode -> {
                _state.update { it.copy(camera = it.camera.copy(mode = intent.mode)) }
            }

            is StudioContract.Intent.SetProjection -> {
                _state.update { it.copy(camera = it.camera.copy(projection = intent.projection)) }
            }

            is StudioContract.Intent.SetMode -> {
                val previous = _state.value.mode
                _state.update { it.copy(mode = intent.mode) }
                // Leaving Play reloads the scene, which is what discards a play session's edits
                // -- Unity's rule, and the reason play mode is safe to experiment in.
                if (previous == StudioContract.Mode.Play && intent.mode == StudioContract.Mode.Edit) {
                    effects.trySend(StudioContract.Effect.LoadExample(_state.value.examples.activeExampleId))
                }
            }

            StudioContract.Intent.SaveScene -> {
                effects.trySend(StudioContract.Effect.SaveScene)
            }

            is StudioContract.Intent.AppendConsole -> {
                _state.update {
                    val entry = StudioContract.ConsoleEntry(intent.level, intent.message)
                    it.copy(console = it.console.copy(entries = it.console.entries + entry))
                }
            }

            StudioContract.Intent.ClearConsole -> {
                _state.update { it.copy(console = it.console.copy(entries = emptyList())) }
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
