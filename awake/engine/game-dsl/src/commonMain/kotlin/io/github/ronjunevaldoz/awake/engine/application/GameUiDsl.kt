// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.ui.theme.UiDefaultTheme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts

typealias GameUiOverlayBlock = GameUiRuntime.() -> Unit
typealias GameUiReadyBlock = suspend GameUiRuntime.() -> Unit
typealias GameUiDisposeBlock = GameUiRuntime.() -> Unit


fun GameDsl.ui(block: GameUiDsl.() -> Unit) {
    install(gameUi(block))
}

fun GameDsl.ui(spec: GameUiSpec) {
    install(spec)
}

fun GameModuleDsl.ui(block: GameUiDsl.() -> Unit) {
    install(gameUi(block))
}

fun GameModuleDsl.ui(spec: GameUiSpec) {
    install(spec)
}
fun gameUi(block: GameUiDsl.() -> Unit): GameUiSpec {
    return GameUiDsl().apply(block).build()
}

class GameUiDsl internal constructor() {
    private var defaultTheme: UiTheme = UiDefaultTheme
    private var defaultFont: UiFont = UiFonts.default()
    private val overlays = mutableListOf<GameUiOverlayBlock>()
    private var onReadyBlock: GameUiReadyBlock = {}
    private var onDisposeBlock: GameUiDisposeBlock = {}

    fun theme(theme: UiTheme) {
        defaultTheme = theme
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
