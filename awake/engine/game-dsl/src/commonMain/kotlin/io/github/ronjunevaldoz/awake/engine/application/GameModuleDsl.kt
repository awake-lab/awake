// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

fun gameModule(
    block: GameModuleDsl.() -> Unit,
): GameModule = object : GameModule {
    override fun install(into: GameSpecBuilder) {
        GameModuleDsl(into).block()
    }
}

fun GameDsl.module(module: GameModule) {
    install(module)
}

fun GameDsl.module(
    block: GameModuleDsl.() -> Unit,
) {
    install(gameModule(block))
}

fun GameModule.createGame(
    window: WindowDsl.() -> Unit,
): AwakeGame = createGameSpec(window).createGame()

fun GameModule.createGameSpec(
    window: WindowDsl.() -> Unit,
): GameSpec = gameSpec {
    window(window)
    module(this@createGameSpec)
}

@AwakeGameDsl
class GameModuleDsl internal constructor(
    builder: GameSpecBuilder,
) : GameSpecDsl(builder) {

    fun module(module: GameModule) {
        install(module)
    }

    fun module(block: GameModuleDsl.() -> Unit) {
        install(gameModule(block))
    }
}
