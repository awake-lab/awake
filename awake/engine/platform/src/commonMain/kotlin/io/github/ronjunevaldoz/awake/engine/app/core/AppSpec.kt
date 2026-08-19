// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app.core

import io.github.ronjunevaldoz.awake.engine.app.config.WindowConfig
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import kotlin.reflect.KClass

/**
 * Immutable specification for an [AwakeAppLifecycle].
 */
class AppSpec internal constructor(
    val windowConfig: WindowConfig,
    private val onReady: List<suspend (Renderer) -> Unit>,
    private val onTick: List<(delta: Float, viewportWidth: Float, viewportHeight: Float) -> Unit>,
    private val onResize: List<(width: Float, height: Float) -> Unit>,
    private val onPause: List<() -> Unit>,
    private val onResume: List<() -> Unit>,
    private val onDispose: List<() -> Unit>,
    private val services: Map<KClass<*>, Any>,
) {
    fun createLifecycle(): AwakeAppLifecycle = AwakeAppLifecycle(
        delegate = object : AppLifecycle {
            override suspend fun ready(renderer: Renderer) {
                onReady.forEach { callback -> callback(renderer) }
            }

            override fun update(delta: Float, viewportWidth: Float, viewportHeight: Float) {
                onTick.forEach { callback -> callback(delta, viewportWidth, viewportHeight) }
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
        services = services,
    )
}
