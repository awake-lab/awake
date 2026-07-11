/*
 * Awake
 * Awake.awake-scene.commonMain
 *
 * Copyright (c) ronjunevaldoz 2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * Builds the navmesh for the MVP demo world (a hardcoded flat walkable area with the
 * existing decorative cube's footprint carved out as an obstacle -- see
 * [io.github.ronjunevaldoz.awake.scene.navigation.DemoNavMeshGeometry]). Returns `null` on
 * platforms with no navmesh backend yet (iOS, wasmJs) -- `recast4j` is plain JVM only, see
 * this module's build.gradle.kts.
 */
expect fun createDemoNavMesh(): NavMesh?
