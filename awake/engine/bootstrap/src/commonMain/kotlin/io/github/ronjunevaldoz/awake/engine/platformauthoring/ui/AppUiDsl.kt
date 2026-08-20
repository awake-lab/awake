// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.platformauthoring.ui

import io.github.ronjunevaldoz.awake.engine.platformauthoring.dsl.AppSpecDsl
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme

typealias AppUiOverlayBlock = AppUiRuntime.() -> Unit
typealias AppUiReadyBlock = suspend AppUiRuntime.() -> Unit
typealias AppUiDisposeBlock = AppUiRuntime.() -> Unit

fun AppSpecDsl.ui(block: AppUiDsl.() -> Unit) {
    install(appUi(block))
}

fun AppSpecDsl.ui(spec: AppUiSpec) {
    install(spec)
}

fun appUi(block: AppUiDsl.() -> Unit): AppUiSpec = AppUiDsl().apply(block).build()

class AppUiDsl internal constructor() {
    private var defaultTheme: UiTheme = UiDefaultTheme
    private var defaultFont: UiFont = UiFonts.default()
    private val overlays = mutableListOf<AppUiOverlayBlock>()
    private var onReadyBlock: AppUiReadyBlock = {}
    private var onDisposeBlock: AppUiDisposeBlock = {}

    fun theme(theme: UiTheme) {
        defaultTheme = theme
    }

    /** Installs a public runtime-free theme contract through Core's neutral runtime adapter. */
    fun theme(theme: UiThemeValues) {
        defaultTheme = theme.asRuntimeTheme()
    }

    fun font(font: UiFont) {
        defaultFont = font
    }

    fun overlay(block: AppUiOverlayBlock) {
        overlays += block
    }

    fun onReady(block: AppUiReadyBlock) {
        onReadyBlock = block
    }

    fun onDispose(block: AppUiDisposeBlock) {
        onDisposeBlock = block
    }

    internal fun build(): AppUiSpec = AppUiSpec(
        theme = defaultTheme,
        font = defaultFont,
        overlays = overlays.toList(),
        onReadyBlock = onReadyBlock,
        onDisposeBlock = onDisposeBlock,
    )
}
