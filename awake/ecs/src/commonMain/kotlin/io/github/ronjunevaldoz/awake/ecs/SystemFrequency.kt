// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ecs

/**
 * Defines when a [System] should be updated within the game loop.
 */
enum class SystemFrequency {
    /** Run during the fixed-timestep simulation pass (gameplay, physics). Default. */
    Simulation,
    
    /** Run during every render frame (infrastructure, transforms, rendering). */
    Infrastructure
}
