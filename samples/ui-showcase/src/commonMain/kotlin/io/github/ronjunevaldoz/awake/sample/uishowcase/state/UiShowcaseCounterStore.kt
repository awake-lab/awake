// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class UiShowcaseCounterStore {
    private val _state = MutableStateFlow(UiShowcaseCounterContract.State())
    val state: StateFlow<UiShowcaseCounterContract.State> = _state.asStateFlow()

    private val effects = Channel<UiShowcaseCounterContract.Effect>(Channel.BUFFERED)

    fun dispatch(intent: UiShowcaseCounterContract.Intent) {
        when (intent) {
            UiShowcaseCounterContract.Intent.Increment -> {
                val nextState = updateState { current ->
                    current.copy(count = current.count + 1)
                }
                if (nextState.count > 0 && nextState.count % 5 == 0) {
                    effects.trySend(UiShowcaseCounterContract.Effect.MilestoneReached(nextState.count))
                }
            }

            UiShowcaseCounterContract.Intent.Decrement -> {
                updateState { current ->
                    current.copy(count = current.count - 1)
                }
            }

            UiShowcaseCounterContract.Intent.Reset -> {
                val previous = _state.value
                updateState { UiShowcaseCounterContract.State() }
                if (previous.count != 0) {
                    effects.trySend(UiShowcaseCounterContract.Effect.ResetCompleted)
                }
            }
        }
    }

    fun drainEffects(): List<UiShowcaseCounterContract.Effect> = buildList {
        while (true) {
            val effect = effects.tryReceive().getOrNull() ?: break
            add(effect)
        }
    }

    private inline fun updateState(
        update: (UiShowcaseCounterContract.State) -> UiShowcaseCounterContract.State,
    ): UiShowcaseCounterContract.State {
        var nextState = _state.value
        _state.update { current ->
            update(current).also { nextState = it }
        }
        return nextState
    }
}
