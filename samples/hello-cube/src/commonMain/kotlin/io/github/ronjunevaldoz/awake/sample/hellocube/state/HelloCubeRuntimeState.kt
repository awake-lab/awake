// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.state

import io.github.ronjunevaldoz.awake.engine.application.FrameStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal enum class HelloCubeCameraMode {
    STATIC,
    FREE_FLY
}

internal data class HelloCubeUiState(
    val mode: HelloCubeCameraMode = HelloCubeCameraMode.STATIC,
    val minimapEnabled: Boolean = false
)

internal class HelloCubeRuntimeState(
    val frameStats: FrameStats = FrameStats()
) {
    private val _uiState = MutableStateFlow(HelloCubeUiState())
    val uiState: StateFlow<HelloCubeUiState> = _uiState.asStateFlow()

    var mode: HelloCubeCameraMode
        get() = _uiState.value.mode
        set(value) {
            _uiState.update { it.copy(mode = value) }
        }

    var minimapEnabled: Boolean
        get() = _uiState.value.minimapEnabled
        set(value) {
            _uiState.update { it.copy(minimapEnabled = value) }
        }
}
