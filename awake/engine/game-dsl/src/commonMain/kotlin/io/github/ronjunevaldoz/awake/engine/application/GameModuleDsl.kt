// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import kotlin.reflect.KClass

fun gameModule(
    block: GameModuleDsl.() -> Unit
): GameModule {
    return object : GameModule {
        override fun install(into: GameSpecBuilder) {
            GameModuleDsl(into).block()
        }
    }
}

fun GameDsl.module(module: GameModule) {
    install(module)
}

fun GameDsl.module(
    block: GameModuleDsl.() -> Unit
) {
    install(gameModule(block))
}

fun GameModule.createGame(
    window: WindowDsl.() -> Unit
): AwakeGame {
    return createGameSpec(window).createGame()
}

fun GameModule.createGameSpec(
    window: WindowDsl.() -> Unit
): GameSpec {
    return gameSpec {
        window(window)
        module(this@createGameSpec)
    }
}

@AwakeGameDsl
class GameModuleDsl internal constructor(
    private val builder: GameSpecBuilder
) {
    fun install(installer: GameInstaller) {
        builder.install(installer)
    }

    fun module(module: GameModule) {
        install(module)
    }

    fun module(block: GameModuleDsl.() -> Unit) {
        install(gameModule(block))
    }

    fun ready(block: suspend (io.github.ronjunevaldoz.awake.render.renderer.Renderer) -> Unit) {
        builder.ready(block)
    }

    fun render(block: (delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit) {
        builder.render(block)
    }

    fun resize(block: (width: Float, height: Float) -> Unit) {
        builder.resize(block)
    }

    fun pause(block: () -> Unit) {
        builder.pause(block)
    }

    fun resume(block: () -> Unit) {
        builder.resume(block)
    }

    fun dispose(block: () -> Unit) {
        builder.dispose(block)
    }

    fun <T : Any> service(type: KClass<T>, value: T) {
        builder.service(type, value)
    }

    fun <T : Any> service(type: KClass<T>): T? = builder.service(type)

    fun <T : Any> requireService(type: KClass<T>): T = builder.requireService(type)

    fun serviceLookup(): GameServiceLookup = builder.serviceLookup()
}
