// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.navigation

import io.github.ronjunevaldoz.awake.core.math.Vec3

/**
 * MVP1a NavMesh slice (see docs/MMORPG_ROADMAP.md): a coarse-grained facade over whatever
 * navmesh library backs a given platform -- same principle as D5's physics facade in
 * docs/MVP_PLAN.md (a small, hand-designed contract, not a 1:1 mirror of the backend's own
 * API), so swapping `recast4j` for a different library or backend later stays contained to
 * one implementation, never touching gameplay code.
 */
interface NavMesh {
    /** Returns waypoints from [start] to [end], or an empty list if no path exists. */
    fun findPath(start: Vec3, end: Vec3): List<Vec3>
}
