// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.app.core.AppSpec
import io.github.ronjunevaldoz.awake.engine.app.dsl.AppSpecBuilder
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle

fun gameModule(
    block: GameModuleDsl.() -> Unit,
): AppModule = object : AppModule {
    override fun install(into: AppSpecBuilder) {
        GameModuleDsl(into).block()
    }
}

fun GameDsl.module(module: AppModule) {
    install(module)
}

fun GameDsl.module(
    block: GameModuleDsl.() -> Unit,
) {
    install(gameModule(block))
}

fun AppModule.createGame(
    window: WindowDsl.() -> Unit,
): AwakeAppLifecycle = createGameSpec(window).createLifecycle()

fun AppModule.createGameSpec(
    window: WindowDsl.() -> Unit,
): AppSpec = gameSpec {
    window(window)
    module(this@createGameSpec)
}

@AwakeGameDsl
class GameModuleDsl internal constructor(
    builder: AppSpecBuilder,
) : GameSpecDsl(builder) {

    fun module(module: AppModule) {
        install(module)
    }

    fun module(block: GameModuleDsl.() -> Unit) {
        install(gameModule(block))
    }
}
