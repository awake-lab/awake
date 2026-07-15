// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import io.github.ronjunevaldoz.awake.render.renderer.Renderer

/**
 * A game's own behavior, injected into [GenericGameApplication] rather than provided by
 * inheriting from it (see docs/MVP_PLAN.md's Decision Log, "GenericGameApplication a
 * standalone render bootstrap") -- a plain implementation of this interface has no backend,
 * no `GenericGameApplication` reference, and is constructible/testable entirely on its own.
 *
 * [ready] receives [renderer] as a parameter (not through this interface's own constructor)
 * because it doesn't exist until the backend has actually built its GPU resources, which
 * happens asynchronously after `Application.create()` runs.
 *
 * Deliberately has no `fixedUpdate` -- forcing every [Game] to reason about a fixed-vs-
 * variable timestep split is itself a form of coupling. [render] receives the raw per-frame
 * `delta` and decides for itself whether/how to step simulation;
 * [io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop] is an optional tool a
 * [Game] can use internally if it wants deterministic stepping, not something
 * [GenericGameApplication] imposes on every game.
 *
 * [resize]/[pause]/[resume]/[dispose] mirror libGDX's `Game`->`Screen` delegation, all
 * defaulting to no-op so a simple [Game] (like `samples:hello-cube`'s `helloCubeGame()`
 * runtime) implements only the callbacks it actually needs.
 */
interface Game {
    suspend fun ready(renderer: Renderer)
    fun render(delta: Float, viewportWidth: Float, viewportHeight: Float)
    fun resize(width: Float, height: Float) {}
    fun pause() {}
    fun resume() {}
    fun dispose() {}
}
