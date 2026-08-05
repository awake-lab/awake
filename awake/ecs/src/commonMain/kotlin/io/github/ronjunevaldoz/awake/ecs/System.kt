// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * An ECS system that operates on entities and components in a [World].
 */
interface System {
    /**
     * Called by the owning runtime or game loop.
     */
    fun update(world: World, delta: Float)
}
