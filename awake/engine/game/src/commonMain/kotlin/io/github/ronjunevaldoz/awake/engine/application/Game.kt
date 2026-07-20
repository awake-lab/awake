// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.render.renderer.Renderer

/**
 * A game's own behavior, injected into [GenericGameApplication] rather than provided by
 * inheriting from it.
 */
interface Game {
    suspend fun ready(renderer: Renderer)
    fun render(delta: Float, viewportWidth: Float, viewportHeight: Float)
    fun resize(width: Float, height: Float) {}
    fun pause() {}
    fun resume() {}
    fun dispose() {}
}
