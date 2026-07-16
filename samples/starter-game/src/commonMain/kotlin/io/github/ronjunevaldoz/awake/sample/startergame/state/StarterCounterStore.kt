// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.state

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class StarterCounterStore {
    private val _state = MutableStateFlow(StarterCounterContract.State())
    val state: StateFlow<StarterCounterContract.State> = _state.asStateFlow()

    private val effects = Channel<StarterCounterContract.Effect>(Channel.BUFFERED)

    fun dispatch(intent: StarterCounterContract.Intent) {
        when (intent) {
            StarterCounterContract.Intent.Increment -> {
                val nextState = updateState { current ->
                    current.copy(count = current.count + 1)
                }
                if (nextState.count > 0 && nextState.count % 5 == 0) {
                    effects.trySend(StarterCounterContract.Effect.MilestoneReached(nextState.count))
                }
            }

            StarterCounterContract.Intent.Decrement -> {
                updateState { current ->
                    current.copy(count = current.count - 1)
                }
            }

            StarterCounterContract.Intent.Reset -> {
                val previous = _state.value
                updateState { StarterCounterContract.State() }
                if (previous.count != 0) {
                    effects.trySend(StarterCounterContract.Effect.ResetCompleted)
                }
            }
        }
    }

    fun drainEffects(): List<StarterCounterContract.Effect> = buildList {
        while (true) {
            val effect = effects.tryReceive().getOrNull() ?: break
            add(effect)
        }
    }

    private inline fun updateState(
        update: (StarterCounterContract.State) -> StarterCounterContract.State
    ): StarterCounterContract.State {
        var nextState = _state.value
        _state.update { current ->
            update(current).also { nextState = it }
        }
        return nextState
    }
}
