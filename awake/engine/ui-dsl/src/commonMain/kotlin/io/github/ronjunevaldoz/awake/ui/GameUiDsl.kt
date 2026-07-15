// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.engine.application.GameDsl

typealias GameUiOverlayBlock = GameUiRuntime.(viewportWidth: Float, viewportHeight: Float) -> Unit
typealias GameUiReadyBlock = suspend GameUiRuntime.() -> Unit
typealias GameUiDisposeBlock = GameUiRuntime.() -> Unit

fun GameDsl.ui(block: GameUiDsl.() -> Unit) {
    install(gameUi(block))
}

fun GameDsl.ui(spec: GameUiSpec) {
    install(spec)
}

fun gameUi(block: GameUiDsl.() -> Unit): GameUiSpec {
    return GameUiDsl().apply(block).build()
}

class GameUiDsl internal constructor() {
    private val overlays = mutableListOf<GameUiOverlayBlock>()
    private var onReadyBlock: GameUiReadyBlock = {}
    private var onDisposeBlock: GameUiDisposeBlock = {}

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
        overlays = overlays.toList(),
        onReadyBlock = onReadyBlock,
        onDisposeBlock = onDisposeBlock
    )
}
