// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.gameauthoring

import io.github.ronjunevaldoz.awake.engine.game.AwakeGame
import io.github.ronjunevaldoz.awake.engine.game.GameInstaller
import io.github.ronjunevaldoz.awake.engine.game.GameServiceLookup
import io.github.ronjunevaldoz.awake.engine.game.GameSpec
import io.github.ronjunevaldoz.awake.engine.game.GameSpecBuilder
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackendBuilder
import io.github.ronjunevaldoz.awake.engine.game.GameWindowConfigBuilder
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.reflect.KClass

@DslMarker
annotation class AwakeGameDsl

fun game(
    block: GameDsl.() -> Unit,
): AwakeGame = gameSpec(block).createGame()

fun gameSpec(
    block: GameDsl.() -> Unit,
): GameSpec {
    val builder = GameSpecBuilder()
    GameDsl(builder).block()
    return builder.build()
}

// Shared forwarding surface for both GameDsl and GameModuleDsl -- they wrap the same
// GameSpecBuilder and expose the same install/lifecycle/service methods. GameDsl adds
// `window(...)` on top since only the top-level game {} block owns window configuration;
// a GameModule installs into an already-windowed spec.
@AwakeGameDsl
sealed class GameSpecDsl(
    protected val builder: GameSpecBuilder,
) {
    fun install(installer: GameInstaller) {
        builder.install(installer)
    }

    fun ready(block: suspend (Renderer) -> Unit) {
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

@AwakeGameDsl
class GameDsl internal constructor(
    builder: GameSpecBuilder,
) : GameSpecDsl(builder) {
    fun window(block: WindowDsl.() -> Unit) {
        WindowDsl(builder.windowBuilder()).apply(block)
    }
}

@AwakeGameDsl
class WindowDsl internal constructor(
    private val builder: GameWindowConfigBuilder,
) {
    var title: String
        get() = builder.title
        set(value) {
            builder.title = value
        }

    val backend = WindowBackendDsl(builder.backend)

    fun size(width: Int, height: Int) {
        builder.size(width, height)
    }
}

@AwakeGameDsl
class WindowBackendDsl internal constructor(
    private val builder: GameWindowBackendBuilder,
) {
    fun default() {
        builder.default()
    }

    fun vulkan() {
        builder.vulkan()
    }

    fun webGpu() {
        builder.webGpu()
    }

    fun openGl() {
        builder.openGl()
    }
}

fun WindowBackendDsl.select(backend: GameWindowBackend) {
    when (backend) {
        GameWindowBackend.DEFAULT -> default()
        GameWindowBackend.VULKAN -> vulkan()
        GameWindowBackend.WEBGPU -> webGpu()
        GameWindowBackend.OPENGL -> openGl()
    }
}
