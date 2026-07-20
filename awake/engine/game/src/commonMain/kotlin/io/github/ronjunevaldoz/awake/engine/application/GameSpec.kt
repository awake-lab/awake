// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.reflect.KClass

/**
 * Immutable specification for an [AwakeGame].
 */
class GameSpec internal constructor(
    val windowConfig: GameWindowConfig,
    private val onReady: List<suspend (Renderer) -> Unit>,
    private val onRender: List<(delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit>,
    private val onResize: List<(width: Float, height: Float) -> Unit>,
    private val onPause: List<() -> Unit>,
    private val onResume: List<() -> Unit>,
    private val onDispose: List<() -> Unit>,
    private val services: Map<KClass<*>, Any>
) {
    fun createGame(): AwakeGame = AwakeGame(
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
        windowConfig = windowConfig,
        services = services
    )
}
