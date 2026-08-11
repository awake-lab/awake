// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.theme.asRuntimeTheme

typealias GameUiOverlayBlock = GameUiRuntime.() -> Unit
typealias GameUiReadyBlock = suspend GameUiRuntime.() -> Unit
typealias GameUiDisposeBlock = GameUiRuntime.() -> Unit

fun GameSpecDsl.ui(block: GameUiDsl.() -> Unit) {
    install(gameUi(block))
}

fun GameSpecDsl.ui(spec: GameUiSpec) {
    install(spec)
}

fun gameUi(block: GameUiDsl.() -> Unit): GameUiSpec = GameUiDsl().apply(block).build()

class GameUiDsl internal constructor() {
    private var defaultTheme: UiTheme = UiDefaultTheme
    private var defaultFont: UiFont = UiFonts.default()
    private val overlays = mutableListOf<GameUiOverlayBlock>()
    private var onReadyBlock: GameUiReadyBlock = {}
    private var onDisposeBlock: GameUiDisposeBlock = {}

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

    fun overlay(block: GameUiOverlayBlock) {
        overlays += block
    }

    fun onReady(block: GameUiReadyBlock) {
        onReadyBlock = block
    }

    fun onDispose(block: GameUiDisposeBlock) {
        onDisposeBlock = block
    }

    internal fun build(): GameUiSpec = GameUiSpec(
        theme = defaultTheme,
        font = defaultFont,
        overlays = overlays.toList(),
        onReadyBlock = onReadyBlock,
        onDisposeBlock = onDisposeBlock,
    )
}
