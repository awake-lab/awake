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

class AwakeGame internal constructor(
    private val delegate: Game,
    val windowConfig: GameWindowConfig,
    private val services: Map<KClass<*>, Any>
) : Game by delegate {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> service(type: KClass<T>): T? = services[type] as? T

    fun <T : Any> requireService(type: KClass<T>): T = checkNotNull(service(type)) {
        "No game service registered for ${type.simpleName}."
    }
}

inline fun <reified T : Any> AwakeGame.service(): T? = service(T::class)

inline fun <reified T : Any> AwakeGame.requireService(): T = requireService(T::class)

@AwakeGameDsl
class GameDsl internal constructor() {
    private var onReady: suspend (Renderer) -> Unit = {}
    private var onRender: (delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit = { _, _, _ -> }
    private var onResize: (width: Float, height: Float) -> Unit = { _, _ -> }
    private var onPause: () -> Unit = {}
    private var onResume: () -> Unit = {}
    private var onDispose: () -> Unit = {}
    private val windowDsl = WindowDsl()
    private val services = linkedMapOf<KClass<*>, Any>()

    fun window(block: WindowDsl.() -> Unit) {
        windowDsl.apply(block)
    }

    fun install(installer: GameInstaller) {
        installer.install(this)
    }

    fun ready(block: suspend (Renderer) -> Unit) {
        onReady = block
    }

    fun render(block: (delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit) {
        onRender = block
    }

    fun resize(block: (width: Float, height: Float) -> Unit) {
        onResize = block
    }

    fun pause(block: () -> Unit) {
        onPause = block
    }

    fun resume(block: () -> Unit) {
        onResume = block
    }

    fun dispose(block: () -> Unit) {
        onDispose = block
    }

    fun <T : Any> service(type: KClass<T>, value: T) {
        services[type] = value
    }

    internal fun build(): AwakeGame = AwakeGame(
        delegate = object : Game {
            override suspend fun ready(renderer: Renderer) = onReady(renderer)

            override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
                onRender(delta, viewportWidth, viewportHeight)
            }

            override fun resize(width: Float, height: Float) {
                onResize(width, height)
            }

            override fun pause() {
                onPause()
            }

            override fun resume() {
                onResume()
            }

            override fun dispose() {
                onDispose()
            }
        },
        windowConfig = windowDsl.build(),
        services = services.toMap()
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
