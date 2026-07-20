// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * An ECS system that operates on entities and components in a [World].
 */
interface System {
    /**
     * Defines how often this system should be updated. Defaults to [SystemFrequency.Simulation].
     */
    val frequency: SystemFrequency get() = SystemFrequency.Simulation

    /**
     * Called once per frame or fixed step depending on [frequency].
     */
    fun update(world: World, delta: Float)
}
