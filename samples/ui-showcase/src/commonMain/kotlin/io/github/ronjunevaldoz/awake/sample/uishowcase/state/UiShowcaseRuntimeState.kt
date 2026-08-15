// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.state

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnAccent
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnBaseColor
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnStylePreset
import io.github.ronjunevaldoz.awake.ui.designsystem.shadcnThemeValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal data class UiShowcaseUiState(
    val showcaseStylePresetIndex: Int = 0,
    val showcaseBaseColorIndex: Int = 0,
    val showcaseAccentIndex: Int = 0,
    val showcaseThemeModeIndex: Int = UiShowcaseThemeMode.Light.ordinal,
    val tipsVisible: Boolean = true,
    val showcaseBadgeVariantIndex: Int = 0,
    val showcaseLiveBadge: Boolean = true,
    val showcaseDangerMode: Boolean = false,
    val showcaseSurfaceRadius: Float = 12f,
    val showcasePrimaryClicks: Int = 0,
    val showcaseCounterEffectMessage: String? = null,
    val showcaseNotifyChecked: Boolean = true,
)

internal class UiShowcaseRuntimeState {
    private val _uiState = MutableStateFlow(UiShowcaseUiState())
    val uiState: StateFlow<UiShowcaseUiState> = _uiState.asStateFlow()
    val counterStore = UiShowcaseCounterStore()

    var showcaseStylePresetIndex: Int
        get() = _uiState.value.showcaseStylePresetIndex
        set(value) {
            _uiState.update { it.copy(showcaseStylePresetIndex = value) }
        }

    var showcaseBaseColorIndex: Int
        get() = _uiState.value.showcaseBaseColorIndex
        set(value) {
            _uiState.update { it.copy(showcaseBaseColorIndex = value) }
        }

    var showcaseAccentIndex: Int
        get() = _uiState.value.showcaseAccentIndex
        set(value) {
            _uiState.update { it.copy(showcaseAccentIndex = value) }
        }

    var showcaseThemeModeIndex: Int
        get() = _uiState.value.showcaseThemeModeIndex
        set(value) {
            _uiState.update { it.copy(showcaseThemeModeIndex = value) }
        }

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

    var showcaseNotifyChecked: Boolean
        get() = _uiState.value.showcaseNotifyChecked
        set(value) {
            _uiState.update { it.copy(showcaseNotifyChecked = value) }
        }

    fun showcaseTheme(): UiThemeValues = shadcnThemeValues(
        preset = showcaseStylePreset(),
        baseColor = showcaseBaseColor(),
        accent = showcaseAccent(),
        dark = showcaseResolvedDarkMode(),
    )

    fun showcaseStylePreset(): ShadcnStylePreset = ShadcnStylePreset.entries.getOrElse(showcaseStylePresetIndex) {
        ShadcnStylePreset.Vega
    }

    fun showcaseBaseColor(): ShadcnBaseColor = ShadcnBaseColor.entries.getOrElse(showcaseBaseColorIndex) {
        ShadcnBaseColor.Neutral
    }

    fun showcaseAccent(): ShadcnAccent = ShadcnAccent.entries.getOrElse(showcaseAccentIndex) {
        ShadcnAccent.Base
    }

    fun showcaseThemeMode(): UiShowcaseThemeMode = UiShowcaseThemeMode.entries.getOrElse(showcaseThemeModeIndex) {
        UiShowcaseThemeMode.Light
    }

    fun showcaseResolvedDarkMode(): Boolean = when (showcaseThemeMode()) {
        UiShowcaseThemeMode.Auto -> platformPrefersDarkTheme()
        UiShowcaseThemeMode.Light -> false
        UiShowcaseThemeMode.Dark -> true
    }
}
