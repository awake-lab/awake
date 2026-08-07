// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.reflect.KClass

class GameSpecBuilder {
    private val onReady = mutableListOf<suspend (Renderer) -> Unit>()
    private val onRender = mutableListOf<(delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit>()
    private val onResize = mutableListOf<(width: Float, height: Float) -> Unit>()
    private val onPause = mutableListOf<() -> Unit>()
    private val onResume = mutableListOf<() -> Unit>()
    private val onDispose = mutableListOf<() -> Unit>()
    private val windowBuilder = GameWindowConfigBuilder()
    private val services = MutableGameServices()

    /** The single [Input] instance for this game session. Registered as a service up front. */
    val input = Input().also { services.register(Input::class, it) }

    fun window(block: GameWindowConfigBuilder.() -> Unit) {
        windowBuilder.apply(block)
    }

    fun windowBuilder(): GameWindowConfigBuilder = windowBuilder

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

    fun build(): GameSpec = GameSpec(
        windowConfig = windowBuilder.build(),
        onReady = onReady.toList(),
        onRender = onRender.toList(),
        onResize = onResize.toList(),
        onPause = onPause.toList(),
        onResume = onResume.toList(),
        onDispose = onDispose.toList(),
        services = services.snapshot(),
    )
}

class GameWindowConfigBuilder {
    var title: String = "Awake"
    private var width: Int = 800
    private var height: Int = 600
    val backend = GameWindowBackendBuilder()

    fun size(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun build(): GameWindowConfig = GameWindowConfig(
        title = title,
        width = width,
        height = height,
        backend = backend.selection,
    )
}

class GameWindowBackendBuilder {
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
