// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World

/**
 * Owns the currently-loaded [Scene], if any -- the only thing that may create or destroy one
 * against [world]. A caller that instantiates a [Scene] directly and never registers it here
 * has opted out of safe switching, same as bypassing any other single-owner resource.
 *
 * Tied to one [World] for its whole lifetime by design: a [SceneGameRuntime] never reassigns
 * its own `world` after setup, so there is no real case where a single [SceneManager] needs to
 * switch which [World] it targets.
 */
class SceneManager(private val world: World) {
    var current: Scene? = null
        private set

    /** Tears down whatever's currently loaded (if anything), then instantiates [document].
     * One call, not a manual teardown-then-load pair -- there is no window where a caller can
     * forget the teardown half. */
    fun switchTo(document: SceneDocument): Scene {
        current?.destroy()
        val scene = SceneLoader.instantiate(document, world)
        current = scene
        return scene
    }

    /** Tears down the current scene without loading a replacement -- e.g. app shutdown. */
    fun close() {
        current?.destroy()
        current = null
    }
}
