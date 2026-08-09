// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.game

import io.github.ronjunevaldoz.awake.core.input.Input
import kotlin.reflect.KClass

enum class GameWindowBackend {
    DEFAULT,
    VULKAN,
    WEBGPU,
    OPENGL,
}

data class GameWindowConfig(
    val title: String,
    val width: Int,
    val height: Int,
    val backend: GameWindowBackend,
)

interface GameInstaller {
    fun install(into: GameSpecBuilder)
}

/**
 * Common service lookup for game runtimes.
 */
interface GameServiceLookup {
    fun <T : Any> service(type: KClass<T>): T?

    fun <T : Any> requireService(type: KClass<T>): T = checkNotNull(service(type)) {
        "No game service registered for ${type.simpleName}."
    }
}

inline fun <reified T : Any> GameServiceLookup.service(): T? = service(T::class)

inline fun <reified T : Any> GameServiceLookup.requireService(): T = requireService(T::class)

/**
 * A single game session. Pure delegation to a [Game] implementation with
 * attached window configuration and services.
 */
class AwakeGame internal constructor(
    private val delegate: Game,
    val windowConfig: GameWindowConfig,
    private val services: Map<KClass<*>, Any>,
) : Game by delegate,
    GameServiceLookup {

    /** The session's input accumulator. Guaranteed to exist. */
    val input: Input get() = requireService(Input::class)

    // services is keyed by the exact KClass<T> each value was registered under (see
    // MutableGameServices.register), so `as? T` matches the entry's real type or the lookup
    // legitimately returns null for an unregistered type.
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(type: KClass<T>): T? = services[type] as? T
}

inline fun <reified T : Any> AwakeGame.service(): T? = service(T::class)

inline fun <reified T : Any> AwakeGame.requireService(): T = requireService(T::class)

fun GameWindowBackendBuilder.select(backend: GameWindowBackend) {
    when (backend) {
        GameWindowBackend.DEFAULT -> default()
        GameWindowBackend.VULKAN -> vulkan()
        GameWindowBackend.WEBGPU -> webGpu()
        GameWindowBackend.OPENGL -> openGl()
    }
}
