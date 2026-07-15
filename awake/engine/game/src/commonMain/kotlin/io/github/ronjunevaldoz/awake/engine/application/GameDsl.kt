// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.reflect.KClass

@DslMarker
annotation class AwakeGameDsl

fun game(
    block: GameDsl.() -> Unit
): AwakeGame {
    val builder = GameDsl()
    builder.block()
    return builder.build()
}

enum class GameWindowBackend {
    DEFAULT,
    VULKAN,
    WEBGPU,
    OPENGL
}

data class GameWindowConfig(
    val title: String,
    val width: Int,
    val height: Int,
    val backend: GameWindowBackend
)

interface GameInstaller {
    fun install(into: GameDsl)
}

interface GameServiceLookup {
    fun <T : Any> service(type: KClass<T>): T?

    fun <T : Any> requireService(type: KClass<T>): T = checkNotNull(service(type)) {
        "No game service registered for ${type.simpleName}."
    }
}

inline fun <reified T : Any> GameServiceLookup.service(): T? = service(T::class)

inline fun <reified T : Any> GameServiceLookup.requireService(): T = requireService(T::class)

class AwakeGame internal constructor(
    private val delegate: Game,
    val windowConfig: GameWindowConfig,
    private val services: Map<KClass<*>, Any>
) : Game by delegate, GameServiceLookup {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(type: KClass<T>): T? = services[type] as? T
}

inline fun <reified T : Any> AwakeGame.service(): T? = service(T::class)

inline fun <reified T : Any> AwakeGame.requireService(): T = requireService(T::class)

@AwakeGameDsl
class GameDsl internal constructor() {
    private val onReady = mutableListOf<suspend (Renderer) -> Unit>()
    private val onRender = mutableListOf<(delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit>()
    private val onResize = mutableListOf<(width: Float, height: Float) -> Unit>()
    private val onPause = mutableListOf<() -> Unit>()
    private val onResume = mutableListOf<() -> Unit>()
    private val onDispose = mutableListOf<() -> Unit>()
    private val windowDsl = WindowDsl()
    private val services = MutableGameServices()

    fun window(block: WindowDsl.() -> Unit) {
        windowDsl.apply(block)
    }

    fun install(installer: GameInstaller) {
        installer.install(this)
    }

    fun ready(block: suspend (Renderer) -> Unit) {
        onReady += block
    }

    fun render(block: (delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit) {
        onRender += block
    }

    fun resize(block: (width: Float, height: Float) -> Unit) {
        onResize += block
    }

    fun pause(block: () -> Unit) {
        onPause += block
    }

    fun resume(block: () -> Unit) {
        onResume += block
    }

    fun dispose(block: () -> Unit) {
        onDispose += block
    }

    fun <T : Any> service(type: KClass<T>, value: T) {
        services.register(type, value)
    }

    fun <T : Any> service(type: KClass<T>): T? = services.service(type)

    fun <T : Any> requireService(type: KClass<T>): T = services.requireService(type)

    fun serviceLookup(): GameServiceLookup = services

    internal fun <T : Any> registerService(type: KClass<T>, value: T) {
        services.register(type, value)
    }

    internal fun build(): AwakeGame = AwakeGame(
        delegate = object : Game {
            override suspend fun ready(renderer: Renderer) {
                onReady.forEach { callback -> callback(renderer) }
            }

            override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
                onRender.forEach { callback -> callback(delta, viewportWidth, viewportHeight) }
            }

            override fun resize(width: Float, height: Float) {
                onResize.forEach { callback -> callback(width, height) }
            }

            override fun pause() {
                onPause.forEach { callback -> callback() }
            }

            override fun resume() {
                onResume.forEach { callback -> callback() }
            }

            override fun dispose() {
                onDispose.asReversed().forEach { callback -> callback() }
            }
        },
        windowConfig = windowDsl.build(),
        services = services.snapshot()
    )
}

@AwakeGameDsl
class WindowDsl internal constructor() {
    var title: String = "Awake"
    private var width: Int = 800
    private var height: Int = 600
    val backend = WindowBackendDsl()

    fun size(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    internal fun build(): GameWindowConfig = GameWindowConfig(
        title = title,
        width = width,
        height = height,
        backend = backend.selection
    )
}

@AwakeGameDsl
class WindowBackendDsl internal constructor() {
    internal var selection: GameWindowBackend = GameWindowBackend.DEFAULT

    fun default() {
        selection = GameWindowBackend.DEFAULT
    }

    fun vulkan() {
        selection = GameWindowBackend.VULKAN
    }

    fun webGpu() {
        selection = GameWindowBackend.WEBGPU
    }

    fun openGl() {
        selection = GameWindowBackend.OPENGL
    }
}

private class MutableGameServices : GameServiceLookup {
    private val services = linkedMapOf<KClass<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> service(type: KClass<T>): T? = services[type] as? T

    fun <T : Any> register(type: KClass<T>, value: T) {
        services[type] = value
    }

    fun snapshot(): Map<KClass<*>, Any> = services.toMap()
}
