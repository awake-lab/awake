// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class UiShowcaseUiState(
    val tipsVisible: Boolean = true,
    val showcaseBadgeVariantIndex: Int = 0,
    val showcaseLiveBadge: Boolean = true,
    val showcaseDangerMode: Boolean = false,
    val showcaseSurfaceRadius: Float = 12f,
    val showcasePrimaryClicks: Int = 0,
    val showcaseCounterEffectMessage: String? = null
)

internal class UiShowcaseRuntimeState {
    private val _uiState = MutableStateFlow(UiShowcaseUiState())
    val uiState: StateFlow<UiShowcaseUiState> = _uiState.asStateFlow()
    val counterStore = UiShowcaseCounterStore()

    var tipsVisible: Boolean
        get() = _uiState.value.tipsVisible
        set(value) {
            _uiState.update { it.copy(tipsVisible = value) }
        }

    var showcaseBadgeVariantIndex: Int
        get() = _uiState.value.showcaseBadgeVariantIndex
        set(value) {
            _uiState.update { it.copy(showcaseBadgeVariantIndex = value) }
        }

    var showcaseLiveBadge: Boolean
        get() = _uiState.value.showcaseLiveBadge
        set(value) {
            _uiState.update { it.copy(showcaseLiveBadge = value) }
        }

    var showcaseDangerMode: Boolean
        get() = _uiState.value.showcaseDangerMode
        set(value) {
            _uiState.update { it.copy(showcaseDangerMode = value) }
        }

    var showcaseSurfaceRadius: Float
        get() = _uiState.value.showcaseSurfaceRadius
        set(value) {
            _uiState.update { it.copy(showcaseSurfaceRadius = value) }
        }

    var showcasePrimaryClicks: Int
        get() = _uiState.value.showcasePrimaryClicks
        set(value) {
            _uiState.update { it.copy(showcasePrimaryClicks = value) }
        }

    var showcaseCounterEffectMessage: String?
        get() = _uiState.value.showcaseCounterEffectMessage
        set(value) {
            _uiState.update { it.copy(showcaseCounterEffectMessage = value) }
        }
}
