// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class StarterGameUiState(
    val tipsVisible: Boolean = true
)

internal class StarterGameRuntimeState {
    private val _uiState = MutableStateFlow(StarterGameUiState())
    val uiState: StateFlow<StarterGameUiState> = _uiState.asStateFlow()

    var tipsVisible: Boolean
        get() = _uiState.value.tipsVisible
        set(value) {
            _uiState.update { it.copy(tipsVisible = value) }
        }
}
